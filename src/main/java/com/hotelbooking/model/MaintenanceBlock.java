package com.hotelbooking.model;

import com.hotelbooking.exception.ValidationException;
import java.time.LocalDate;

public final class MaintenanceBlock {
    private final String id;
    private final String roomId;
    private final LocalDate checkIn;
    private final LocalDate checkOut;

    public MaintenanceBlock(String id, String roomId, LocalDate checkIn, LocalDate checkOut) {
        this.id = User.required(id, "Maintenance ID");
        this.roomId = User.required(roomId, "Room ID");
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new ValidationException("Maintenance end date must be after its start date.");
        }
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }
    public String getId() { return id; }
    public String getRoomId() { return roomId; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
}
