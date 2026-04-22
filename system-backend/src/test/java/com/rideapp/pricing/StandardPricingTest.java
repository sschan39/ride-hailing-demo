// File: src/test/java/com/rideapp/pricing/StandardPricingTest.java
package com.rideapp.pricing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TEST RECORD & METHODOLOGY:
 * --------------------------
 * WHAT IS TESTED:
 * 1. Accuracy: Verifying distance * 1.5 multiplier.
 * 2. Zero Case: Ensuring 0km results in $0 fare.
 * 3. Consistency: Ensuring the same input always yields the same price.
 */
class StandardPricingTest {

    private StandardPricing pricing;

    @BeforeEach
    void setUp() {
        pricing = new StandardPricing();
    }

    @Test
    @DisplayName("Math: Fare should be exactly distance * 1.5")
    void testCalculateFare_StandardCalculation() {
        double distance = 10.0;
        double expected = 15.0;
        
        assertEquals(expected, pricing.calculateFare(distance), 0.001);
    }

    @Test
    @DisplayName("Edge Case: Zero distance should result in zero fare")
    void testCalculateFare_ZeroDistance() {
        assertEquals(0.0, pricing.calculateFare(0.0), 0.001);
    }

    @Test
    @DisplayName("Clearly Wrong: Large distances should scale linearly")
    void testCalculateFare_LargeDistance() {
        // 100km * 1.5 = 150
        assertEquals(150.0, pricing.calculateFare(100.0), 0.001);
    }
}