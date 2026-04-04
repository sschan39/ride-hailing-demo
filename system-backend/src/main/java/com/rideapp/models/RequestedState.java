package com.rideapp.models;

import com.rideapp.driver.Driver;

public class RequestedState implements RideState {
    @Override
    public void accept(Ride ride, Driver driver) {
        ride.setDriver(driver);
        System.out.println("Ride accepted by driver: " + driver.getName());
        ride.setState(new AcceptedState());
    }

    @Override
    public void start(Ride ride) { System.out.println("Cannot start. Ride not accepted yet."); }
    @Override
    public void complete(Ride ride) { System.out.println("Cannot complete. Ride not started."); }
    @Override
    public void processPayment(Ride ride) { System.out.println("Cannot pay. Ride not completed."); }
}