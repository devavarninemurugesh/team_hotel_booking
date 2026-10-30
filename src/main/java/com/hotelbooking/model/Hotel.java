package com.hotelbooking.model;

import static com.hotelbooking.model.User.required;

public final class Hotel {
    private final String id;
    private String name;
    private String city;
    private String address;
    private final String managerId;

    public Hotel(String id, String name, String city, String address, String managerId) {
        this.id = required(id, "Hotel ID");
        this.name = required(name, "Hotel name");
        this.city = required(city, "City");
        this.address = required(address, "Address");
        this.managerId = required(managerId, "Manager ID");
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getAddress() { return address; }
    public String getManagerId() { return managerId; }
    public void setName(String name) { this.name = required(name, "Hotel name"); }
    public void setCity(String city) { this.city = required(city, "City"); }
    public void setAddress(String address) { this.address = required(address, "Address"); }
}
