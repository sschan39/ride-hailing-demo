package com.rideapp.pricing;

public class StandardPricing implements PricingStrategy {

    double baseFare;
    double ratePerKm;
    double ratePerMin;
    @Override
    public double calculateFare(double distance) {
        return distance * 1.5; // Base rate
    }
}
