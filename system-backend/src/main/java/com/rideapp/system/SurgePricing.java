package com.rideapp.system;

public class SurgePricing implements PricingStrategy {
    @Override
    public double calculateFare(double distance) {
        return distance * 3.0; // High demand rate
    }
}