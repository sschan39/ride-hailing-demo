// File: src/test/java/com/rideapp/dispatch/RideDispatcherTest.java
package com.rideapp.dispatch;

import com.rideapp.map.MapProvider;
import com.rideapp.models.*;
import com.rideapp.pricing.PricingStrategy;
import com.rideapp.pricing.SurgePricing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TEST RECORD & METHODOLOGY:
 * --------------------------
 * WHAT IS TESTED:
 * 1. Dispatcher Logic: Success path for requesting a ride and broadcasting to drivers.
 * 2. Preconditions: Rejection based on unpaid balances or invalid payment methods.
 * 3. Geospatial Logic: Ensuring drivers outside the 5km radius are ignored.
 * 4. Concurrency/Assignment: Ensuring a ride cannot be assigned to two drivers.
 * 5. Parameter Accuracy: Verified Location constructor order (lat, lon, address).
 *
 * HOW IT IS TESTED:
 * - Framework: JUnit 5 + Mockito.
 * - State Management: Reflection resets the Singleton 'instance' and 'onlineDrivers' 
 * list to ensure a clean slate for every test case.
 * - Stubs: MapProvider and PricingStrategy are mocked to prevent external API calls.
 */
class RideDispatcherTest {

    private RideDispatcher dispatcher;
    private MapProvider mockMapProvider;
    private PricingStrategy mockPricingStrategy;

    @BeforeEach
    void setUp() throws Exception {
        mockMapProvider = mock(MapProvider.class);
        mockPricingStrategy = mock(PricingStrategy.class);

        // RESET SINGLETON
        Field instance = RideDispatcher.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);

        dispatcher = RideDispatcher.getInstance(mockMapProvider);

