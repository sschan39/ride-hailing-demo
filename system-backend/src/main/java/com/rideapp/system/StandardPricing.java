package com.rideapp.system;

public class StandardPricing implements PricingStrategy {
    @Override
    public double calculateFare(double distance) {
        return distance * 1.5; // Base rate
    }
}
