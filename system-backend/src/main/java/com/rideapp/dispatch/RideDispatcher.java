// File: src/main/java/com/rideapp/dispatch/RideDispatcher.java
package com.rideapp.dispatch;

import com.rideapp.map.MapProvider;
import com.rideapp.models.Driver;
import com.rideapp.models.Location;
import com.rideapp.models.Passenger;
import com.rideapp.models.Ride;
import com.rideapp.models.Route;
import com.rideapp.pricing.PricingStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class RideDispatcher {
    private static RideDispatcher instance;
    private MapProvider mapProvider;
    // Master lists for proximity searching
    private List<Driver> onlineDrivers;
    private static final double MAX_SEARCH_RADIUS_KM = 5.0; // 5 km search radius
    private Map<String, Queue<Driver>> driverQueues;
    private List<Ride> pendingRides;

    private RideDispatcher(MapProvider mapProvider) {
        driverQueues = new HashMap<>();
        pendingRides = new LinkedList<>();
        this.onlineDrivers = new ArrayList<>();
        driverQueues.put("STANDARD", new LinkedList<>());
        driverQueues.put("SUV", new LinkedList<>());
        this.mapProvider = mapProvider;
    }

    public static RideDispatcher getInstance(MapProvider mapProvider) {
        if (instance == null) {
            instance = new RideDispatcher(mapProvider);
        }
        return instance;
    }

    public void addAvailableDriver(Driver driver, String licensePlate, Location startingLocation) {
        driver.getRegisteredVehicles().stream()
              .filter(v -> v.getLicensePlate().equals(licensePlate))
              .findFirst()
              .ifPresent(vehicle -> {
                  driver.setActiveVehicle(vehicle);
                  driver.setAvailable(true);
                  driver.updateLocation(startingLocation);
                  onlineDrivers.add(driver);
                  System.out.println("[DISPATCHER] " + driver.getUsername() + " is online at " + startingLocation.getAddress());
              });
    }

public Ride requestRide(Passenger passenger, Location origin, Location destination, 
                            List<String> acceptableTypes, PricingStrategy pricingStrategy) {

        // NEW: Precondition Check
        if (passenger.hasUnpaidBalance()) {
            System.out.println("🚨 [DISPATCHER] Request denied: " + passenger.getUsername() + " has an unpaid balance. Please update your payment method.");
            return null;
        }
        if (!passenger.hasValidPaymentMethod() || passenger.hasUnpaidBalance()) {
            System.out.println("[SYSTEM REJECTED] Account status abnormal. Please check payment method or unpaid balances.");
            return null;
        }

        // 1. ONE clean call to the Map API
        // NEW: Alternative Course 3a (Invalid Route)
        Route tripRoute = mapProvider.getRoute(origin, destination);
        if (tripRoute == null) {
            System.out.println("[SYSTEM REJECTED] Could not calculate route. Please re-enter your pickup or dropoff location.");
            return null;
        }
        
       

        System.out.println("\n[SYSTEM] Ride Requested: " + origin.getAddress() + " -> " + destination.getAddress());
        System.out.println("  -> Route Details: " + tripRoute);

        // 2. Pass the Route object directly into the Ride
        Ride newRide = new Ride(passenger, origin, destination, tripRoute, acceptableTypes, pricingStrategy);
        double estimatedFare = pricingStrategy.calculateFare(tripRoute.getDistanceKm());
        // 3. Geospatial Search
        System.out.println("\n[SYSTEM] Broadcasting ride request to nearby drivers...");

        // 1. Find ALL nearby eligible drivers, not just one
        List<Driver> nearbyDrivers = findNearbyEligibleDrivers(origin, acceptableTypes);

        if (nearbyDrivers.isEmpty()) {
            System.out.println("[DISPATCHER] No drivers available nearby within 5km.");
            return null;
        }
        

        // 2. Broadcast the push notification to all of them
        for (Driver driver : nearbyDrivers) {
            if (driver.getActiveVehicle() == null) {
                continue; // Skip drivers with missing vehicle data
            }       
            driver.receivePushNotification(newRide, estimatedFare);
        }

        return newRide;


    }

    // NEW: Finds a list of drivers instead of just one
    private List<Driver> findNearbyEligibleDrivers(Location pickupLocation, List<String> acceptableTypes) {
        List<Driver> eligible = new ArrayList<>();
        for (Driver driver : onlineDrivers) {
            if (driver.getActiveVehicle() == null) {
                System.out.println("⚠️ [WARN] Skipping Driver " + driver.getUsername() + ": No active vehicle found.");
                continue; // Skip drivers with missing vehicle data
            }
            if (!driver.isAvailable() || !acceptableTypes.contains(driver.getActiveVehicle().getVehicleType())) continue;
            
            double distance = mapProvider.getStraightLineDistance(driver.getCurrentLocation(), pickupLocation);
            if (distance <= MAX_SEARCH_RADIUS_KM) {
                eligible.add(driver);
            }
        }
        return eligible;
    }



    // NEW: Step 4 & Alt 3b - The concurrency lock for accepting a ride
    public synchronized boolean assignRideToDriver(Driver driver, Ride ride) {
        // If the ride already has a driver, someone else beat them to it!
        if (ride.getDriver() != null) {
            System.out.println("⚠️ [SYSTEM] Sorry " + driver.getUsername() + ", this order has already been taken by another driver.");
            return false;
        }

        // Otherwise, assign it and lock it
        ride.setDriver(driver);
        ride.accept(driver);
        System.out.println("✅ [SYSTEM] Ride successfully assigned to " + driver.getUsername() + ". Stopping broadcast.");
        return true;
    }
}