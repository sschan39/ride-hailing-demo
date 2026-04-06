// File: src/main/java/com/rideapp/system/MapProvider.java
package com.rideapp.map;

import com.rideapp.models.Location;

public interface MapProvider {
    double calculateDistance(Location origin, Location destination);
    int calculateETA(double distanceKm);
}