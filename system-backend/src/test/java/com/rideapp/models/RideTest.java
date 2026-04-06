// File: src/test/java/com/rideapp/models/RideTest.java
package com.rideapp.models;

import com.rideapp.pricing.PricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

import java.util.Arrays;

class RideTest {
    private Ride ride;
    private Passenger mockPassenger;
    private Driver mockDriver;
    private PricingStrategy stubPricing;

    @BeforeEach
    void setUp() {
        // 1. Create Stubs/Mocks
        mockPassenger = mock(Passenger.class);
        mockDriver = mock(Driver.class);
        stubPricing = mock(PricingStrategy.class);

        // 2. Define Stub Behavior
        when(mockPassenger.getRole()).thenReturn("PASSENGER");
        when(mockPassenger.getUsername()).thenReturn("test_rider");
        when(mockDriver.getRole()).thenReturn("DRIVER");
        when(mockDriver.getUsername()).thenReturn("test_driver");
        
        // No matter what distance is passed, our stub always charges $20.0
        when(stubPricing.calculateFare(anyDouble())).thenReturn(20.0);

        // 3. Initialize the Object Under Test
        ride = new Ride(mockPassenger, "Point A", "Point B", 10.0, Arrays.asList("STANDARD"),stubPricing);
    }

    @Test
    void testRideRequiresDualConfirmationToComplete() {
        // Setup state to InTransit
        ride.accept(mockDriver);
        ride.start();
        
        assertFalse(ride.isPayable(), "Ride should not be payable while in transit");

        // Passenger confirms (1 of 2)
        ride.confirmEnd(mockPassenger);
        assertFalse(ride.isPayable(), "Ride should still not be payable, waiting on driver");

        // Driver confirms (2 of 2)
        ride.confirmEnd(mockDriver);
        
        // Now it should have transitioned through Completed to Paid
        // isPayable() returns false once it hits PaidState, so let's verify it got processed
        // We can verify this by checking if the state is no longer InTransit
        assertEquals("PaidState", ride.getState().getClass().getSimpleName());
    }

    @Test
    void testDefensivePaymentCheck() {
        // Ride just requested, not even accepted
        assertFalse(ride.isPayable(), "Requested ride must block payment attempts");
    }
}