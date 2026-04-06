// File: src/main/java/com/rideapp/system/DummyMapProvider.java
package com.rideapp.map;

import com.rideapp.models.Location;
import com.rideapp.models.Route;

public class DummyMapProvider implements MapProvider {
    private static final int EARTH_RADIUS_KM = 6371;
    private static final double AVERAGE_CITY_SPEED_KMH = 30.0;

    @Override
    public double getStraightLineDistance(Location loc1, Location loc2) {
        double latDistance = Math.toRadians(loc2.getLatitude() - loc1.getLatitude());
        double lonDistance = Math.toRadians(loc2.getLongitude() - loc1.getLongitude());
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(loc1.getLatitude())) * Math.cos(Math.toRadians(loc2.getLatitude()))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                 
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c; 
    }

    @Override
    public Route getRoute(Location origin, Location destination) {
        // In a real app, this would hit an API to get actual road distance, 
        // accounting for traffic and one-way streets.
        // For our dummy provider, we just use the straight-line distance.
        double distance = getStraightLineDistance(origin, destination);
        
        // Calculate ETA
        double timeInHours = distance / AVERAGE_CITY_SPEED_KMH;
        int eta = (int) Math.round(timeInHours * 60);

        // Return the packaged DTO
        return new Route(distance, eta);
    }
}