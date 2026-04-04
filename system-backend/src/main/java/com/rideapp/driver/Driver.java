package com.rideapp.driver;

import com.rideapp.models.Ride;

public class Driver implements Observer {
    private String name;

    public Driver(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    @Override
    public void update(Ride ride) {
        System.out.println("--> Push Notification to " + name + ": New ride request from " + ride.getOrigin() + " to " + ride.getDestination());
    }

    // Driver action
    public void acceptRide(Ride ride) {
        ride.accept(this);
    }
}