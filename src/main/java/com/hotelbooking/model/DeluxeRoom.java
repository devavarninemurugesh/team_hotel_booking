package com.hotelbooking.model;
import java.math.BigDecimal;
public final class DeluxeRoom extends Room {
    public DeluxeRoom(String id, String hotelId, String number, int capacity, BigDecimal rate) {
        super(id, hotelId, number, capacity, rate);
    }
    @Override public RoomType getType() { return RoomType.DELUXE; }
}
