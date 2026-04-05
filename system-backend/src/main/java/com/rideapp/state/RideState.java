package com.rideapp.state;

import com.rideapp.models.Driver;
import com.rideapp.models.Ride;

public interface RideState {
    void accept(Ride ride, Driver driver);
    void start(Ride ride);
    void complete(Ride ride);
    void processPayment(Ride ride);

    boolean isPayable();

    
}