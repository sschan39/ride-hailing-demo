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


public class App3 {
    public static void main(String[] args) throws InterruptedException {
        DummyMapProvider mapProvider = new DummyMapProvider();
        RideDispatcher dispatcher = RideDispatcher.getInstance(mapProvider);
        AuthService auth = AuthService.getInstance();

        // Setup users
        auth.registerUser("PASSENGER", "alice", "pass");
        auth.registerUser("DRIVER", "driver_bob", "pass");
        auth.registerUser("DRIVER", "driver_charlie", "pass");
        auth.registerUser("DRIVER", "driver_dave", "pass");

        Passenger alice = (Passenger) auth.login("alice", "pass");
        alice.addPaymentMethod(new CreditCard("Alice Rider", "41112222", "12/28"));

        Driver bob = (Driver) auth.login("driver_bob", "pass");
        Driver charlie = (Driver) auth.login("driver_charlie", "pass");
        Driver dave = (Driver) auth.login("driver_dave", "pass");

        bob.registerVehicle(new StandardCar("ABC-1", "Camry"));
        charlie.registerVehicle(new StandardCar("XYZ-2", "Accord"));
        dave.registerVehicle(new StandardCar("LMN-3", "Civic"));

        // All three drivers go online in Shibuya (all within radius)
        Location shibuya = new Location(35.6580, 139.7016, "Shibuya");
        dispatcher.addAvailableDriver(bob, "ABC-1", shibuya);
        dispatcher.addAvailableDriver(charlie, "XYZ-2", shibuya);
        dispatcher.addAvailableDriver(dave, "LMN-3", shibuya);

        System.out.println("\n--- [PASSENGER REQUESTS RIDE] ---");
        Location tokyoTower = new Location(35.6586, 139.7454, "Tokyo Tower");
        
        // This will now broadcast to Bob, Charlie, and Dave
        Ride activeRide = dispatcher.requestRide(alice, shibuya, tokyoTower, Arrays.asList("STANDARD"), new StandardPricing());

        System.out.println("\n--- [DRIVERS REACT] ---");
        // Alternative Course 3a: Bob rejects it
        bob.rejectRide(activeRide);

        // Typical Course (Steps 3-5): Charlie accepts it
        charlie.tryAcceptRide(activeRide);

        // Alternative Course 3b: Dave tries to accept it slightly after Charlie
        Thread.sleep(500); // Simulate human delay
        dave.tryAcceptRide(activeRide); 
    }
}