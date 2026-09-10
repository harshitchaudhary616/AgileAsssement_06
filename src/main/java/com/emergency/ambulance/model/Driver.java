package com.emergency.ambulance.model;

import java.util.Objects;

public class Driver {
    private final String driverId;
    private final String name;
    private final String phone;

    public Driver(String driverId, String name, String phone) {
        if (isBlank(driverId) || isBlank(name) || isBlank(phone))
            throw new IllegalArgumentException("Driver id, name and phone are required.");
        this.driverId = driverId.trim();
        this.name = name.trim();
        this.phone = phone.trim();
    }
    private boolean isBlank(String s){ return s == null || s.trim().isEmpty(); }
    public String getDriverId(){ return driverId; }
    public String getName(){ return name; }
    public String getPhone(){ return phone; }
    @Override public boolean equals(Object o){ return o instanceof Driver d && driverId.equals(d.driverId); }
    @Override public int hashCode(){ return Objects.hash(driverId); }
}
