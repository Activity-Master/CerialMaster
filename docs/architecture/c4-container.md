# C4 Level 2 — Containers

Containers in this library-centric deployment.

```mermaid
graph TD
  ClientApp[Cerial Client / Activity Master callers]
  CerialMasterLib[Cerial Master Library (GuicedEE module)]
  ActivityMasterCore[Activity Master Core + FSDM Services]
  VertxPersistence[Vertx Persistence + Mutiny Sessions]
  Postgres[PostgreSQL]
  SerialHardware[Serial Port Hardware]

  ClientApp --> CerialMasterLib
  CerialMasterLib --> ActivityMasterCore
  CerialMasterLib --> VertxPersistence
  VertxPersistence --> Postgres
  CerialMasterLib --> SerialHardware
  SerialHardware --> CerialMasterLib
```

Notes
- The library is loaded via JPMS `com.guicedee.activitymaster.cerialmaster` and Guice (`CerialMasterModule`, `CerialMasterInclusionModule`).
- Persistence flows use Mutiny reactive sessions supplied by Activity Master’s Vert.x persistence module.
- Serial port enumeration uses jSerialComm (`com.fazecast.jSerialComm.SerialPort`) while persisted configuration is stored via Activity Master resource/classification services.
