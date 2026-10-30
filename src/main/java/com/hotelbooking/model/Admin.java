package com.hotelbooking.model;

public final class Admin extends User {
    public Admin(String id, String name, String email, String phone) {
        super(id, name, email, phone);
    }
    @Override public UserRole getRole() { return UserRole.ADMIN; }
}
