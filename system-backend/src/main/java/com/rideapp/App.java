// File: src/main/java/com/rideapp/App.java
package com.rideapp;

import com.rideapp.dispatch.RideDispatcher;
import com.rideapp.models.*;
import com.rideapp.pricing.StandardPricing;
import com.rideapp.vehicles.*;

import java.util.Arrays;

public class App {
    public static void main(String[] args) {
        RideDispatcher dispatcher = RideDispatcher.getInstance();

        // Setup users directly for demo
        Passenger alice = new Passenger("alice", "pass");
        Driver bob = new Driver("bob", "pass");

        // Bob adds two cars to his garage
        System.out.println("=== 1. BOB'S GARAGE ===");
        bob.registerVehicle(new StandardCar("ABC-123", "Toyota Camry"));
        bob.registerVehicle(new SUV("XYZ-999", "Honda CR-V"));

        System.out.println("\n=== 2. ALICE REQUESTS A RIDE ===");
        // Alice is in a hurry, she will accept a STANDARD or an SUV
        dispatcher.requestRide(alice, "Home", "Office", 5.0, 
                               Arrays.asList("STANDARD", "SUV"), 
                               new StandardPricing());

        System.out.println("\n=== 3. BOB GOES ONLINE ===");
        // Bob decides to drive his SUV today. 
        // The dispatcher will instantly realize Alice is okay with an SUV and match them!
        dispatcher.addAvailableDriver(bob, "XYZ-999"); 
    }
}