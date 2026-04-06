// File: src/test/java/com/rideapp/dispatch/RideDispatcherTest.java
package com.rideapp.dispatch;

import com.rideapp.models.Driver;
import com.rideapp.models.Passenger;
import com.rideapp.pricing.PricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

// Add this import to use Arrays.asList
import java.util.Arrays; 

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RideDispatcherTest {
    private RideDispatcher dispatcher;
    private Driver stubDriver;
    private Passenger stubPassenger;
    private PricingStrategy stubPricing;

    @BeforeEach
    void setUp() {
        dispatcher = RideDispatcher.getInstance();
        
        stubDriver = mock(Driver.class);
        when(stubDriver.getUsername()).thenReturn("QueueDriver");
        when(stubDriver.isAvailable()).thenReturn(true);
        
        stubPassenger = mock(Passenger.class);
        stubPricing = mock(PricingStrategy.class);
    }

    @Test
    void testUnauthorizedPassengerCannotRequestRide() {
        // FIX: Added Arrays.asList("STANDARD") as the 5th argument
        dispatcher.requestRide(null, "A", "B", 5.0, Arrays.asList("STANDARD"), stubPricing);
        
        // Assertions remain the same
    }

    @Test
    void testDriverIsMatchedWhenJoiningQueueWithPendingRides() {
        // FIX: Added Arrays.asList("STANDARD") as the 5th argument
        dispatcher.requestRide(stubPassenger, "A", "B", 5.0, Arrays.asList("STANDARD"), stubPricing);
        
        // 2. Driver goes online (Make sure to pass the dummy license plate we added earlier!)
        dispatcher.addAvailableDriver(stubDriver, "DUMMY-PLATE");
        
        // 3. Verify the driver was notified and accepted the ride
        verify(stubDriver, times(1)).update(any());
        verify(stubDriver, times(1)).acceptRide(any());
    }
}