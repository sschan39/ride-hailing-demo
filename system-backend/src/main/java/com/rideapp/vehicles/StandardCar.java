// File: src/main/java/com/rideapp/models/StandardCar.java
package com.rideapp.vehicles;

import com.rideapp.models.Vehicle;


public class StandardCar extends Vehicle {
    public StandardCar(String licensePlate, String makeAndModel) {
        super(licensePlate, makeAndModel);
    }

    @Override
    public String getVehicleType() { return "STANDARD"; }
}