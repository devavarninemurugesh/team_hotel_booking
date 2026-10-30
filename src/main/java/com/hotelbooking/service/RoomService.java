package com.hotelbooking.service;

import com.hotelbooking.exception.NotFoundException;
import com.hotelbooking.model.*;
import com.hotelbooking.repository.InMemoryDataStore;
import com.hotelbooking.util.Ids;

import java.math.BigDecimal;
import java.util.List;

public final class RoomService {
    private final InMemoryDataStore store;
    public RoomService(InMemoryDataStore store) { this.store = store; }

    public Room createRoom(String hotelId, String number, RoomType type, int capacity, BigDecimal nightlyRate) {
        store.findHotel(hotelId).orElseThrow(() -> new NotFoundException("Hotel was not found."));
        String id = Ids.newId("ROM");
        Room room = switch (type) {
            case SINGLE -> new SingleRoom(id, hotelId, number, capacity, nightlyRate);
            case DOUBLE -> new DoubleRoom(id, hotelId, number, capacity, nightlyRate);
            case DELUXE -> new DeluxeRoom(id, hotelId, number, capacity, nightlyRate);
            case SUITE -> new SuiteRoom(id, hotelId, number, capacity, nightlyRate);
        };
        store.addRoom(room);
        return room;
    }
    public List<Room> getRoomsForHotel(String hotelId) {
        return store.getRooms().stream().filter(r -> r.getHotelId().equals(hotelId)).toList();
    }
}
