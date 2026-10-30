package com.hotelbooking.service;

import com.hotelbooking.exception.NotFoundException;
import com.hotelbooking.exception.BusinessRuleException;
import com.hotelbooking.model.*;
import com.hotelbooking.repository.InMemoryDataStore;
import com.hotelbooking.util.Ids;
import java.util.List;

public final class HotelService {
    private final InMemoryDataStore store;
    public HotelService(InMemoryDataStore store) { this.store = store; }

    public Hotel createHotel(String name, String city, String address, String managerId) {
        User manager = store.findUser(managerId)
                .orElseThrow(() -> new NotFoundException("Hotel manager was not found."));
        if (!(manager instanceof HotelManager)) throw new BusinessRuleException("Owner must be a hotel manager.");
        Hotel hotel = new Hotel(Ids.newId("HTL"), name, city, address, managerId);
        store.addHotel(hotel);
        return hotel;
    }
    public List<Hotel> listHotels() { return store.getHotels(); }
    public List<Hotel> hotelsManagedBy(String managerId) {
        return store.getHotels().stream().filter(h -> h.getManagerId().equals(managerId)).toList();
    }
}
