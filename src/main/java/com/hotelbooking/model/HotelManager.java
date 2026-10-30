package com.hotelbooking.model;

public final class HotelManager extends User {
    public HotelManager(String id, String name, String email, String phone) {
        super(id, name, email, phone);
    }
    @Override public UserRole getRole() { return UserRole.HOTEL_MANAGER; }
}
