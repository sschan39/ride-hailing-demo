// File: src/main/java/com/rideapp/dispatch/RideDispatcher.java
package com.rideapp.dispatch;

import com.rideapp.models.Driver;
import com.rideapp.models.Passenger;
import com.rideapp.models.Ride;
import com.rideapp.pricing.PricingStrategy;
import java.util.LinkedList;
import java.util.Queue;

public class RideDispatcher {
    private static RideDispatcher instance;
    
    // Proper FIFO queues for matching
    private Queue<Driver> availableDriversQueue;
    private Queue<Ride> pendingRidesQueue;

    private RideDispatcher() {
        availableDriversQueue = new LinkedList<>();
        pendingRidesQueue = new LinkedList<>();
    }

    public static RideDispatcher getInstance() {
        if (instance == null) {
            instance = new RideDispatcher();
        }
        return instance;
    }

    // Driver signals they are ready for work
    public void addAvailableDriver(Driver driver) {
        driver.setAvailable(true);
        availableDriversQueue.offer(driver);
        System.out.println("[DISPATCHER] Driver " + driver.getUsername() + " joined the queue. Total available: " + availableDriversQueue.size());
        
        attemptMatch(); // Check if any rides are waiting for this driver
    }

    // Passenger requests a ride THROUGH the dispatcher
    public Ride requestRide(Passenger passenger, String origin, String destination, double distance, PricingStrategy pricingStrategy) {
        if (passenger == null) {
            System.out.println("[SYSTEM ERROR] Unauthorized. Must be logged in.");
            return null;
        }

        // The Dispatcher is the Creator
        Ride newRide = new Ride(passenger, origin, destination, distance, pricingStrategy);
        pendingRidesQueue.offer(newRide);
        
        System.out.println("[DISPATCHER] Ride Request " + newRide.getId().substring(0,8) + "... added to queue. Total pending rides: " + pendingRidesQueue.size());
        
        attemptMatch(); // Check if any drivers are available for this ride

        return newRide;
    }


    // The core matchmaking brain
    private void attemptMatch() {
        // While we have BOTH a waiting ride AND an available driver
        while (!pendingRidesQueue.isEmpty() && !availableDriversQueue.isEmpty()) {
            Ride ride = pendingRidesQueue.poll();     // Pull first ride from queue
            Driver driver = availableDriversQueue.poll(); // Pull first driver from queue
            
            System.out.println("[DISPATCHER] Matched Ride " + ride.getId().substring(0,8) + " with Driver " + driver.getUsername());
            
            // Notify the driver and auto-accept for the demo
            driver.update(ride);
            driver.acceptRide(ride);
        }
    }
}