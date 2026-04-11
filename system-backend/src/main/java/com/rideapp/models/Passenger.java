// File: Passenger.java
package com.rideapp.models;

import com.rideapp.payment.PaymentMethod;

public class Passenger extends User {
    private PaymentMethod defaultPaymentMethod;
    private double unpaidBalance;

    public Passenger(String username, String hashedPassword) {
        super(username, hashedPassword);
        this.unpaidBalance = 0.0;
    }

    @Override
    public String getRole() { return "PASSENGER"; }

    public void addPaymentMethod(PaymentMethod method) {
        this.defaultPaymentMethod = method;
        System.out.println("[SYSTEM] Added " + method.getMaskedDetails() + " to " + getUsername() + "'s account.");
    }
    public PaymentMethod getDefaultPaymentMethod() { return defaultPaymentMethod; }

    public boolean hasValidPaymentMethod() { 
        return defaultPaymentMethod != null && defaultPaymentMethod.isValid(); 
    }
    public boolean hasUnpaidBalance() { 
        return unpaidBalance > 0.0; 
    }

    public void addUnpaidBalance(double amount) { this.unpaidBalance += amount; }
    public void clearUnpaidBalance() { this.unpaidBalance = 0.0; }
}