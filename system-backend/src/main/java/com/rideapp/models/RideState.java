package com.rideapp.models;

import com.rideapp.driver.Driver;

public interface RideState {
    void accept(Ride ride, Driver driver);
    void start(Ride ride);
    void complete(Ride ride);
    void processPayment(Ride ride);
}