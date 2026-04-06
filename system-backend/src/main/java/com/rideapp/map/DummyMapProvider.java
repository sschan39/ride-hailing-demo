// File: src/main/java/com/rideapp/system/DummyMapProvider.java
package com.rideapp.map;

import com.rideapp.models.Location;

public class DummyMapProvider implements MapProvider {
    private static final int EARTH_RADIUS_KM = 6371;
    private static final double AVERAGE_CITY_SPEED_KMH = 30.0; // 30 km/h average speed

    @Override
    public double calculateDistance(Location loc1, Location loc2) {
        // Real Haversine formula for calculating distance on a sphere
        double latDistance = Math.toRadians(loc2.getLatitude() - loc1.getLatitude());
        double lonDistance = Math.toRadians(loc2.getLongitude() - loc1.getLongitude());
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(loc1.getLatitude())) * Math.cos(Math.toRadians(loc2.getLatitude()))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
                 
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c; // Distance in kilometers
    }

    @Override
    public int calculateETA(double distanceKm) {
        // Time = Distance / Speed. Convert to minutes.
        double timeInHours = distanceKm / AVERAGE_CITY_SPEED_KMH;
        return (int) Math.round(timeInHours * 60);
    }
}