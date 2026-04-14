## Regenerated Use Case Diagram

```plantuml
@startuml
left to right direction
skinparam packageStyle rectangle

actor User
actor Passenger
actor Driver
actor "Map Provider" as MapProvider
actor "Payment Gateway" as PaymentGateway

Passenger --|> User
Driver --|> User

rectangle "Ride Hailing System" {
  usecase "Register Account" as UC_Register
  usecase "Login" as UC_Login
  usecase "View Ride History" as UC_History

  usecase "Request Ride" as UC_Request
  usecase "Calculate Route & ETA" as UC_Route
  usecase "Find Nearby Eligible Drivers" as UC_Search
  usecase "Broadcast Ride Request" as UC_Broadcast

  usecase "Register Vehicle" as UC_RegisterVehicle
  usecase "Select Active Vehicle" as UC_SelectVehicle
  usecase "Go Online / Set Availability" as UC_Availability
  usecase "Accept Ride" as UC_Accept
  usecase "Start Ride" as UC_Start

  usecase "Confirm Ride End" as UC_ConfirmEnd
  usecase "Complete Ride" as UC_Complete
  usecase "Process Payment" as UC_Pay
}

User --> UC_Register
User --> UC_Login
User --> UC_History

Passenger --> UC_Request
Passenger --> UC_ConfirmEnd

Driver --> UC_RegisterVehicle
Driver --> UC_SelectVehicle
Driver --> UC_Availability
Driver --> UC_Accept
Driver --> UC_Start
Driver --> UC_ConfirmEnd

UC_Request .> UC_Route : <<include>>
UC_Request .> UC_Search : <<include>>
UC_Request .> UC_Broadcast : <<include>>
UC_ConfirmEnd .> UC_Complete : <<include>>
UC_Complete .> UC_Pay : <<include>>

MapProvider --> UC_Route
MapProvider --> UC_Search
PaymentGateway --> UC_Pay
@enduml
```

## Notes

- `Passenger` and `Driver` are specialized actors that inherit shared behavior from `User`.
- Shared use cases: registration, login, and ride history.
- Passenger-specific and driver-specific actions remain separate.
