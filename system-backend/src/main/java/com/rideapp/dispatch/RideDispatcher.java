// File: src/main/java/com/rideapp/dispatch/RideDispatcher.java
package com.rideapp.dispatch;

import com.rideapp.map.MapProvider;
import com.rideapp.models.Driver;
import com.rideapp.models.Location;
import com.rideapp.models.Passenger;
import com.rideapp.models.Ride;
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
        
        // 1. Calculate trip metrics using the MapProvider
        double tripDistance = mapProvider.calculateDistance(origin, destination);
        int tripEta = mapProvider.calculateETA(tripDistance);

        System.out.println("\n[SYSTEM] Ride Requested: " + origin.getAddress() + " -> " + destination.getAddress());
        System.out.printf("  -> Estimated Distance: %.2f km | ETA: %d mins%n", tripDistance, tripEta);

        Ride newRide = new Ride(passenger, origin, destination, tripDistance, tripEta, acceptableTypes, pricingStrategy);

        // 2. Geospatial Search: Find the closest driver
        Driver closestDriver = findClosestEligibleDriver(origin, acceptableTypes);

        if (closestDriver != null) {
            System.out.println("[DISPATCHER] MATCHED! Closest driver is " + closestDriver.getUsername() + " in a " + closestDriver.getActiveVehicle().getVehicleType());
            
            closestDriver.setAvailable(false); // Take them off the market
            closestDriver.update(newRide);
            closestDriver.acceptRide(newRide);
        } else {
            System.out.println("[DISPATCHER] No drivers available nearby. Adding to waiting list...");
            // In a full implementation, you'd add this ride to a pending list and retry periodically.
        }

        return newRide;
    }

    private Driver findClosestEligibleDriver(Location pickupLocation, List<String> acceptableTypes) {
        Driver closest = null;
        double minDistance = Double.MAX_VALUE;

        for (Driver driver : onlineDrivers) {
            if (!driver.isAvailable()) continue; // Skip busy/offline drivers
            
            // Check if vehicle type is acceptable
            if (!acceptableTypes.contains(driver.getActiveVehicle().getVehicleType())) continue;

            // Calculate driver's distance to the pickup location
            double distanceToPickup = mapProvider.calculateDistance(driver.getCurrentLocation(), pickupLocation);

            // Check if within radius and is the closest so far
            if (distanceToPickup <= MAX_SEARCH_RADIUS_KM && distanceToPickup < minDistance) {
                minDistance = distanceToPickup;
                closest = driver;
            }
        }
        if (closest != null) {
            System.out.printf("  -> Driver %s is %.2f km away from pickup.%n", closest.getUsername(), minDistance);
        }
        return closest;
    }


    // The advanced non-blocking matchmaker
    private void attemptMatch() {
        Iterator<Ride> rideIterator = pendingRides.iterator();
        
        while (rideIterator.hasNext()) {
            Ride ride = rideIterator.next();
            
            // Check if any of the passenger's acceptable types have an available driver
            for (String allowedType : ride.getAcceptableVehicleTypes()) {
                Queue<Driver> availableDriversForType = driverQueues.get(allowedType);
                
                if (availableDriversForType != null && !availableDriversForType.isEmpty()) {
                    // Match found!
                    Driver matchedDriver = availableDriversForType.poll();
                    System.out.println("[DISPATCHER] MATCHED! " + ride.getPassenger().getUsername() + " gets " + matchedDriver.getUsername() + "'s " + allowedType);
                    
                    matchedDriver.update(ride);
                    matchedDriver.acceptRide(ride);
                    
                    // Remove the ride from the pending list so it doesn't get matched again
                    rideIterator.remove(); 
                    break; // Stop looking for vehicles for THIS ride, move to next ride
                }
            }
        }
    }
}