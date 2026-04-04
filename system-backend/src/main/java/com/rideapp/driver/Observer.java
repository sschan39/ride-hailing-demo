package com.rideapp.driver;

import com.rideapp.models.Ride;

// Observer Interface
interface Observer {
    void update(Ride ride);
}