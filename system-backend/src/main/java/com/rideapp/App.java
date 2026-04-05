// File: src/main/java/com/rideapp/App.java
package com.rideapp;

import com.rideapp.models.*;
import com.rideapp.auth.AuthService;
import com.rideapp.dispatch.RideDispatcher;
import com.rideapp.pricing.StandardPricing;

public class App {
    public static void main(String[] args) throws InterruptedException {
        AuthService auth = AuthService.getInstance();
        RideDispatcher dispatcher = RideDispatcher.getInstance();

        // 1. Setup & Login
        auth.registerUser("PASSENGER", "alice_rider", "pass123");
        auth.registerUser("DRIVER", "bob_driver", "pass123");
        Passenger alice = (Passenger) auth.login("alice_rider", "pass123");
        Driver bob = (Driver) auth.login("bob_driver", "pass123");

        // 2. Queue up
        dispatcher.addAvailableDriver(bob);
        
        // 3. Request Ride (Dispatcher creates and returns the Ride object for demo purposes)
        Ride activeRide = dispatcher.requestRide(alice, "Taipei 101", "Main Station", 10.0, new StandardPricing());
        
        System.out.println("\n=== RIDE IN PROGRESS ===");
        activeRide.start(); // Set to InTransit, starts clock
        
        // Simulating driving time
        Thread.sleep(1500); 

        System.out.println("\n=== ARRIVED AT DESTINATION ===");
        // Only one confirms (Nothing happens yet)
        activeRide.confirmEnd(alice);
        
        // The second confirms (Triggers completion, end time, and Payment Gateway)
        activeRide.confirmEnd(bob);

        System.out.println("\n=== POST-RIDE VERIFICATION ===");
        // Verify the immutable records were stored successfully
        alice.printHistory();
        bob.printHistory();
    }
}