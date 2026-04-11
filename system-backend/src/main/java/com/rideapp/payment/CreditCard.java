// File: src/main/java/com/rideapp/payment/CreditCard.java
package com.rideapp.payment;

public class CreditCard extends PaymentMethod {
    private String cardNumber;
    private String expirationDate;

    public CreditCard(String accountHolderName, String cardNumber, String expirationDate) {
        super(accountHolderName);
        this.cardNumber = cardNumber;
        this.expirationDate = expirationDate;
    }

    public String getCardNumber() { return cardNumber; }

    @Override
    public String getMaskedDetails() {
        String lastFour = cardNumber.substring(Math.max(0, cardNumber.length() - 4));
        return "Credit Card ending in " + lastFour + " (Exp: " + expirationDate + ")";
    }
}