// File: src/main/java/com/rideapp/App.java
package com.rideapp;

import com.rideapp.auth.AuthService;
import com.rideapp.dispatch.RideDispatcher;
import com.rideapp.models.*;
import com.rideapp.pricing.StandardPricing;
import com.rideapp.map.DummyMapProvider;
import com.rideapp.vehicles.*;

import java.util.Arrays;

public class App {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("===================================================");
        System.out.println("          STARTING RIDE-HAILING SYSTEM             ");
        System.out.println("===================================================\n");

        // ---------------------------------------------------------
        // PHASE 1: SYSTEM INITIALIZATION
        // ---------------------------------------------------------
        DummyMapProvider mapProvider = new DummyMapProvider();
        RideDispatcher dispatcher = RideDispatcher.getInstance(mapProvider);
        AuthService auth = AuthService.getInstance();

        // ---------------------------------------------------------
        // PHASE 2: ACCOUNT REGISTRATION & SETUP
        // ---------------------------------------------------------
        System.out.println("--- [PHASE 2] REGISTRATION & SETUP ---");
        auth.registerUser("PASSENGER", "alice_rider", "pass123");
        auth.registerUser("DRIVER", "bob_drifts", "pass123");
        auth.registerUser("DRIVER", "charlie_suv", "pass123");

        Passenger alice = (Passenger) auth.login("alice_rider", "pass123");
        Driver bob = (Driver) auth.login("bob_drifts", "pass123");
        Driver charlie = (Driver) auth.login("charlie_suv", "pass123");

        // Drivers register their vehicles into their garage
        bob.registerVehicle(new StandardCar("ABC-123", "Toyota Camry"));
        charlie.registerVehicle(new SUV("XYZ-999", "Honda CR-V"));

        // Define some real-world coordinates for our simulation
        Location shibuyaPickup = new Location(35.6580, 139.7016, "Shibuya Station");
        Location shinjukuDriverLoc = new Location(35.6896, 139.7005, "Shinjuku (3.5km away)"); 
        Location yokohamaDriverLoc = new Location(35.4437, 139.6380, "Yokohama (25km away)"); 
        Location tokyoTowerDropoff = new Location(35.6586, 139.7454, "Tokyo Tower");

        // Drivers select a vehicle and go online
        System.out.println("\n--- [PHASE 3] DRIVERS GO ONLINE ---");
        dispatcher.addAvailableDriver(bob, "ABC-123", shinjukuDriverLoc);
        dispatcher.addAvailableDriver(charlie, "XYZ-999", yokohamaDriverLoc);

        // ---------------------------------------------------------
        // PHASE 4: PASSENGER REQUESTS A RIDE
        // ---------------------------------------------------------
        System.out.println("\n--- [PHASE 4] RIDE REQUEST & DISPATCH ---");
        // Alice is willing to take either a Standard car or an SUV
        Ride activeRide = dispatcher.requestRide(
                alice, 
                shibuyaPickup, 
                tokyoTowerDropoff, 
                Arrays.asList("STANDARD", "SUV"), 
                new StandardPricing()
        );

        // ---------------------------------------------------------
        // PHASE 5: RIDE IN TRANSIT
        // ---------------------------------------------------------
        // In a real app, the broadcast would wait for the driver to tap "Accept" on their phone.
        // Since our Dispatcher automatically assigned Bob (he was closest), we start the ride.
        if (activeRide != null) {
            System.out.println("\n--- [PHASE 5] RIDE IN TRANSIT ---");
            activeRide.start(); // Transitions state to InTransit
            
            System.out.println("[SYSTEM] Driving to destination...");
            Thread.sleep(2000); // Simulate the time it takes to drive there

            // ---------------------------------------------------------
            // PHASE 6: ARRIVAL & DUAL CONFIRMATION
            // ---------------------------------------------------------
            System.out.println("\n--- [PHASE 6] ARRIVAL & PAYMENT ---");
            // Alice gets out and confirms on her app
            activeRide.confirmEnd(alice);
            
            // Bob finishes the trip and confirms on his app
            // This triggers the Completion State -> Payment Gateway -> Paid State
            // activeRide.confirmEnd(bob);

            // ---------------------------------------------------------
            // PHASE 7: POST-RIDE VERIFICATION (LEDGER & HISTORY)
            // ---------------------------------------------------------
            System.out.println("\n--- [PHASE 7] LEDGER & HISTORY VERIFICATION ---");
            alice.printHistory();
            System.out.println();
            bob.printHistory();
        } else {
            System.out.println("[SYSTEM] Ride could not be completed. No drivers found.");
        }
        
        System.out.println("\n===================================================");
        System.out.println("                 SYSTEM SHUTDOWN                   ");
        System.out.println("===================================================");
    }
}