// File: src/test/java/com/rideapp/models/RideTest.java
package com.rideapp.models;

import com.rideapp.pricing.PricingStrategy;
import com.rideapp.state.CompletedState;
import com.rideapp.state.InTransitState;
import com.rideapp.state.RideState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TEST RECORD & METHODOLOGY:
 * --------------------------
 * WHAT IS TESTED:
 * 1. Completion Logic: Testing single-confirmation vs. dual-confirmation modes.
 * 2. Safety/Fraud Detection: Verifying the "Route Deviation" check (>1.5x distance).
 * 3. State Delegation: Ensuring methods like accept() or complete() call the state object.
 * 4. Post-conditions: Verifying the driver is set back to available after a clean finish.
 *
 * HOW IT IS TESTED:
 * - Framework: JUnit 5 + Mockito.
 * - Strategy: Testing the 'evaluateCompletion' logic through public confirmEnd() methods.
 * - Stubs: Mocking Passenger, Driver, Route, and RideState to isolate the Ride's coordinating logic.
 */
class RideTest {

    private Ride ride;
    private Passenger mockPassenger;
    private Driver mockDriver;
    private Route mockRoute;
    private PricingStrategy mockPricing;

    @BeforeEach
    void setUp() {
        mockPassenger = mock(Passenger.class);
        mockDriver = mock(Driver.class);
        mockRoute = mock(Route.class);
        mockPricing = mock(PricingStrategy.class);
        
        when(mockRoute.getDistanceKm()).thenReturn(10.0);
        when(mockDriver.getUsername()).thenReturn("driver1");

        ride = new Ride(
            mockPassenger, 
            new Location(0,0, "A"), 
            new Location(0,1, "B"), 
            mockRoute, 
            Collections.singletonList("STANDARD"), 
            mockPricing
        );
        ride.setDriver(mockDriver);
    }

    @Test
    @DisplayName("Safety: Should flag manual review if actual distance is 50% higher than estimate")
    void testRouteDeviation_FlagsReview() {
        // Arrange: Est 10km, Actual 16km (> 10 * 1.5)
        double actualDistance = 16.0;

        // Act
        ride.confirmEnd(mockDriver, actualDistance);

        // Assert
        assertTrue(ride.requiresManualReview(), "Ride should be flagged for review due to deviation.");
        assertFalse(ride.isPayable(), "A flagged ride should not be payable.");
    }

    @Test
    @DisplayName("Safety: Should NOT flag manual review if distance is within 50% margin")
    void testRouteDeviation_NormalRide() {
        // Arrange: Est 10km, Actual 12km (Within margin)
        double actualDistance = 12.0;

        // Act
        ride.confirmEnd(mockDriver, actualDistance);

        // Assert
        assertFalse(ride.requiresManualReview());
        verify(mockDriver).setAvailable(true); // Should finalize and unlock driver
    }

    @Test
    @DisplayName("Flow: Driver confirmation should trigger finalization in default mode")
    void testConfirmEnd_SingleConfirmationMode() {
        // In default mode, passengerConfirmedEnd isn't required
        ride.confirmEnd(mockDriver, 10.0);

        // We verify finalizeRide ran by checking if the driver was set to available
        verify(mockDriver).setAvailable(true);
    }

    @Test
    @DisplayName("Clearly Wrong: confirmEnd should ignore calls from the WRONG driver")
    void testConfirmEnd_WrongDriver() {
        Driver wrongDriver = mock(Driver.class);
        when(wrongDriver.getUsername()).thenReturn("imposter");

        ride.confirmEnd(wrongDriver, 10.0);

        assertFalse(ride.requiresManualReview());
        verify(mockDriver, never()).setAvailable(true); 
    }

    @Test
    @DisplayName("Edge Case: isPayable should consider both manual review and state status")
    void testIsPayable_LogicalAND() {
        RideState mockState = mock(RideState.class);
        ride.setState(mockState);
        
        // Scenario: State says it's payable, but system flagged it for fraud
        when(mockState.isPayable()).thenReturn(true);
        // Force manual review via reflection or by triggering deviation
        ride.confirmEnd(mockDriver, 100.0); 

        assertFalse(ride.isPayable(), "Should return false if requiresManualReview is true, even if state is 'Paid'");
    }

    @Test
    @DisplayName("State Logic: accept() should delegate to current state")
    void testStateDelegation() {
        RideState mockState = mock(RideState.class);
        ride.setState(mockState);
        
        ride.accept(mockDriver);
        
        verify(mockState).accept(ride, mockDriver);
    }

    @Test
    @DisplayName("State Transition: complete() should move ride to CompletedState")
    void testRideCompleteTransition() {
        // Start in InTransitState
        ride.setState(new InTransitState());
        
        ride.complete();
        
        assertTrue(ride.getState() instanceof CompletedState);
        assertNotNull(ride.getEndTime());
    }
}