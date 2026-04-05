// File: src/main/java/com/rideapp/system/PaymentGateway.java
package com.rideapp.payment;

import com.rideapp.models.Ride;
import com.rideapp.models.RideRecord;

public class PaymentGateway {
    private static final double PLATFORM_FEE_PERCENTAGE = 0.20; // Platform takes 20%

    public static boolean processPayment(Ride ride) {
        // THE DEFENSIVE CHECK
        if (!ride.isPayable()) {
            System.out.println("[PAYMENT ERROR] Security Alert: Ride " + ride.getId().substring(0,8) + " is NOT in a payable state!");
            return false; 
        }

        double totalFare = ride.getPricingStrategy().calculateFare(ride.getDistance());
        
        System.out.println("\n[PAYMENT GATEWAY] Processing payment for passenger: " + ride.getPassenger().getUsername());
        System.out.println("  -> Charging card... Success! Total Fare: $" + totalFare);

        double platformCut = totalFare * PLATFORM_FEE_PERCENTAGE;
        double driverEarnings = totalFare - platformCut;
        
        ride.getDriver().addEarnings(driverEarnings);

        RideRecord record = new RideRecord(
            ride.getId(), 
            ride.getPassenger().getUsername(), 
            ride.getDriver().getUsername(), 
            totalFare, 
            ride.getEndTime()
        );

        ride.getPassenger().addRideRecord(record);
        ride.getDriver().addRideRecord(record);
        
        return true; // Payment successful
    }
}