// File: src/main/java/com/rideapp/system/MapProvider.java
package com.rideapp.map;

import com.rideapp.models.Location;
import com.rideapp.models.Route;

public interface MapProvider {
    Route getRoute(Location origin, Location destination);

    double getStraightLineDistance(Location loc1, Location loc2);
}