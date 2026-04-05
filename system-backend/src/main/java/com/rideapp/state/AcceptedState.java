// File: src/main/java/com/rideapp/state/AcceptedState.java
package com.rideapp.state;

import com.rideapp.models.Ride;
import com.rideapp.models.Driver;
import java.time.LocalDateTime;

public class AcceptedState implements RideState {
    @Override
    public void accept(Ride ride, Driver driver) { System.out.println("Ride already accepted."); }
    
    @Override
    public void start(Ride ride) {
        ride.setStartTime(LocalDateTime.now()); // Record start time
        System.out.println("[STATE] Driver picked up passenger. Ride " + ride.getId().substring(0,8) + "... started at " + ride.getStartTime());
        ride.setState(new InTransitState());
    }

    @Override
    public void complete(Ride ride) { System.out.println("Cannot complete. Ride not started."); }
    @Override
    public void processPayment(Ride ride) { System.out.println("Cannot pay. Ride not completed."); }

    @Override
    public boolean isPayable() {return false;}
}