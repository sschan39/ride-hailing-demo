// File: src/main/java/com/rideapp/models/RideRecord.java
package com.rideapp.models;

import java.time.LocalDateTime;

public class RideRecord {
    private final String rideId;
    private final String passengerUsername;
    private final String driverUsername;
    private final double fare;
    private final LocalDateTime endTime;

    public RideRecord(String rideId, String passengerUsername, String driverUsername, double fare, LocalDateTime endTime) {
        this.rideId = rideId;
        this.passengerUsername = passengerUsername;
        this.driverUsername = driverUsername;
        this.fare = fare;
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return String.format("Ride [%s] | %s -> %s | Fare: $%.2f | Ended: %s", 
                rideId.substring(0,8), passengerUsername, driverUsername, fare, endTime);
    }
}