package com.rideapp.events;

import com.rideapp.models.Ride;

// Observer Interface
public interface Observer {
    void update(Ride ride);
}