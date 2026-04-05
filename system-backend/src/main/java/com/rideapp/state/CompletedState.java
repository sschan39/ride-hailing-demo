package com.rideapp.state;

import com.rideapp.models.Driver;
import com.rideapp.models.Ride;
import com.rideapp.payment.PaymentGateway;

public class CompletedState implements RideState {
    @Override
    public void accept(Ride ride, Driver driver) { System.out.println("Ride is already completed."); }
    @Override
    public void start(Ride ride) { System.out.println("Ride is already completed."); }
    @Override
    public void complete(Ride ride) { System.out.println("Ride is already completed."); }

    @Override
    public void processPayment(Ride ride) {
        // We move the gateway trigger INSIDE the state. 
        // This is the purest form of the State Pattern!
        System.out.println("[STATE] Initiating payment process...");
        boolean success = PaymentGateway.processPayment(ride);
        
        if (success) {
            ride.setState(new PaidState());
            System.out.println("[STATE] Ride is now PAID.");
        } else {
            System.out.println("[STATE] Payment failed. Waiting for retry.");
        }
    }

    @Override
    public boolean isPayable() {return true;}
}