// File: Passenger.java
package com.rideapp.models;

public class Passenger extends User {
    public Passenger(String username, String hashedPassword) {
        super(username, hashedPassword);
    }

    @Override
    public String getRole() { return "PASSENGER"; }
}