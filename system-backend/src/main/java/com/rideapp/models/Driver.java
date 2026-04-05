// File: com/rideapp/models/Driver.java
package com.rideapp.models;

import java.util.ArrayList;
import java.util.List;

import com.rideapp.events.Observer;

public class Driver extends User implements Observer {
    private boolean isAvailable;
    private double accountBalance = 0.0;

    private List<Vehicle> registeredVehicles = new ArrayList<>();
    private Vehicle activeVehicle; // The one currently being driven

    public Driver(String username, String hashedPassword) {
        super(username, hashedPassword);
        this.isAvailable = false; 
    }

    @Override
    public String getRole() { 
        return "DRIVER"; 
    }


    // Toggle status to receive orders
    public void setAvailable(boolean available) { 
        this.isAvailable = available; 
        System.out.println(getUsername() + " status changed to: " + (available ? "Available" : "Offline"));
    }
    
    public boolean isAvailable() { 
        return isAvailable; 
    }

    // Observer Pattern implementation
    @Override
    public void update(Ride ride) {
        if (isAvailable) {
            System.out.println("--> Push Notification to " + getUsername() + ": New ride request from " + ride.getPassenger().getUsername() + "!");
        }
    }

    public void acceptRide(Ride ride) {
        ride.accept(this);
    }

    public void addEarnings(double amount) {
        this.accountBalance += amount;
        System.out.println("[LEDGER] Added $" + amount + " to " + getUsername() + "'s balance. Total: $" + accountBalance);
    }


    public void registerVehicle(Vehicle vehicle) {
        registeredVehicles.add(vehicle);
        System.out.println("[SYSTEM] " + getUsername() + " added a " + vehicle.getVehicleType() + " to their garage.");
    }

    public void setActiveVehicle(Vehicle vehicle) {
        if (!registeredVehicles.contains(vehicle)) {
            throw new IllegalArgumentException("Vehicle not registered to this driver!");
        }
        this.activeVehicle = vehicle;
    }

    public Vehicle getActiveVehicle() { return activeVehicle; }
}