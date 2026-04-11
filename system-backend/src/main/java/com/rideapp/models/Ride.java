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
    private RideState state;
    private PricingStrategy pricingStrategy;
    private Driver driver;
    private List<String> acceptableVehicleTypes;
    private Route route;
    
    // Time tracking fields
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // NEW: Flexible confirmation and deviation tracking
    private boolean passengerConfirmedEnd = false;
    private boolean driverConfirmedEnd = false;
    private boolean requiresDualConfirmation = false; // Feature flag: defaults to driver-only
    private double actualDistanceKm;
    private boolean requiresManualReview = false;

    public Ride(Passenger passenger, Location origin, Location destination, Route route, 
                List<String> acceptableVehicleTypes, PricingStrategy pricingStrategy) {
        this.id = UUID.randomUUID().toString();
        this.passenger = passenger;
        this.origin = origin;
        this.destination = destination;
        this.route = route;
        this.acceptableVehicleTypes = acceptableVehicleTypes;
        this.pricingStrategy = pricingStrategy;
        this.state = new RequestedState();
    }

    // Driver confirmation (includes odometer reading for Alt Course 2a)
    public void confirmEnd(Driver driver, double actualDistanceKm) {
        if (this.driver != driver) return;
        this.driverConfirmedEnd = true;
        this.actualDistanceKm = actualDistanceKm;
        System.out.println("🏁 [ACTION] Driver " + driver.getUsername() + " confirmed ride end.");
        evaluateCompletion();
    }

    // Passenger confirmation (kept for flexible dual-confirmation support)
    public void confirmEnd(Passenger passenger) {
        if (this.passenger != passenger) return;
        this.passengerConfirmedEnd = true;
        System.out.println("🏁 [ACTION] Passenger " + passenger.getUsername() + " confirmed ride end.");
        evaluateCompletion();
    }

    // Centralized check to see if we transition out of active state
    private void evaluateCompletion() {
        if (requiresDualConfirmation && (!driverConfirmedEnd || !passengerConfirmedEnd)) {
            System.out.println("⏳ [SYSTEM] Waiting for both parties to confirm...");
            return;
        }
        if (!requiresDualConfirmation && !driverConfirmedEnd) {
            return; // Even in single-mode, the driver MUST confirm
        }

        System.out.println("🔄 [SYSTEM] Confirmation criteria met. Processing ride end...");
        finalizeRide();
    }

    private void finalizeRide() {
        // Alternative Course 2a - Route Deviation Check (>50% difference)
        double estimatedDistance = route.getDistanceKm();
        if (actualDistanceKm > (estimatedDistance * 1.5)) {
            System.out.println("🚨 [SYSTEM ALERT] Route severely deviated! Est: " + estimatedDistance + "km, Actual: " + actualDistanceKm + "km.");
            this.requiresManualReview = true;
            System.out.println("⏸️ [SYSTEM] Payment paused. Flagged for manual review by customer service.");
            return;
        }

        // If everything is normal, trigger State Pattern transitions
        this.complete();         // Shifts to Completed state
        this.processPayment();   // Shifts to Paid state (delegates to PaymentGateway internally)
        
        // Postcondition - Unlock Driver
        if (this.driver != null) {
            this.driver.setAvailable(true);
        }
    }

    // --- Getters and Setters ---
    public RideState getState() { return state; }
    public String getId() { return id; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Passenger getPassenger() { return passenger; }
    public Route getRoute() { return route; }
    
    // Updated to use actual distance if recorded, falling back to estimated route distance
    public double getDistance() { 
        return actualDistanceKm > 0 ? actualDistanceKm : route.getDistanceKm(); 
    }
    
    public double getActualDistanceKm() { return actualDistanceKm; }
    public PricingStrategy getPricingStrategy() { return pricingStrategy; }

    public void setDriver(Driver driver) { this.driver = driver; }
    public Driver getDriver() { return driver; }

    public List<String> getAcceptableVehicleTypes() { return acceptableVehicleTypes; }

    public void setState(RideState state) { this.state = state; }

    // State delegation
    public void accept(Driver driver) { state.accept(this, driver); }
    public void start() { state.start(this); }
    public void complete() { state.complete(this); }
    public void processPayment() { state.processPayment(this); }

    public boolean isPayable() {
        // Defensive check: A flagged ride is never payable, regardless of State Pattern status
        return !requiresManualReview && state.isPayable();
    }

    public Location getOrigin() { return origin; }
    public Location getDestination() { return destination; }
    
    public boolean requiresManualReview() { return requiresManualReview; }
}