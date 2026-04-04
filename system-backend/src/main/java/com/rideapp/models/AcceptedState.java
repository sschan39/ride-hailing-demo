package com.rideapp.models;

import com.rideapp.driver.Driver;

public class AcceptedState implements RideState {
    @Override
    public void accept(Ride ride, Driver driver) { System.out.println("Ride already accepted."); }
    
    @Override
    public void start(Ride ride) {
        System.out.println("Driver has picked up the passenger. Ride is in transit.");
        ride.setState(new InTransitState());
    }

    @Override
    public void complete(Ride ride) { System.out.println("Cannot complete. Ride not started."); }
    @Override
    public void processPayment(Ride ride) { System.out.println("Cannot pay. Ride not completed."); }
}