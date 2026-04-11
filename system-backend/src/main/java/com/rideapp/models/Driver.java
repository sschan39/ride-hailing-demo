// File: com/rideapp/models/Driver.java
package com.rideapp.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.rideapp.dispatch.RideDispatcher;
import com.rideapp.events.Observer;

public class Driver extends User implements Observer {
    private boolean isAvailable;
    private double accountBalance = 0.0;
    private Location currentLocation;

    private List<Vehicle> registeredVehicles = new ArrayList<>();
    private Vehicle activeVehicle; // The one currently being driven

    public Driver(String username, String hashedPassword) {
        super(username, hashedPassword);
        this.isAvailable = false; 
    }

    // NEW: Step 2 - Receive push notification with required details
    public void receivePushNotification(Ride ride, double estimatedFare) {
        System.out.println("📱 [APP - " + getUsername() + "] New Ride Request!");
        System.out.println("    📍 Pickup: " + ride.getOrigin().getAddress());
        System.out.println("    📏 Distance: " + String.format("%.2f", ride.getDistance()) + " km");
        System.out.println("    💰 Est. Fare: $" + String.format("%.2f", estimatedFare));
    }
    // NEW: Step 3 & 5 - Driver actively chooses to accept
    public void tryAcceptRide(Ride ride) {
        System.out.println("👉 [ACTION] " + getUsername() + " clicked 'Accept'...");
        
        boolean success = RideDispatcher.getInstance(null).assignRideToDriver(this, ride);
        
        if (success) {
            this.setAvailable(false);
            // Step 5 - Start Navigation
            System.out.println("🗺️ [NAVIGATION] Routing " + getUsername() + " to " + ride.getOrigin().getAddress() + "...\n");
        }
    }

    // NEW: Alternative Course 3a - Driver actively rejects
    public void rejectRide(Ride ride) {
        System.out.println("❌ [ACTION] " + getUsername() + " clicked 'Reject'.");
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

    public List<Vehicle> getRegisteredVehicles() {
        return registeredVehicles;
    }
    
    public void updateLocation(Location location) {
        this.currentLocation = location;
        // In a real app, this would ping the dispatcher every few seconds
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }
}