// File: src/test/java/com/rideapp/state/RequestedStateTest.java
package com.rideapp.state;

import com.rideapp.models.Driver;
import com.rideapp.models.Ride;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TEST RECORD & METHODOLOGY:
 * --------------------------
 * WHAT IS TESTED:
 * 1. Primary Transition: Ensuring accept() sets the driver and moves to AcceptedState.
 * 2. Guard Logic: Verifying that start, complete, and processPayment are ignored.
 * 3. Security: Verifying isPayable is false (money shouldn't move before the driver arrives).
 *
 * HOW IT IS TESTED:
 * - Framework: JUnit 5 + Mockito.
 * - Strategy: Verify interaction and state mutation on the Ride mock.
 */
class RequestedStateTest {

    private RequestedState state;
    private Ride mockRide;
    private Driver mockDriver;

    @BeforeEach
    void setUp() {
        state = new RequestedState();
        mockRide = mock(Ride.class);
        mockDriver = mock(Driver.class);
        when(mockDriver.getUsername()).thenReturn("driver_smith");
    }

    @Test
    @DisplayName("Transition: accept() should link driver and move to AcceptedState")
    void testAccept_Success() {
        // Act
        state.accept(mockRide, mockDriver);

        // Assert
        // 1. Verify driver was assigned to the ride
        verify(mockRide).setDriver(mockDriver);

        // 2. Verify transition to AcceptedState
        ArgumentCaptor<RideState> stateCaptor = ArgumentCaptor.forClass(RideState.class);
        verify(mockRide).setState(stateCaptor.capture());
        assertTrue(stateCaptor.getValue() instanceof AcceptedState, "Ride should transition to AcceptedState");
    }

    @Test
    @DisplayName("Guard: isPayable must be false in RequestedState")
    void testIsPayable() {
        assertFalse(state.isPayable());
    }

    @Test
    @DisplayName("Guard: All other actions should be blocked")
    void testBlockedActions() {
        // Act
        state.start(mockRide);
        state.complete(mockRide);
        state.processPayment(mockRide);

        // Assert
        // setState should NEVER be called for these actions in this state
        verify(mockRide, never()).setState(any());
    }
}