// File: User.java
package com.rideapp.models;

import java.util.ArrayList;
import java.util.List;

public abstract class User {
    private String username;
    private String hashedPassword;

    protected List<RideRecord> rideHistory = new ArrayList<>();

    public User(String username, String hashedPassword) {
        this.username = username;
        this.hashedPassword = hashedPassword;
    }

    public String getUsername() { return username; }
    public String getHashedPassword() { return hashedPassword; }
    
    public abstract String getRole();

    public void addRideRecord(RideRecord record) {
        rideHistory.add(record);
    }
    
    public void printHistory() {
        System.out.println("--- History for " + getUsername() + " ---");
        for (RideRecord record : rideHistory) {
            System.out.println(record);
        }
    }

}