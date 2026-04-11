// File: src/main/java/com/rideapp/App.java
package com.rideapp;

import com.rideapp.auth.AuthService;
import com.rideapp.dispatch.RideDispatcher;
import com.rideapp.models.*;
import com.rideapp.payment.CreditCard;
import com.rideapp.pricing.StandardPricing;
import com.rideapp.vehicles.*;
import com.rideapp.map.DummyMapProvider;

import java.util.Arrays;


public class App4 {
    public static void main(String[] args) throws InterruptedException {
        // --- 1. SYSTEM SETUP ---
        DummyMapProvider mapProvider = new DummyMapProvider();
        RideDispatcher dispatcher = RideDispatcher.getInstance(mapProvider);
        AuthService auth = AuthService.getInstance();

        // Register Users
        auth.registerUser("PASSENGER", "alice_good", "pass");
        auth.registerUser("PASSENGER", "bob_broke", "pass");
        auth.registerUser("DRIVER", "driver_charlie", "pass");

        // Login & Setup Details
        Passenger alice = (Passenger) auth.login("alice_good", "pass");
        Passenger bob = (Passenger) auth.login("bob_broke", "pass");
        Driver charlie = (Driver) auth.login("driver_charlie", "pass");

        // Set up payments: Alice has a good card, Bob has the QA "9999" trigger card
        alice.addPaymentMethod(new CreditCard("Alice", "4111222233334444", "12/28"));
        bob.addPaymentMethod(new CreditCard("Bob", "1111222233339999", "01/25")); // 9999 triggers decline

        charlie.registerVehicle(new StandardCar("XYZ-1", "Civic"));
        
        Location shibuya = new Location(35.6580, 139.7016, "Shibuya");
        Location tokyoTower = new Location(35.6586, 139.7454, "Tokyo Tower"); // ~4.0km away


        // =====================================================================
        System.out.println("\n✅ SCENARIO 1: THE HAPPY PATH (Normal Ride)");
        // =====================================================================
        dispatcher.addAvailableDriver(charlie, "XYZ-1", shibuya);
        Ride ride1 = dispatcher.requestRide(alice, shibuya, tokyoTower, Arrays.asList("STANDARD"), new StandardPricing());
        charlie.tryAcceptRide(ride1);
        ride1.start();
        
        // Charlie ends the ride and reports 4.1km (Normal, close to estimated 4.0km)
        ride1.confirmEnd(charlie, 4.1);
        
        System.out.println("-> POST-CHECK: Charlie is available? " + charlie.isAvailable());


        // =====================================================================
        System.out.println("\n🚨 SCENARIO 2: EDGE CASE - ROUTE DEVIATION (Alt Course 2a)");
        // =====================================================================
        // Charlie is available again, so Alice requests another ride back
        Ride ride2 = dispatcher.requestRide(alice, tokyoTower, shibuya, Arrays.asList("STANDARD"), new StandardPricing());
        charlie.tryAcceptRide(ride2);
        ride2.start();

        // Charlie gets lost and drives 12.5km (Way over the 1.5x threshold of ~6.0km)
        ride2.confirmEnd(charlie, 12.5);

        System.out.println("-> POST-CHECK: Ride flagged for review? " + ride2.requiresManualReview());
        System.out.println("-> POST-CHECK: Is ride payable? " + ride2.isPayable());
        
        // Manual override to free Charlie up for the next test since he is locked
        charlie.setAvailable(true); 


        // =====================================================================
        System.out.println("\n💳 SCENARIO 3: EDGE CASE - PAYMENT DECLINED (Alt Course 4a)");
        // =====================================================================
        // Bob (who has the bad card ending in 9999) requests a ride
        Ride ride3 = dispatcher.requestRide(bob, shibuya, tokyoTower, Arrays.asList("STANDARD"), new StandardPricing());
        charlie.tryAcceptRide(ride3);
        ride3.start();

        // Charlie drives normally this time
        ride3.confirmEnd(charlie, 4.0);

        System.out.println("-> POST-CHECK: Bob has unpaid balance? " + bob.hasUnpaidBalance());

        // =====================================================================
        System.out.println("\n🛑 SCENARIO 4: EDGE CASE - PASSENGER LOCKED OUT");
        // =====================================================================
        // Bob tries to request a ride while having an unpaid balance from Scenario 3
        System.out.println("[TEST] Bob attempts to request another ride...");
        Ride ride4 = dispatcher.requestRide(bob, tokyoTower, shibuya, Arrays.asList("STANDARD"), new StandardPricing());
        
        if (ride4 == null) {
            System.out.println("-> POST-CHECK: System correctly blocked Bob from requesting a ride.");
        } else {
            System.out.println("-> POST-CHECK: [FAIL] System allowed Bob to ride!");
        }
    }
}