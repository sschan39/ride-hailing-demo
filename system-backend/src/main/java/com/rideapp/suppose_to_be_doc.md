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



11/4
feat: replace boolean payment flags with PaymentMethod class hierarchy

- Created abstract `PaymentMethod` class to standardise payment validation and masking.
- Implemented `CreditCard` subclass to handle specific card details (number, exp date).
- Refactored `Passenger` model to replace `hasValidPaymentMethod` boolean with an actual `PaymentMethod` object dependency.
- Refactored `Passenger` model to replace `hasUnpaidBalance` boolean with a precise `double unpaidBalance` tracker.
- Updated `App.java` integration tests to demonstrate adding a credit card and clearing account debt before ride requests.

This resolves the primitive obsession regarding user account standing and aligns the codebase with the Precondition requirements specified in the Passenger Module Use Case document.

feat(dispatch): implement broadcast model and driver concurrency locks

- Refactored `RideDispatcher` to broadcast ride requests to all nearby eligible drivers instead of forcing direct assignment.
- Added `receivePushNotification`, `tryAcceptRide`, and `rejectRide` methods to the `Driver` model to support driver autonomy.
- Implemented a `synchronized` lock (`assignRideToDriver`) in the dispatcher to handle race conditions (Alternative Course 3b), ensuring only the first driver to accept is assigned the ride.
- Updated `App.java` integration test to simulate multiple drivers receiving the broadcast, rejecting, and concurrently attempting to accept a ride.

This aligns the codebase with the updated Driver Module Use Case documentation, specifically resolving the gaps for Alternative Courses 3a and 3b.

feat(ride-lifecycle): complete end-to-end ride execution with edge case handling

This commit finalizes the core ride execution loop, integrating the State Pattern, Payment Gateway, and Dispatcher with robust edge case handling as defined in the system requirements.

Key implementations:
- **Payment Stub & Routing**: Created `BankAPI` interface and `StubBankAPI`. The `PaymentGateway` now acts as a dynamic router, automatically processing aggregate `Ride` objects and verifying transactions. Card numbers ending in "9999" simulate 402 Declined responses.
- **Flexible Confirmation**: Overloaded `confirmEnd` in the `Ride` model to allow driver-injected odometer readings while structurally supporting future dual-confirmation logic.
- **Route Deviation (Alt Course 2a)**: Added logic to `Ride.finalizeRide()` to detect actual distance > 150% of estimated distance. Automatically pauses payment and flags the ride for manual review.
- **Payment Failure & Lockout (Alt Course 4a)**: If the bank stub declines the card, the system correctly freezes the passenger's account with an unpaid balance. Added a defensive check to `RideDispatcher` to instantly reject new ride requests from users with unpaid balances.
- **State Transition Fixes**: Ensured `RideDispatcher` triggers the `AcceptedState` transition upon successful assignment.
- **Test Suite**: Added an end-to-end master test in `App.java` validating the Happy Path, Route Deviation, Payment Declined, and Passenger Lockout scenarios. All tests pass successfully.