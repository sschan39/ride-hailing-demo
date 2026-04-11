// File: src/main/java/com/rideapp/payment/BankAPI.java
package com.rideapp.payment;

public interface BankAPI {
    boolean chargeCard(String cardNumber, double amount);
}