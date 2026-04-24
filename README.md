# Ride Hailing Demo

Simple demo system for a ride-hailing workflow (registration, dispatch, trip lifecycle, and payment simulation).

## Installation Guide

### Prerequisites
- Java 17
- Maven 3.8+

### 1) Open the backend project
```bash
cd /home/runner/work/ride-hailing-demo/ride-hailing-demo/system-backend
```

### 2) Install dependencies and compile
```bash
mvn clean compile
```

### 3) Run tests
```bash
mvn test
```

> Note: At the time of writing, there is a pre-existing failing test (`AuthServiceTest.testNullInputs`) unrelated to this documentation update.

### 4) Run a demo scenario
Run any demo `main` class:

```bash
mvn org.codehaus.mojo:exec-maven-plugin:3.1.0:java -Dexec.mainClass=com.rideapp.App
```

Other scenarios:
- `com.rideapp.App1` (preconditions, invalid route, surge retry)
- `com.rideapp.App3` (broadcast + multi-driver accept/reject behavior)
- `com.rideapp.App4` (happy path + payment/edge-case scenarios)

### 5) View the UI mockup (optional)
Open this file in a browser:
- `/home/runner/work/ride-hailing-demo/ride-hailing-demo/Mobile_Interface.html`

---

## Simple User Manual

### Roles
- **Passenger**: requests rides and pays for completed trips.
- **Driver**: registers a vehicle, goes online, accepts rides, and completes trips.

### Passenger Flow
1. Register and log in.
2. Add a valid payment method.
3. Enter pickup and destination.
4. Choose acceptable vehicle types.
5. Submit ride request.
6. Wait for driver assignment.
7. Confirm trip end.
8. Review receipt/history.

### Driver Flow
1. Register and log in.
2. Register vehicle (e.g., Standard or SUV).
3. Set vehicle active and go online with current location.
4. Receive nearby ride requests.
5. Accept or reject request.
6. Start ride after pickup.
7. Confirm end of ride to trigger payment flow.
8. Check earnings/history.

### Key System Behaviors
- Nearby-driver search is based on rider location and vehicle type.
- First successful driver acceptance gets assigned.
- Trip moves through states: Requested → Accepted → InTransit → Completed/Paid.
- Payment can fail (simulated), creating unpaid balance and blocking new ride requests until cleared.

### Troubleshooting
- **No driver assigned**: ensure at least one driver is online, in range, and has a matching vehicle type.
- **Ride request rejected**: check passenger payment method and unpaid balance.
- **Payment declined in demo**: card numbers ending with `9999` simulate bank decline behavior.
