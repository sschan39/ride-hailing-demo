package com.rideapp.models;

import com.rideapp.driver.Driver;

public class PaidState implements RideState {
    @Override
    public void accept(Ride ride, Driver driver) { System.out.println("Ride is closed and paid."); }
    @Override
    public void start(Ride ride) { System.out.println("Ride is closed and paid."); }
    @Override
    public void complete(Ride ride) { System.out.println("Ride is closed and paid."); }
    @Override
    public void processPayment(Ride ride) { System.out.println("Ride is already paid."); }
}