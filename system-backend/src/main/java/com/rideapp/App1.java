// File: src/main/java/com/rideapp/App.java
package com.rideapp;

import com.rideapp.auth.AuthService;
import com.rideapp.dispatch.RideDispatcher;
import com.rideapp.models.*;
import com.rideapp.payment.CreditCard;
import com.rideapp.pricing.StandardPricing;
import com.rideapp.pricing.SurgePricing;
import com.rideapp.vehicles.*;
import com.rideapp.map.DummyMapProvider;

import java.util.Arrays;

public class App1 {
    public static void main(String[] args) throws InterruptedException {
        DummyMapProvider mapProvider = new DummyMapProvider();
        RideDispatcher dispatcher = RideDispatcher.getInstance(mapProvider);
        AuthService auth = AuthService.getInstance();

        auth.registerUser("PASSENGER", "alice", "pass");
        auth.registerUser("DRIVER", "charlie_suv", "pass");
        Passenger alice = (Passenger) auth.login("alice", "pass");
        Driver charlie = (Driver) auth.login("charlie_suv", "pass");

        charlie.registerVehicle(new SUV("XYZ-999", "Honda CR-V"));

        // Locations
        Location shibuyaPickup = new Location(35.6580, 139.7016, "Shibuya Station");
        Location invalidDropoff = new Location(0, 0, "Invalid");
        Location tokyoTowerDropoff = new Location(35.6586, 139.7454, "Tokyo Tower");
        Location farAwayLocation = new Location(35.4437, 139.6380, "Yokohama (25km away)"); 

        System.out.println("\n--- [TEST] PRECONDITION VALIDATION ---");
        System.out.println("\n--- [TEST] PRECONDITION VALIDATION ---");
        
        // 1. Try to request WITHOUT a payment method (Should Fail)
        dispatcher.requestRide(alice, shibuyaPickup, tokyoTowerDropoff, Arrays.asList("STANDARD"), new StandardPricing());
        
        // 2. Add a valid credit card
        alice.addPaymentMethod(new CreditCard("Alice Rider", "4111222233334444", "12/28"));
        
        // 3. Try to request WITH an unpaid balance (Should Fail)
        alice.addUnpaidBalance(15.50); 
        dispatcher.requestRide(alice, shibuyaPickup, tokyoTowerDropoff, Arrays.asList("STANDARD"), new StandardPricing());
        
        // 4. Clear balance (Now she is fully eligible to ride)
        alice.clearUnpaidBalance();

        System.out.println("\n--- [TEST] ALTERNATIVE COURSE 3a (INVALID ROUTE) ---");
        dispatcher.requestRide(alice, shibuyaPickup, invalidDropoff, Arrays.asList("STANDARD"), new StandardPricing());

        System.out.println("\n--- [TEST] ALTERNATIVE COURSE 5a (TIMEOUT & SURGE PRICING) ---");
        // Charlie goes online, but he is in Yokohama (too far from Shibuya)
        dispatcher.addAvailableDriver(charlie, "XYZ-999", farAwayLocation);

        // 1. Initial Standard Request
        Ride activeRide = dispatcher.requestRide(alice, shibuyaPickup, tokyoTowerDropoff, Arrays.asList("SUV"), new StandardPricing());

        if (activeRide == null) {
            System.out.println("[APP UI] 3 minutes have passed. No vehicles available.");
            System.out.println("[APP UI] Prompt: 'Would you like to try again with Surge Pricing?' -> Passenger clicks YES.");
            
            // For the sake of the simulation succeeding, Charlie magically drives closer to Shinjuku
            System.out.println("[SYSTEM] (Simulation) Charlie drives into the 5km radius...");
            charlie.updateLocation(new Location(35.6896, 139.7005, "Shinjuku")); 

            // 2. Retry with Surge Pricing
            activeRide = dispatcher.requestRide(alice, shibuyaPickup, tokyoTowerDropoff, Arrays.asList("SUV"), new SurgePricing());
        }

        if (activeRide != null) {
            activeRide.start();
            activeRide.confirmEnd(alice);
            activeRide.confirmEnd(charlie);
            System.out.println("\n[FINANCE LEDGER] Passenger final bill: $" + String.format("%.2f", activeRide.getRoute().getDistanceKm() * 2.5)); // Assuming surge multiplier logic
        }
    }
}