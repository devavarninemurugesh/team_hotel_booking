-- Relational design exercise only; the Java application uses in-memory collections.
CREATE TABLE users (
    user_id VARCHAR(40) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE,
    phone VARCHAR(16) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('GUEST', 'ADMIN', 'HOTEL_MANAGER')),
    password_hash VARCHAR(512) NOT NULL,
    id_proof_type VARCHAR(30),
    id_proof_number VARCHAR(100)
);

CREATE TABLE hotels (
    hotel_id VARCHAR(40) PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address VARCHAR(300) NOT NULL,
    manager_id VARCHAR(40) NOT NULL REFERENCES users(user_id)
);

CREATE TABLE rooms (
    room_id VARCHAR(40) PRIMARY KEY,
    hotel_id VARCHAR(40) NOT NULL REFERENCES hotels(hotel_id),
    room_number VARCHAR(30) NOT NULL,
    room_type VARCHAR(20) NOT NULL,
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    nightly_rate DECIMAL(12, 2) NOT NULL CHECK (nightly_rate > 0),
    UNIQUE (hotel_id, room_number)
);

CREATE TABLE bookings (
    booking_id VARCHAR(50) PRIMARY KEY,
    guest_id VARCHAR(40) NOT NULL REFERENCES users(user_id),
    hotel_id VARCHAR(40) NOT NULL REFERENCES hotels(hotel_id),
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    total_price DECIMAL(12, 2) NOT NULL CHECK (total_price > 0),
    status VARCHAR(24) NOT NULL,
    CHECK (check_out > check_in)
);

CREATE TABLE booking_rooms (
    booking_id VARCHAR(50) NOT NULL REFERENCES bookings(booking_id),
    room_id VARCHAR(40) NOT NULL REFERENCES rooms(room_id),
    PRIMARY KEY (booking_id, room_id)
);

CREATE TABLE payments (
    payment_id VARCHAR(50) PRIMARY KEY,
    booking_id VARCHAR(50) NOT NULL REFERENCES bookings(booking_id),
    method VARCHAR(20) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL CHECK (amount > 0),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE maintenance_blocks (
    maintenance_id VARCHAR(50) PRIMARY KEY,
    room_id VARCHAR(40) NOT NULL REFERENCES rooms(room_id),
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    CHECK (check_out > check_in)
);

CREATE TABLE reviews (
    review_id VARCHAR(50) PRIMARY KEY,
    booking_id VARCHAR(50) NOT NULL UNIQUE REFERENCES bookings(booking_id),
    guest_id VARCHAR(40) NOT NULL REFERENCES users(user_id),
    hotel_id VARCHAR(40) NOT NULL REFERENCES hotels(hotel_id),
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
