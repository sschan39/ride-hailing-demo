// File: src/test/java/com/rideapp/state/CompletedStateTest.java
package com.rideapp.state;

import com.rideapp.models.Ride;
import com.rideapp.payment.PaymentGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TEST RECORD & METHODOLOGY:
 * --------------------------
 * WHAT IS TESTED:
 * 1. Financial Integration: Verifying that processPayment calls the PaymentGateway.
 * 2. Success Transition: Ensuring a successful payment moves the Ride to PaidState.
 * 3. Failure Handling: Ensuring a failed payment does NOT change the state (stays in Completed).
 * 4. Business Rules: Verifying isPayable is true, and that all other actions (start, accept) are blocked.
 *
 * HOW IT IS TESTED:
 * - Framework: JUnit 5 + Mockito.
 * - Statics: Use Mockito.mockStatic to intercept the call to PaymentGateway.processPayment.
 * - Strategy: State-based testing to verify the Ride's internal state remains consistent.
 */
class CompletedStateTest {

    private CompletedState state;
    private Ride mockRide;

    @BeforeEach
    void setUp() {
        state = new CompletedState();
        mockRide = mock(Ride.class);
    }

    @Test
    @DisplayName("Logic: isPayable should be true in CompletedState")
    void testIsPayable() {
        assertTrue(state.isPayable(), "Ride must be payable once it reaches the Completed state.");
    }

    @Test
    @DisplayName("Transition: Successful payment moves Ride to PaidState")
    void testProcessPayment_Success() {
        try (MockedStatic<PaymentGateway> gatewayMock = mockStatic(PaymentGateway.class)) {
            // Arrange
            gatewayMock.when(() -> PaymentGateway.processPayment(mockRide)).thenReturn(true);

            // Act
            state.processPayment(mockRide);

            // Assert
            verify(mockRide).setState(any(PaidState.class));
            gatewayMock.verify(() -> PaymentGateway.processPayment(mockRide), times(1));
        }
    }

    @Test
    @DisplayName("Recovery: Failed payment keeps Ride in CompletedState for retry")
    void testProcessPayment_Failure() {
        try (MockedStatic<PaymentGateway> gatewayMock = mockStatic(PaymentGateway.class)) {
            // Arrange
            gatewayMock.when(() -> PaymentGateway.processPayment(mockRide)).thenReturn(false);

            // Act
            state.processPayment(mockRide);

            // Assert
            // State should NOT change to Paid if the bank declined
            verify(mockRide, never()).setState(any(PaidState.class));
        }
    }

    @Test
    @DisplayName("Clearly Wrong: All other actions should be ignored")
    void testBlockedActions() {
        // Act
        state.accept(mockRide, null);
        state.start(mockRide);
        state.complete(mockRide);

        // Assert
        verify(mockRide, never()).setState(any());
    }

    @Test
    @DisplayName("Integration: Ensure Ride is the same one passed to the Gateway")
    void testGatewayCallConsistency() {
        try (MockedStatic<PaymentGateway> gatewayMock = mockStatic(PaymentGateway.class)) {
            state.processPayment(mockRide);
            gatewayMock.verify(() -> PaymentGateway.processPayment(eq(mockRide)));
        }
    }
}