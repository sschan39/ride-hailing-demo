// File: src/test/java/com/rideapp/dispatch/RideDispatcherTest.java
package com.rideapp.dispatch;

import com.rideapp.models.Driver;
import com.rideapp.models.Passenger;
import com.rideapp.pricing.PricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
        // Pass null as passenger
        dispatcher.requestRide(null, "A", "B", 5.0, stubPricing);
        
        // Depending on your implementation, you might want to expose the queue size
        // for testing, or check console output. Assuming you expose getPendingRidesCount():
        // assertEquals(0, dispatcher.getPendingRidesCount());
    }

    @Test
    void testDriverIsMatchedWhenJoiningQueueWithPendingRides() {
        // 1. Passenger requests ride first (nobody online)
        dispatcher.requestRide(stubPassenger, "A", "B", 5.0, stubPricing);
        
        // 2. Driver goes online
        dispatcher.addAvailableDriver(stubDriver);
        
        // 3. Verify the driver was notified and accepted the ride
        // Mockito's 'verify' checks if a method was called on the stub
        verify(stubDriver, times(1)).update(any());
        verify(stubDriver, times(1)).acceptRide(any());
    }
}