package com.hotelbooking.model;

import com.hotelbooking.exception.ValidationException;

public abstract class User {
    private final String id;
    private String name;
    private String email;
    private String phone;

    protected User(String id, String name, String email, String phone) {
        this.id = required(id, "User ID");
        setName(name);
        setEmail(email);
        setPhone(phone);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public abstract UserRole getRole();

    public void setName(String name) { this.name = required(name, "Name"); }
    public void setEmail(String email) {
        String value = required(email, "Email");
        if (!value.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ValidationException("Email address is invalid.");
        }
        this.email = value.toLowerCase();
    }
    public void setPhone(String phone) {
        String value = required(phone, "Phone");
        if (!value.matches("^\\+?[0-9]{10,15}$")) {
            throw new ValidationException("Phone must contain 10 to 15 digits, optionally prefixed by +.");
        }
        this.phone = value;
    }

    protected static String required(String value, String field) {
        if (value == null) throw new ValidationException(field + " cannot be null.");
        String normalized = value.trim();
        if (normalized.isEmpty()) throw new ValidationException(field + " cannot be blank.");
        return normalized;
    }
}
