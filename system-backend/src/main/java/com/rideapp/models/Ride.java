// File: src/main/java/com/rideapp/models/Ride.java
package com.rideapp.models;

import com.rideapp.payment.PaymentGateway;
import com.rideapp.pricing.PricingStrategy;
import com.rideapp.state.RequestedState;
import com.rideapp.state.RideState;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Ride {
    private final String id; // Unique ID
    private Passenger passenger;
    private Location origin;
    private Location destination;
    private double distance;
    private RideState state;
    private PricingStrategy pricingStrategy;
    private Driver driver;
    private List<String> acceptableVehicleTypes;
    private int estimatedTimeMinutes;
    
    // New time tracking fields
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean passengerConfirmedEnd = false;
    private boolean driverConfirmedEnd = false;

    // Notice the constructor no longer needs the ID, it generates it automatically
    public Ride(Passenger passenger, Location origin, Location destination, double distance, 
                List<String> acceptableVehicleTypes, PricingStrategy pricingStrategy) {
        this.id = UUID.randomUUID().toString(); 
        this.passenger = passenger;
        this.origin = origin;
        this.destination = destination;
        this.distance = distance;
        this.pricingStrategy = pricingStrategy;
        this.state = new RequestedState();
        this.acceptableVehicleTypes = acceptableVehicleTypes;

    }


    public void confirmEnd(User user) {
        if (user.getRole().equals("PASSENGER")) {
            passengerConfirmedEnd = true;
            System.out.println("[RIDE] Passenger " + user.getUsername() + " confirmed arrival.");
        } else if (user.getRole().equals("DRIVER")) {
            driverConfirmedEnd = true;
            System.out.println("[RIDE] Driver " + user.getUsername() + " confirmed arrival.");
        }

        // If both have confirmed, the Ride triggers its own state transitions
        if (passengerConfirmedEnd && driverConfirmedEnd) {
            this.complete();         // State Pattern shifts to Completed
            this.processPayment();   // State Pattern handles the gateway and shifts to Paid
        }
    }


    // New Getters and Setters
    public RideState getState() {return state;}
    public String getId() { return id; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    // Existing Getters/Setters and State delegation
    public Passenger getPassenger() { return passenger; }

    public double getDistance() { return distance; }

    public PricingStrategy getPricingStrategy() { return pricingStrategy; }

    public void setDriver(Driver driver) { this.driver = driver; }
    public Driver getDriver() {return driver;}

    public List<String> getAcceptableVehicleTypes() { 
        return acceptableVehicleTypes; 
    }

    public void setState(RideState state) { this.state = state; }

    public void accept(Driver driver) { state.accept(this, driver); }
    public void start() { state.start(this); }
    public void complete() { state.complete(this); }
    public void processPayment() { state.processPayment(this); }

    public boolean isPayable() {
        return state.isPayable();
    }


}