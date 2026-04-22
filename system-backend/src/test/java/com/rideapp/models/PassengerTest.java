// File: src/test/java/com/rideapp/models/PassengerTest.java
package com.rideapp.models;

import com.rideapp.payment.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TEST RECORD & METHODOLOGY:
 * --------------------------
 * WHAT IS TESTED:
 * 1. Role Identification: Ensures the class correctly identifies as "PASSENGER".
 * 2. Payment Logic: Verification of valid vs. invalid vs. missing payment methods.
 * 3. Debt Management: Testing that unpaid balances are tracked and cleared correctly.
 * 4. Integration Logic: Verification that the boolean "gatekeeper" methods behave as expected.
 *
 * HOW IT IS TESTED:
 * - Framework: JUnit 5 + Mockito.
 * - Stubs: Mockito is used to stub the PaymentMethod interface to simulate 
 * expired cards (isValid = false) or active cards (isValid = true).
 * - Assertions: Boolean checks for system-critical state flags.
 */
class PassengerTest {

    private Passenger passenger;

    @BeforeEach
    void setUp() {
        passenger = new Passenger("traveler_jane", "hashed_pass");
    }

    @Test
    @DisplayName("Role: Should return 'PASSENGER'")
    void testGetRole() {
        assertEquals("PASSENGER", passenger.getRole());
    }

    @Test
    @DisplayName("Payment: Should return false if no payment method is added")
    void testHasValidPaymentMethod_NoneProvided() {
        assertFalse(passenger.hasValidPaymentMethod(), "Should be invalid if no method is set.");
    }

    @Test
    @DisplayName("Payment: Should return true if a valid payment method is added")
    void testHasValidPaymentMethod_ValidStub() {
        // Arrange
        PaymentMethod mockCard = mock(PaymentMethod.class);
        when(mockCard.isValid()).thenReturn(true);
        when(mockCard.getMaskedDetails()).thenReturn("**** 1234");

        // Act
        passenger.addPaymentMethod(mockCard);

        // Assert
        assertTrue(passenger.hasValidPaymentMethod());
        assertEquals(mockCard, passenger.getDefaultPaymentMethod());
    }

    @Test
    @DisplayName("Payment: Should return false if the added payment method is invalid/expired")
    void testHasValidPaymentMethod_InvalidStub() {
        // Arrange
        PaymentMethod mockExpiredCard = mock(PaymentMethod.class);
        when(mockExpiredCard.isValid()).thenReturn(false);

        // Act
        passenger.addPaymentMethod(mockExpiredCard);

        // Assert
        assertFalse(passenger.hasValidPaymentMethod(), "Should return false for expired/invalid cards.");
    }

    @Test
    @DisplayName("Balance: Should correctly track and clear unpaid balances")
    void testUnpaidBalanceLogic() {
        // Act 1: Initial state
        assertFalse(passenger.hasUnpaidBalance());

        // Act 2: Add debt
        passenger.addUnpaidBalance(25.50);
        assertTrue(passenger.hasUnpaidBalance(), "Should return true when balance is > 0.");

        // Act 3: Clear debt
        passenger.clearUnpaidBalance();
        assertFalse(passenger.hasUnpaidBalance(), "Should return false after balance is cleared.");
    }

    @Test
    @DisplayName("Clearly Wrong: Verify that zero balance is not considered 'unpaid'")
    void testHasUnpaidBalance_ZeroAmount() {
        passenger.addUnpaidBalance(0.0);
        assertFalse(passenger.hasUnpaidBalance(), "Zero balance should not be flagged as an unpaid balance.");
    }
}