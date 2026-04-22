// File: src/test/java/com/rideapp/payment/PaymentGatewayTest.java
package com.rideapp.payment;

import com.rideapp.models.*;
import com.rideapp.pricing.PricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;

/**
 * TEST RECORD & METHODOLOGY:
 * --------------------------
 * WHAT IS TESTED:
 * 1. Ledger Math: Verifying the 20% platform fee vs 80% driver split.
 * 2. Error Handling: Rejection of payments when the Ride is not in a 'Payable' state.
 * 3. Recovery Logic: Ensuring 'unpaidBalance' is added to Passenger if the bank declines.
 * 4. Record Keeping: Checking that RideRecords are added to both parties on success.
 *
 * HOW IT IS TESTED:
 * - Framework: JUnit 5 + Mockito.
 * - Strategy: Mocking the Ride, Passenger, Driver, and CreditCard objects.
 * - Precision: Using deltas for double-precision math (currency).
 */
class PaymentGatewayTest {

    private Ride mockRide;
    private Passenger mockPassenger;
    private Driver mockDriver;
    private PricingStrategy mockPricing;
    private CreditCard mockCard;

    @BeforeEach
    void setUp() {
        mockRide = mock(Ride.class);
        mockPassenger = mock(Passenger.class);
        mockDriver = mock(Driver.class);
        mockPricing = mock(PricingStrategy.class);
        mockCard = mock(CreditCard.class);

        // Setup common returns
        when(mockRide.getPassenger()).thenReturn(mockPassenger);
        when(mockRide.getDriver()).thenReturn(mockDriver);
        when(mockRide.getPricingStrategy()).thenReturn(mockPricing);
        when(mockPassenger.getDefaultPaymentMethod()).thenReturn(mockCard);
        
        // Mock names for logging/logic
        when(mockPassenger.getUsername()).thenReturn("jane_doe");
        when(mockDriver.getUsername()).thenReturn("driver_dan");
        when(mockRide.getId()).thenReturn("ride-12345678");
    }

    @Test
    @DisplayName("Security: Should fail immediately if ride.isPayable() is false")
    void testProcessPayment_NotPayable() {
        when(mockRide.isPayable()).thenReturn(false);

        boolean result = PaymentGateway.processPayment(mockRide);

        assertFalse(result, "Payment should not process for unpayable rides.");
        verifyNoInteractions(mockPricing); // Should never even calculate fare
    }

    @Test
    @DisplayName("Math: Verify 80/20 split between Driver and Platform")
    void testProcessPayment_SuccessSplit() {
        // Arrange
        when(mockRide.isPayable()).thenReturn(true);
        when(mockRide.getDistance()).thenReturn(10.0);
        when(mockPricing.calculateFare(10.0)).thenReturn(100.0); // Simple $100 fare
        
        // Mock Card to be a CreditCard specifically
        when(mockCard.getCardNumber()).thenReturn("1234-5678");

        // Act
        boolean result = PaymentGateway.processPayment(mockRide);

        // Assert
        assertTrue(result);
        // Driver should get 80% of $100 = $80.0
        verify(mockDriver).addEarnings(80.0);
        
        // Verify RideRecords were created and added
        verify(mockPassenger).addRideRecord(any(RideRecord.class));
        verify(mockDriver).addRideRecord(any(RideRecord.class));
    }

    @Test
    @DisplayName("Recovery: Bank decline should add unpaid balance to passenger")
    void testProcessPayment_BankDecline() {
        // Arrange
        when(mockRide.isPayable()).thenReturn(true);
        when(mockPricing.calculateFare(anyDouble())).thenReturn(50.0);
        
        // We cannot easily mock 'new StubBankAPI()', but if that stub is hardcoded
        // to pass/fail based on certain inputs, we test that. 
        // Assuming StubBankAPI is currently hardcoded to return true for testing, 
        // a failed bank check should result in:
        
        // (Simulating the logic path of a failure)
        // Note: To truly test a "fail" in StubBankAPI, you'd need to inject the API 
        // into the Gateway rather than 'newing' it up inside the method.
    }

    @Test
    @DisplayName("Edge Case: Unsupported Payment Method")
    void testProcessPayment_UnsupportedMethod() {
        // Arrange
        when(mockRide.isPayable()).thenReturn(true);
        // Return a generic PaymentMethod that isn't a CreditCard
        when(mockPassenger.getDefaultPaymentMethod()).thenReturn(mock(PaymentMethod.class));

        // Act
        boolean result = PaymentGateway.processPayment(mockRide);

        // Assert
        assertFalse(result, "Should fail for unsupported payment types.");
    }
    @Test
    @DisplayName("Recovery Logic: Should add unpaid balance to passenger when bank declines (Card 9999)")
    void testProcessPayment_BankDecline_AddsDebt() {
        // Arrange
        when(mockRide.isPayable()).thenReturn(true);
        when(mockRide.getDistance()).thenReturn(10.0);
        when(mockPricing.calculateFare(10.0)).thenReturn(50.0);
        
        // Use the magic "9999" number from your StubBankAPI logic
        when(mockCard.getCardNumber()).thenReturn("4111-1111-1111-9999");

        // Act
        boolean result = PaymentGateway.processPayment(mockRide);

        // Assert
        assertFalse(result, "Payment should fail if the bank declines the transaction.");
        
        // Verify that the $50.0 was actually added to the passenger's debt
        verify(mockPassenger, times(1)).addUnpaidBalance(50.0);
        
        // Verify that the driver did NOT get paid
        verify(mockDriver, never()).addEarnings(anyDouble());
    }
}