// File: src/test/java/com/rideapp/state/InTransitStateTest.java
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
 * 1. Completion Logic: Verifying that complete() records the endTime and transitions state.
 * 2. Guard Logic: Ensuring illegal actions (start, accept, processPayment) are blocked.
 * 3. Data Integrity: Checking that the ride is NOT payable while in transit.
 *
 * HOW IT IS TESTED:
 * - Framework: JUnit 5 + Mockito.
 * - Strategy: Verifying that the Ride object's internal state is mutated correctly 
 * when business events occur.
 */
class InTransitStateTest {

    private InTransitState state;
    private Ride mockRide;

    @BeforeEach
    void setUp() {
        state = new InTransitState();
        mockRide = mock(Ride.class);
        when(mockRide.getId()).thenReturn("transit-ride-uuid");
    }

    @Test
    @DisplayName("Transition: complete() should set endTime and move to CompletedState")
    void testComplete_Success() {
        // Act
        state.complete(mockRide);

        // Assert
        // 1. Verify endTime was captured
        verify(mockRide).setEndTime(any());

        // 2. Verify state transition to CompletedState
        ArgumentCaptor<RideState> stateCaptor = ArgumentCaptor.forClass(RideState.class);
        verify(mockRide).setState(stateCaptor.capture());
        assertTrue(stateCaptor.getValue() instanceof CompletedState, "Ride should transition to CompletedState");
    }

    @Test
    @DisplayName("Guard: isPayable should be false while in transit")
    void testIsPayable() {
        assertFalse(state.isPayable(), "Ride should not be payable while passenger is still in transit.");
    }

    @Test
    @DisplayName("Guard: processPayment() should be blocked while in transit")
    void testProcessPayment_Blocked() {
        state.processPayment(mockRide);
        verify(mockRide, never()).setState(any());
    }

    @Test
    @DisplayName("Clearly Wrong: Verify start() and accept() do nothing in this state")
    void testBlockedActions() {
        state.start(mockRide);
        state.accept(mockRide, null);

        verify(mockRide, never()).setState(any());
    }
}