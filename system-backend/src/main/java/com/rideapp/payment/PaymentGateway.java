// File: src/main/java/com/rideapp/system/PaymentGateway.java
package com.rideapp.payment;

import com.rideapp.models.Ride;
import com.rideapp.models.RideRecord;

public class PaymentGateway {
    private static final double PLATFORM_FEE_PERCENTAGE = 0.20; // Platform takes 20%

public static boolean processPayment(Ride ride) {
        // 1. THE DEFENSIVE CHECK
        if (!ride.isPayable()) {
            System.out.println("[PAYMENT ERROR] Security Alert: Ride " + ride.getId().substring(0,8) + " is NOT in a payable state!");
            return false; 
        }

        double totalFare = ride.getPricingStrategy().calculateFare(ride.getDistance());
        
        System.out.println("\n[PAYMENT GATEWAY] Processing payment for passenger: " + ride.getPassenger().getUsername());

        // 2. THE BANK API CHECK
        PaymentMethod method = ride.getPassenger().getDefaultPaymentMethod();
        boolean bankApproved = false;

        if (method instanceof CreditCard) {
            CreditCard card = (CreditCard) method;
            BankAPI bankApi = new StubBankAPI(); // Dynamically route to the Bank API stub
            bankApproved = bankApi.chargeCard(card.getCardNumber(), totalFare);
        } else {
            System.out.println("❌ [PAYMENT GATEWAY] Error: Unsupported payment method.");
        }

        // 3. HANDLE FAILURE (Alternative Course 4a)
        if (!bankApproved) {
            System.out.println("⚠️ [PAYMENT DECLINED] Freezing account. Unpaid balance: $" + String.format("%.2f", totalFare));
            ride.getPassenger().addUnpaidBalance(totalFare); 
            return false; // Stop processing and return failure
        }

        // 4. HANDLE SUCCESS & LEDGER MATH
        System.out.println("  -> Charging card... Success! Total Fare: $" + String.format("%.2f", totalFare));

        double platformCut = totalFare * PLATFORM_FEE_PERCENTAGE;
        double driverEarnings = totalFare - platformCut;
        
        ride.getDriver().addEarnings(driverEarnings);
        // 5. IMMUTABLE LEDGER LOGGING
        RideRecord record = new RideRecord(
            ride.getId(), 
            ride.getPassenger().getUsername(), 
            ride.getDriver().getUsername(), 
            totalFare, 
            ride.getEndTime()
        );

        ride.getPassenger().addRideRecord(record);
        ride.getDriver().addRideRecord(record);
        
        return true; 
    }
}