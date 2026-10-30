package com.hotelbooking.model;

public record BookingRoom(String bookingId, String roomId) {
    public BookingRoom {
        User.required(bookingId, "Booking ID");
        User.required(roomId, "Room ID");
    }
}
