package com.hotelbooking.model;

import com.hotelbooking.exception.ValidationException;
import java.math.BigDecimal;

public abstract class Room {
    private final String id;
    private final String hotelId;
    private final String roomNumber;
    private final int capacity;
    private BigDecimal nightlyRate;

    protected Room(String id, String hotelId, String roomNumber, int capacity, BigDecimal nightlyRate) {
        this.id = User.required(id, "Room ID");
        this.hotelId = User.required(hotelId, "Hotel ID");
        this.roomNumber = User.required(roomNumber, "Room number");
        if (capacity < 1) throw new ValidationException("Room capacity must be positive.");
        this.capacity = capacity;
        setNightlyRate(nightlyRate);
    }

    public String getId() { return id; }
    public String getHotelId() { return hotelId; }
    public String getRoomNumber() { return roomNumber; }
    public int getCapacity() { return capacity; }
    public BigDecimal getNightlyRate() { return nightlyRate; }
    public abstract RoomType getType();
    public void setNightlyRate(BigDecimal rate) {
        if (rate == null || rate.signum() <= 0) throw new ValidationException("Nightly rate must be positive.");
        this.nightlyRate = rate.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
