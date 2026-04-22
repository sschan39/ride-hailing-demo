// File: src/test/java/com/rideapp/map/DummyMapProviderTest.java
package com.rideapp.map;

import com.rideapp.models.Location;
import com.rideapp.models.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TEST RECORD & METHODOLOGY:
 * --------------------------
 * WHAT IS TESTED:
 * 1. Haversine Formula Accuracy: Verifying that the straight-line distance math 
 * correctly calculates distances between known global coordinates.
 * 2. Route Logic: Ensuring the ETA is calculated correctly based on the 30km/h constant.
 * 3. Error Handling: Verifying that "Invalid" addresses correctly trigger a null return.
 * 4. Precision: Checking that very small distances (same location) return 0.
 *
 * HOW IT IS TESTED:
 * - Framework: JUnit 5.
 * - Strategy: Value-based testing using known geographic points (e.g., London to Paris).
 * - Assertions: assertEquals with a delta (0.1) for floating-point math comparisons.
 */
class DummyMapProviderTest {

    private DummyMapProvider mapProvider;

    @BeforeEach
    void setUp() {
        mapProvider = new DummyMapProvider();
    }

    @Test
    @DisplayName("Should calculate correct distance between London and Paris (~344km)")
    void testGetStraightLineDistance_RealPoints() {
        // Arrange
        Location london = new Location(51.5074, -0.1278, "London");
        Location paris = new Location(48.8566, 2.3522, "Paris");

        // Act
        double distance = mapProvider.getStraightLineDistance(london, paris);

        // Assert
        // Allowed delta of 1.0 km because Earth isn't a perfect sphere in all formulas
        assertEquals(344.0, distance, 1.0, "Distance between London and Paris should be approx 344km");
    }

    @Test
    @DisplayName("Should return 0 distance for the same location")
    void testGetStraightLineDistance_SameLocation() {
        Location loc = new Location(40.7128, -74.0060, "New York");
        double distance = mapProvider.getStraightLineDistance(loc, loc);
        assertEquals(0.0, distance, 0.001);
    }

    @Test
    @DisplayName("Should calculate correct ETA based on 30km/h speed")
    void testGetRoute_ETACalculation() {
        // Arrange: 15km distance should take 30 mins at 30km/h
        Location start = new Location(0.0, 0.0, "Start");
        Location end = new Location(0.1349, 0.0, "End"); // Approx 15km apart

        // Act
        Route route = mapProvider.getRoute(start, end);

        // Assert
        assertNotNull(route);
        assertEquals(15.0, route.getDistanceKm(), 0.5);
        assertEquals(30, route.getEstimatedTimeMinutes(), "15km at 30km/h should take 30 minutes");
    }

    @Test
    @DisplayName("Should return null if address is 'Invalid'")
    void testGetRoute_InvalidAddress() {
        Location valid = new Location(1.0, 1.0, "Valid St");
        Location invalid = new Location(2.0, 2.0, "Invalid");

        Route result1 = mapProvider.getRoute(invalid, valid);
        Route result2 = mapProvider.getRoute(valid, invalid);

        assertNull(result1, "Route should be null if origin is Invalid");
        assertNull(result2, "Route should be null if destination is Invalid");
    }

    @Test
    @DisplayName("Clearly Wrong: Verify distance isn't negative")
    void testDistance_NonNegative() {
        Location loc1 = new Location(-10.0, -10.0, "A");
        Location loc2 = new Location(10.0, 10.0, "B");
        
        double distance = mapProvider.getStraightLineDistance(loc1, loc2);
        assertTrue(distance >= 0, "Distance can never be negative");
    }
}