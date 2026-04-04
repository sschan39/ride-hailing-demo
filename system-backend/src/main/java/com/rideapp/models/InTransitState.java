package com.rideapp.models;

import com.rideapp.driver.Driver;

public class InTransitState implements RideState {
    @Override
    public void accept(Ride ride, Driver driver) { System.out.println("Ride already in transit."); }
    @Override
    public void start(Ride ride) { System.out.println("Ride already in transit."); }

    @Override
    public void complete(Ride ride) {
        System.out.println("Arrived at destination. Ride completed.");
        ride.setState(new CompletedState());
    }

    @Override
    public void processPayment(Ride ride) { System.out.println("Cannot pay yet. Ride is still in transit."); }
}