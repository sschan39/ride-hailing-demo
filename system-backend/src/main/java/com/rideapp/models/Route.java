// File: src/main/java/com/rideapp/models/Route.java
package com.rideapp.models;

public class Route {
    private final double distanceKm;
    private final int estimatedTimeMinutes;

    public Route(double distanceKm, int estimatedTimeMinutes) {
        this.distanceKm = distanceKm;
        this.estimatedTimeMinutes = estimatedTimeMinutes;
    }

    public double getDistanceKm() { return distanceKm; }
    public int getEstimatedTimeMinutes() { return estimatedTimeMinutes; }

    @Override
    public String toString() {
        return String.format("%.2f km (approx. %d mins)", distanceKm, estimatedTimeMinutes);
    }
}