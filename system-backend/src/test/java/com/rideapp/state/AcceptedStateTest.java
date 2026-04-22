// File: src/test/java/com/rideapp/state/AcceptedStateTest.java
package com.rideapp.state;

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
 * 1. State Transition: Verifying that calling start() moves the Ride to InTransitState.
 * 2. Data Recording: Ensuring the startTime is captured when the ride starts.
 * 3. Guard Logic: Verifying that illegal transitions (complete, processPayment) are blocked.
 * 4. Safety: Ensuring isPayable() remains false in this state.
 *
 * HOW IT IS TESTED:
 * - Framework: JUnit 5 + Mockito.
 * - Strategy: Behavior verification. We mock the Ride object and use ArgumentCaptors 
 * to check that the state was updated correctly.
 */
class AcceptedStateTest {

    private AcceptedState state;
    private Ride mockRide;

    @BeforeEach
    void setUp() {
        state = new AcceptedState();
        mockRide = mock(Ride.class);
        when(mockRide.getId()).thenReturn("test-ride-uuid");
    }

    @Test
    @DisplayName("Transition: start() should set startTime and move to InTransitState")
    void testStart_Success() {
        // Act
        state.start(mockRide);

        // Assert
        // 1. Verify startTime was set
        verify(mockRide).setStartTime(any());
        
        // 2. Verify state transition to InTransitState
        ArgumentCaptor<RideState> stateCaptor = ArgumentCaptor.forClass(RideState.class);
        verify(mockRide).setState(stateCaptor.capture());
        assertTrue(stateCaptor.getValue() instanceof InTransitState, "State should transition to InTransitState");
    }

    @Test
    @DisplayName("Guard: isPayable should always be false in AcceptedState")
    void testIsPayable() {
        assertFalse(state.isPayable(), "AcceptedState should never allow payment processing.");
    }

    @Test
    @DisplayName("Guard: complete() and processPayment() should not trigger state changes")
    void testIllegalTransitions() {
        // Act
        state.complete(mockRide);
        state.processPayment(mockRide);

        // Assert
        // Verify setState was never called during these illegal attempts
        verify(mockRide, never()).setState(any());
    }

    @Test
    @DisplayName("Clearly Wrong: Verify accept() does nothing if already in AcceptedState")
    void testAccept_AlreadyAccepted() {
        state.accept(mockRide, null);
        verify(mockRide, never()).setState(any());
    }
}