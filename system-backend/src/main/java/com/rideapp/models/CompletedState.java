package com.rideapp.models;

import com.rideapp.driver.Driver;

public class CompletedState implements RideState {
    @Override
    public void accept(Ride ride, Driver driver) { System.out.println("Ride is already completed."); }
    @Override
    public void start(Ride ride) { System.out.println("Ride is already completed."); }
    @Override
    public void complete(Ride ride) { System.out.println("Ride is already completed."); }

    @Override
    public void processPayment(Ride ride) {
        double fare = ride.getPricingStrategy().calculateFare(ride.getDistance());
        System.out.println("Payment processed successfully. Total fare: $" + fare);
        ride.setState(new PaidState());
    }
}