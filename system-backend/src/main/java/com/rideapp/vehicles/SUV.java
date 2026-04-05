// File: src/main/java/com/rideapp/models/SUV.java
package com.rideapp.vehicles;

import com.rideapp.models.Vehicle;

public class SUV extends Vehicle {
    public SUV(String licensePlate, String makeAndModel) {
        super(licensePlate, makeAndModel);
    }

    @Override
    public String getVehicleType() { return "SUV"; }
}