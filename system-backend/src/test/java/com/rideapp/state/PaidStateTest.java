// File: src/test/java/com/rideapp/state/PaidStateTest.java
package com.rideapp.state;

import com.rideapp.models.Ride;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TEST RECORD & METHODOLOGY:
 * --------------------------
 * WHAT IS TESTED:
 * 1. Terminal Behavior: Ensuring all transition methods (accept, start, complete, processPayment) 
 * do not result in further state changes.
 * 2. Finality: Verifying isPayable is false to prevent double-charging.
 *
 * HOW IT IS TESTED:
 * - Framework: JUnit 5 + Mockito.
 * - Strategy: "Black Hole" testing—verifying that no matter what is called, the 
 * mockRide.setState() method is never invoked.
 */
class PaidStateTest {

    private PaidState state;
    private Ride mockRide;

    @BeforeEach
    void setUp() {
        state = new PaidState();
        mockRide = mock(Ride.class);
    }

    @Test
    @DisplayName("Finality: isPayable must be false to prevent duplicate transactions")
    void testIsPayable() {
        assertFalse(state.isPayable(), "A ride in PaidState must not be payable again.");
    }

    @Test
    @DisplayName("Guard: All actions must be ignored in PaidState")
    void testAllActionsBlocked() {
        // Act
        state.accept(mockRide, null);
        state.start(mockRide);
        state.complete(mockRide);
        state.processPayment(mockRide);

        // Assert
        // The ride should never change state again once it is Paid.
        verify(mockRide, never()).setState(any());
    }
}