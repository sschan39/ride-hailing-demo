// File: UserFactory.java
package com.rideapp.auth;

import com.rideapp.models.Driver;
import com.rideapp.models.Passenger;
import com.rideapp.models.User;

public class UserFactory {
    public static User createUser(String role, String username, String hashedPassword) {
        if (role.equalsIgnoreCase("PASSENGER")) {
            return new Passenger(username, hashedPassword);
        } else if (role.equalsIgnoreCase("DRIVER")) {
            return new Driver(username, hashedPassword);
        }
        throw new IllegalArgumentException("Unknown user role: " + role);
    }
}