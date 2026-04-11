// File: src/main/java/com/rideapp/payment/PaymentMethod.java
package com.rideapp.payment;

public abstract class PaymentMethod {
    private String accountHolderName;
    private boolean isValid;

    public PaymentMethod(String accountHolderName) {
        this.accountHolderName = accountHolderName;
        this.isValid = true; // Assume valid upon creation for this demo
    }

    public String getAccountHolderName() { return accountHolderName; }
    public boolean isValid() { return isValid; }
    public void setValid(boolean valid) { this.isValid = valid; }

    // Every payment method must know how to display its masked details
    public abstract String getMaskedDetails();
}