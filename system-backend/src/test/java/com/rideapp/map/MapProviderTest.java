// File: src/test/java/com/rideapp/system/MapProviderTest.java
package com.rideapp.map;

import com.rideapp.models.Location;
import com.rideapp.models.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapProviderTest {
    private MapProvider mapProvider;
    private Location tokyoStation;
    private Location shibuyaStation;

    @BeforeEach
    void setUp() {
        mapProvider = new DummyMapProvider();
        tokyoStation = new Location(35.6812, 139.7671, "Tokyo Station");
        shibuyaStation = new Location(35.6580, 139.7016, "Shibuya Station");
    }

    @Test
    void testStraightLineDistanceCalculation() {
        double distance = mapProvider.getStraightLineDistance(tokyoStation, shibuyaStation);
        
        // The real-world straight-line distance is roughly 6.5km. 
        // We use a delta of 0.5km to account for slight Haversine rounding differences.
        assertEquals(6.5, distance, 0.5, "Distance should be approximately 6.5km");
    }

    @Test
    void testRouteDtoGeneration() {
        Route route = mapProvider.getRoute(tokyoStation, shibuyaStation);
        
        assertNotNull(route, "Route DTO should not be null");
        assertTrue(route.getDistanceKm() > 0, "Distance should be positive");
        assertTrue(route.getEstimatedTimeMinutes() > 0, "ETA should be positive");
    }
}