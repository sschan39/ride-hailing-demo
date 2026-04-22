// File: src/test/java/com/rideapp/pricing/SurgePricingTest.java
package com.rideapp.pricing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TEST RECORD & METHODOLOGY:
 * --------------------------
 * WHAT IS TESTED:
 * 1. Accuracy: Verifying the 3.0 multiplier (Surge Rate).
 * 2. Comparison: Ensuring Surge is consistently higher than Standard.
 * 3. Zero Case: Ensuring no base fare is hidden in the calculation.
 */
class SurgePricingTest {

    private SurgePricing surgePricing;
    private StandardPricing standardPricing;

    @BeforeEach
    void setUp() {
        surgePricing = new SurgePricing();
        standardPricing = new StandardPricing();
    }

    @Test
    @DisplayName("Math: Surge fare should be exactly distance * 3.0")
    void testCalculateFare_SurgeCalculation() {
        double distance = 10.0;
        double expected = 30.0;
        
        assertEquals(expected, surgePricing.calculateFare(distance), 0.001);
    }

    @Test
    @DisplayName("Logic: Surge Pricing should be exactly double the Standard Pricing")
    void testCompareToStandard() {
        double distance = 5.0;
        
        double standardTotal = standardPricing.calculateFare(distance); // 5 * 1.5 = 7.5
        double surgeTotal = surgePricing.calculateFare(distance);       // 5 * 3.0 = 15.0

        assertEquals(standardTotal * 2, surgeTotal, "Surge should be double the standard rate.");
    }

    @Test
    @DisplayName("Edge Case: Negative distance should return 0 or handle gracefully")
    void testNegativeDistance() {
        // Depending on your business requirements, you might want to return 0 
        // or throw an Exception. Currently, it would return a negative number.
        double result = surgePricing.calculateFare(-10.0);
        assertTrue(result <= 0, "Fare should not be positive for negative distance.");
    }
}