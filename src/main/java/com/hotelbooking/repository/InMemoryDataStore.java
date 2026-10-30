package com.hotelbooking.repository;

import com.hotelbooking.exception.DuplicateRecordException;
import com.hotelbooking.exception.NotFoundException;
import com.hotelbooking.model.*;

import java.util.*;

public final class InMemoryDataStore {
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, String> credentialHashes = new HashMap<>();
    private final Map<String, Hotel> hotels = new LinkedHashMap<>();
    private final Map<String, Room> rooms = new LinkedHashMap<>();
    private final Map<String, Booking> bookings = new LinkedHashMap<>();
    private final List<BookingRoom> bookingRooms = new ArrayList<>();
    private final Map<String, Payment> payments = new LinkedHashMap<>();
    private final Map<String, MaintenanceBlock> maintenanceBlocks = new LinkedHashMap<>();
    private final Map<String, Review> reviews = new LinkedHashMap<>();

    public void addUser(User user, String credentialHash) {
        Objects.requireNonNull(user, "User");
        Objects.requireNonNull(credentialHash, "Credential hash");
        ensureAbsent(users, user.getId(), "User");
        if (findUserByEmail(user.getEmail()).isPresent()) {
            throw new DuplicateRecordException("A user with this email already exists.");
        }
        users.put(user.getId(), user);
        credentialHashes.put(user.getId(), credentialHash);
    }

    public Optional<User> findUser(String id) { return Optional.ofNullable(users.get(id)); }
    public Optional<User> findUserByEmail(String email) {
        return users.values().stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst();
    }
    public Optional<String> findCredentialHash(String userId) {
        return Optional.ofNullable(credentialHashes.get(userId));
    }
    public List<User> getUsers() { return List.copyOf(users.values()); }

    public void addHotel(Hotel hotel) {
        Objects.requireNonNull(hotel, "Hotel");
        ensureAbsent(hotels, hotel.getId(), "Hotel");
        hotels.put(hotel.getId(), hotel);
    }
    public Optional<Hotel> findHotel(String id) { return Optional.ofNullable(hotels.get(id)); }
    public List<Hotel> getHotels() { return List.copyOf(hotels.values()); }

    public void addRoom(Room room) {
        Objects.requireNonNull(room, "Room");
        ensureAbsent(rooms, room.getId(), "Room");
        if (rooms.values().stream().anyMatch(existing ->
                existing.getHotelId().equals(room.getHotelId())
                        && existing.getRoomNumber().equalsIgnoreCase(room.getRoomNumber()))) {
            throw new DuplicateRecordException("Room number already exists in this hotel.");
        }
        rooms.put(room.getId(), room);
    }
    public Optional<Room> findRoom(String id) { return Optional.ofNullable(rooms.get(id)); }
    public List<Room> getRooms() { return List.copyOf(rooms.values()); }

    public void addBooking(Booking booking) {
        Objects.requireNonNull(booking, "Booking");
        ensureAbsent(bookings, booking.getId(), "Booking");
        bookings.put(booking.getId(), booking);
    }
    public Optional<Booking> findBooking(String id) { return Optional.ofNullable(bookings.get(id)); }
    public List<Booking> getBookings() { return List.copyOf(bookings.values()); }
    public List<Booking> getBookingsForGuest(String guestId) {
        return bookings.values().stream().filter(b -> b.getGuestId().equals(guestId)).toList();
    }
    public void addBookingRoom(BookingRoom association) {
        Objects.requireNonNull(association, "Booking-room association");
        if (bookingRooms.contains(association)) {
            throw new DuplicateRecordException("Room is already associated with this booking.");
        }
        bookingRooms.add(association);
    }
    public List<BookingRoom> getBookingRooms() { return List.copyOf(bookingRooms); }
    public List<BookingRoom> getBookingRooms(String bookingId) {
        return bookingRooms.stream().filter(br -> br.bookingId().equals(bookingId)).toList();
    }

    public void addPayment(Payment payment) {
        Objects.requireNonNull(payment, "Payment");
        ensureAbsent(payments, payment.getId(), "Payment");
        payments.put(payment.getId(), payment);
    }
    public Optional<Payment> findPayment(String id) { return Optional.ofNullable(payments.get(id)); }
    public List<Payment> getPayments() { return List.copyOf(payments.values()); }
    public List<Payment> getPaymentsForBooking(String bookingId) {
        return payments.values().stream().filter(p -> p.getBookingId().equals(bookingId)).toList();
    }

    public void addMaintenanceBlock(MaintenanceBlock block) {
        Objects.requireNonNull(block, "Maintenance block");
        ensureAbsent(maintenanceBlocks, block.getId(), "Maintenance block");
        maintenanceBlocks.put(block.getId(), block);
    }
    public List<MaintenanceBlock> getMaintenanceBlocks() { return List.copyOf(maintenanceBlocks.values()); }
    public List<MaintenanceBlock> getMaintenanceBlocksForRoom(String roomId) {
        return maintenanceBlocks.values().stream().filter(b -> b.getRoomId().equals(roomId)).toList();
    }

    public void addReview(Review review) {
        Objects.requireNonNull(review, "Review");
        ensureAbsent(reviews, review.getId(), "Review");
        if (reviews.values().stream().anyMatch(r -> r.getBookingId().equals(review.getBookingId()))) {
            throw new DuplicateRecordException("A review already exists for this booking.");
        }
        reviews.put(review.getId(), review);
    }
    public List<Review> getReviews() { return List.copyOf(reviews.values()); }
    public List<Review> getReviewsForHotel(String hotelId) {
        return reviews.values().stream().filter(r -> r.getHotelId().equals(hotelId)).toList();
    }

    private static <T> void ensureAbsent(Map<String, T> records, String id, String type) {
        if (records.containsKey(id)) throw new DuplicateRecordException(type + " ID already exists: " + id);
    }
}
