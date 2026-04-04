package com.rideapp.system;

import com.rideapp.driver.Driver;
import com.rideapp.models.Ride;
import java.util.ArrayList;
import java.util.List;

public class RideDispatcher {
    private static RideDispatcher instance;
    private List<Driver> availableDrivers;

    private RideDispatcher() {
        availableDrivers = new ArrayList<>();
    }

    public static RideDispatcher getInstance() {
        if (instance == null) {
            instance = new RideDispatcher();
        }
        return instance;
    }

    public void registerDriver(Driver driver) {
        availableDrivers.add(driver);
    }

    public void unregisterDriver(Driver driver) {
        availableDrivers.remove(driver);
    }

    // Broadcasts to all observers (drivers)
    public void requestRide(Ride ride) {
        System.out.println("System: Broadcasting new ride request...");
        for (Driver driver : availableDrivers) {
            driver.update(ride);
        }
    }
}