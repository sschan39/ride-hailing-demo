// File: src/main/java/com/rideapp/state/InTransitState.java
package com.rideapp.state;

import com.rideapp.models.Ride;
import com.rideapp.models.Driver;
import java.time.LocalDateTime;

public class InTransitState implements RideState {
    @Override
    public void accept(Ride ride, Driver driver) { System.out.println("Ride already in transit."); }
    @Override
    public void start(Ride ride) { System.out.println("Ride already in transit."); }

    @Override
    public void complete(Ride ride) {
        ride.setEndTime(LocalDateTime.now()); // Record end time
        System.out.println("[STATE] Arrived at destination. Ride " + ride.getId().substring(0,8) + "... completed at " + ride.getEndTime());
        ride.setState(new CompletedState());
    }

    @Override
    public void processPayment(Ride ride) { System.out.println("Cannot pay yet. Ride is still in transit."); }

    @Override
    public boolean isPayable() {return false;}
}