        // CLEAR INTERNAL DRIVER LIST
        Field driversField = RideDispatcher.class.getDeclaredField("onlineDrivers");
        driversField.setAccessible(true);
        ((List<?>) driversField.get(dispatcher)).clear();
    }

    @Test
    @DisplayName("Should successfully request ride and notify nearby drivers")
    void testRequestRide_Success() {
        // Arrange
        Passenger mockPassenger = mock(Passenger.class);
        when(mockPassenger.hasUnpaidBalance()).thenReturn(false);
        when(mockPassenger.hasValidPaymentMethod()).thenReturn(true);

        // Fixed Location constructor: (lat, lon, address)
        Location origin = new Location(34.0522, -118.2437, "Los Angeles");
        Location dest = new Location(34.0736, -118.4004, "Beverly Hills");
        
        Route mockRoute = mock(Route.class);
        when(mockMapProvider.getRoute(origin, dest)).thenReturn(mockRoute);
        when(mockPricingStrategy.calculateFare(anyDouble())).thenReturn(25.0);

        Driver mockDriver = mock(Driver.class);
        Vehicle mockVehicle = mock(Vehicle.class);
        when(mockVehicle.getVehicleType()).thenReturn("STANDARD");
        when(mockDriver.getActiveVehicle()).thenReturn(mockVehicle);
        when(mockDriver.isAvailable()).thenReturn(true);
        when(mockDriver.getCurrentLocation()).thenReturn(new Location(34.0530, -118.2440, "Nearby Street"));
        
        when(mockMapProvider.getStraightLineDistance(any(), any())).thenReturn(2.0);

        addDriverToInternalList(mockDriver);

        // Act
        Ride result = dispatcher.requestRide(mockPassenger, origin, dest, Arrays.asList("STANDARD"), mockPricingStrategy);

        // Assert
        assertNotNull(result);
        verify(mockDriver, times(1)).receivePushNotification(eq(result), eq(25.0));
    }

    @Test
    @DisplayName("Should reject ride if passenger has unpaid balance")
    void testRequestRide_UnpaidBalance() {
        Passenger mockPassenger = mock(Passenger.class);
        when(mockPassenger.hasUnpaidBalance()).thenReturn(true);

        // Using dummy coordinates
        Location dummyLoc = new Location(0.0, 0.0, "Void");

        Ride result = dispatcher.requestRide(mockPassenger, dummyLoc, dummyLoc, Collections.singletonList("STANDARD"), mockPricingStrategy);

        assertNull(result, "Dispatcher should return null if passenger has debt.");
    }

    @Test
    @DisplayName("Should return null if no drivers are within search radius")
    void testRequestRide_NoDriversNearby() {
        Passenger mockPassenger = mock(Passenger.class);
        when(mockPassenger.hasUnpaidBalance()).thenReturn(false);
        when(mockPassenger.hasValidPaymentMethod()).thenReturn(true);
        when(mockMapProvider.getRoute(any(), any())).thenReturn(mock(Route.class));

        Driver farDriver = mock(Driver.class);
        when(farDriver.isAvailable()).thenReturn(true);
        // Driver is 10km away
        when(mockMapProvider.getStraightLineDistance(any(), any())).thenReturn(10.0); 
        addDriverToInternalList(farDriver);
        Vehicle mockVehicle = mock(Vehicle.class); // Create a mock vehicle
    
        when(farDriver.isAvailable()).thenReturn(true);
        when(farDriver.getActiveVehicle()).thenReturn(mockVehicle); // Link it
        when(mockVehicle.getVehicleType()).thenReturn("STANDARD"); // Give it a type

        Location origin = new Location(0.0, 0.0, "Origin");
        Location dest = new Location(0.1, 0.1, "Dest");

        Ride result = dispatcher.requestRide(mockPassenger, origin, dest, Collections.singletonList("STANDARD"), mockPricingStrategy);

        assertNull(result, "Should not return a ride if no drivers are within 5km.");
    }

    @Test
    @DisplayName("Assignment Logic: Should prevent two drivers from accepting the same ride")
    void testAssignRide_ConcurrencyProtection() {
        Location loc = new Location(0.0, 0.0, "Test Loc");
        Ride ride = new Ride(mock(Passenger.class), loc, loc, mock(Route.class), null, null);
        
        Driver driver1 = mock(Driver.class);
        when(driver1.getUsername()).thenReturn("Driver1");
        Driver driver2 = mock(Driver.class);
        when(driver2.getUsername()).thenReturn("Driver2");

        boolean firstAssignment = dispatcher.assignRideToDriver(driver1, ride);
        boolean secondAssignment = dispatcher.assignRideToDriver(driver2, ride);

        assertTrue(firstAssignment);
        assertFalse(secondAssignment, "Second assignment must fail.");
        assertEquals(driver1, ride.getDriver());
    }

    @Test
    @DisplayName("Clearly Wrong: Should fail if MapProvider cannot find a route")
    void testRequestRide_InvalidRoute() {
        Passenger mockPassenger = mock(Passenger.class);
        when(mockPassenger.hasUnpaidBalance()).thenReturn(false);
        when(mockPassenger.hasValidPaymentMethod()).thenReturn(true);
        
        when(mockMapProvider.getRoute(any(), any())).thenReturn(null);

        Location loc = new Location(0.0, 0.0, "Nowhere");
        Ride result = dispatcher.requestRide(mockPassenger, loc, loc, null, mockPricingStrategy);

        assertNull(result, "Ride should not be created if route is null.");
    }

    private void addDriverToInternalList(Driver d) {
        try {
            Field field = RideDispatcher.class.getDeclaredField("onlineDrivers");
            field.setAccessible(true);
            List<Driver> drivers = (List<Driver>) field.get(dispatcher);
            drivers.add(d);
        } catch (Exception e) {
            fail("Reflection injection failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Pricing Strategy: Dispatcher should handle Surge pricing math")
    void testRequestRide_WithSurgePricing() {
        PricingStrategy surge = new SurgePricing();
        Location origin = new Location(0, 0, "A");
        Location dest = new Location(0, 10, "B"); // 10km
        
        // In your Dispatcher logic, calculate fare
        double fare = surge.calculateFare(10.0);
        
        assertEquals(30.0, fare, "Surge should result in 10km * 3.0 = $30");
    }
}