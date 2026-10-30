-- SQL practice only; queries are not used by the in-memory Java application.

-- Find rooms in a city that can hold the requested party and have no overlapping
-- confirmed/checked-in booking or maintenance block for [arrival, departure).
SELECT r.room_id, h.name AS hotel_name, h.city, r.room_number, r.room_type, r.nightly_rate
FROM rooms r
JOIN hotels h ON h.hotel_id = r.hotel_id
WHERE LOWER(h.city) = LOWER(:city)
  AND r.capacity >= :guest_count
  AND NOT EXISTS (
      SELECT 1
      FROM booking_rooms br
      JOIN bookings b ON b.booking_id = br.booking_id
      WHERE br.room_id = r.room_id
        AND b.status <> 'CANCELLED'
        AND b.check_in < :departure
        AND :arrival < b.check_out
  )
  AND NOT EXISTS (
      SELECT 1 FROM maintenance_blocks m
      WHERE m.room_id = r.room_id
        AND m.check_in < :departure
        AND :arrival < m.check_out
  );

-- Guest's booking history and assigned rooms.
SELECT b.booking_id, b.check_in, b.check_out, b.status, b.total_price,
       h.name AS hotel_name, r.room_number
FROM bookings b
JOIN hotels h ON h.hotel_id = b.hotel_id
JOIN booking_rooms br ON br.booking_id = b.booking_id
JOIN rooms r ON r.room_id = br.room_id
WHERE b.guest_id = :guest_id
ORDER BY b.check_in DESC;

-- Average guest rating per hotel.
SELECT h.hotel_id, h.name, ROUND(AVG(rv.rating), 2) AS average_rating, COUNT(rv.review_id) AS review_count
FROM hotels h
LEFT JOIN reviews rv ON rv.hotel_id = h.hotel_id
GROUP BY h.hotel_id, h.name
ORDER BY average_rating DESC;

-- Manager's upcoming bookings.
SELECT b.booking_id, b.check_in, b.check_out, b.status, u.name AS guest_name, h.name AS hotel_name
FROM bookings b
JOIN hotels h ON h.hotel_id = b.hotel_id
JOIN users u ON u.user_id = b.guest_id
WHERE h.manager_id = :manager_id
  AND b.check_in >= CURRENT_DATE
ORDER BY b.check_in;

-- Payment reconciliation by payment status.
SELECT method, status, SUM(amount) AS total_amount, COUNT(*) AS payment_count
FROM payments
GROUP BY method, status
ORDER BY method, status;
