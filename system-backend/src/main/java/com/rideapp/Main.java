package com.rideapp;

import com.rideapp.driver.Driver;
import com.rideapp.models.Ride;
import com.rideapp.system.RideDispatcher;
import com.rideapp.system.StandardPricing;
import com.rideapp.system.SurgePricing;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== INITIALIZING RIDE-HAILING SYSTEM ===");
        
        // 1. Setup Dispatcher (Singleton) and Drivers (Observers)
        RideDispatcher dispatcher = RideDispatcher.getInstance();
        Driver driver1 = new Driver("Alice (Car 1)");
        Driver driver2 = new Driver("Bob (Car 2)");
        
        dispatcher.registerDriver(driver1);
        dispatcher.registerDriver(driver2);

        System.out.println("\n=== PASSENGER MODULE: REQUEST A RIDE ===");
        // Passenger requests a ride with Standard Pricing (Strategy)
        Ride myRide = new Ride("Taipei 101", "Main Station", 5.5, new StandardPricing());
        dispatcher.requestRide(myRide);

        System.out.println("\n=== DRIVER MODULE: ACCEPT RIDE ===");
        // Alice accepts the ride. State shifts from Requested -> Accepted
        driver1.acceptRide(myRide);
        
        // If Bob tries to accept, the State pattern prevents it
        driver2.acceptRide(myRide);

        System.out.println("\n=== RIDE LIFECYCLE ===");
        myRide.start();     // State shifts from Accepted -> In Transit
        myRide.complete();  // State shifts from In Transit -> Completed
        
        System.out.println("\n=== PAYMENT MODULE ===");
        myRide.processPayment(); // State shifts from Completed -> Paid
    }
}