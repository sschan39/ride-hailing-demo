// File: src/main/java/com/rideapp/payment/StubBankAPI.java
package com.rideapp.payment;

public class StubBankAPI implements BankAPI {
    @Override
    public boolean chargeCard(String cardNumber, double amount) {
        System.out.println("🌐 [BANK STUB] Contacting bank API for card ending in " + 
                           cardNumber.substring(Math.max(0, cardNumber.length() - 4)) + "...");
        
        // Simulate network delay
        try { Thread.sleep(500); } catch (InterruptedException e) { }

        // Test logic: end with Card "9999" always declines due to insufficient funds
        if (cardNumber.endsWith("9999")) {
            System.out.println("❌ [BANK STUB] Response: 402 Payment Required (Insufficient Funds).");
            return false;
        }

        System.out.println("✅ [BANK STUB] Response: 200 OK (Transaction Approved).");
        return true;
    }
}