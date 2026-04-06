Phase 1: Setup & Availability
Account Registration: The AuthService (Singleton) handles secure registration and login for both Passenger and Driver (which inherit from the User base class).

The Garage: Drivers instantiate their specific Vehicle types (StandardCar, SUV) and register them.

Going Online: A driver logs in, selects their active vehicle, pushes their current GPS Location to the system, and tells the RideDispatcher they are available.

Phase 2: The Request & Geospatial Dispatch
The Request: A passenger defines their Origin, Destination, and acceptable vehicle types.

Routing: The Dispatcher queries the MapProvider (Facade Pattern) to calculate the physical distance and ETA, returning a clean Route DTO.

Geospatial Search: The Dispatcher scans its master list of online drivers, using the MapProvider's straight-line calculation to find drivers within a specific radius (e.g., 5km) who have the right vehicle type.

The Broadcast: The Dispatcher uses the Observer Pattern to ping these nearby drivers with the pending Ride request.

Phase 3: Acceptance & Transit
The Match: A nearby driver hits "Accept". The Dispatcher locks that driver (sets them to unavailable) and assigns them to the Ride.

Passenger Notification: The Dispatcher immediately fires a notification back to the Passenger: "Driver Ken is on the way in a Standard Car!"

State Transitions: The Ride uses the State Pattern to safely transition from RequestedState -> AcceptedState -> InTransitState.

Phase 4: Completion & Ledger
Dual Confirmation: Both the driver and passenger trigger confirmEnd() when they arrive.

Security Gate: The Ride transitions to CompletedState. The system checks isPayable() to ensure we aren't charging for a ride that was cancelled or is still driving.

The Split: The PaymentGateway calculates the final fare using the injected PricingStrategy (Strategy Pattern). It extracts the 20% platform fee and credits the remaining balance to the Driver's account.

The Receipt: The heavy Ride object is converted into an immutable RideRecord (DTO) and saved to both the Passenger's and Driver's ride histories.