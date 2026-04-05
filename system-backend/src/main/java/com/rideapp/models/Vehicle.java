// File: src/main/java/com/rideapp/models/Vehicle.java
package com.rideapp.models;

public abstract class Vehicle {
    private String licensePlate;
    private String makeAndModel;

    public Vehicle(String licensePlate, String makeAndModel) {
        this.licensePlate = licensePlate;
        this.makeAndModel = makeAndModel;
    }

    public String getLicensePlate() { return licensePlate; }
    public String getMakeAndModel() { return makeAndModel; }
    
    // Every subclass must define what type of vehicle it is
    public abstract String getVehicleType();
}