package com.rideapp.models;

import com.rideapp.system.PricingStrategy;
import com.rideapp.driver.Driver;

public class Ride {
    private String origin;
    private String destination;
    private double distance;
    private RideState state;
    private PricingStrategy pricingStrategy;
    private Driver driver;

    public Ride(String origin, String destination, double distance, PricingStrategy pricingStrategy) {
        this.origin = origin;
        this.destination = destination;
        this.distance = distance;
        this.pricingStrategy = pricingStrategy;
        this.state = new RequestedState(); // Initial State
    }

    // State delegation methods
    public void accept(Driver driver) { state.accept(this, driver); }
    public void start() { state.start(this); }
    public void complete() { state.complete(this); }
    public void processPayment() { state.processPayment(this); }

    // Getters and Setters
    public void setState(RideState state) { this.state = state; }
    public void setDriver(Driver driver) { this.driver = driver; }
    public PricingStrategy getPricingStrategy() { return pricingStrategy; }
    public double getDistance() { return distance; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
}