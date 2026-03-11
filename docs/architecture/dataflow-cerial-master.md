# Data Flow and Trust Boundaries — Cerial Master

```mermaid
flowchart TD
  Caller[Caller / Cerial Client]
  Service[CerialMasterService]
  Install[CerialMasterInstall]
  SystemSvc[Activity Master System Services]
  ResourceItemSvc[ResourceItemService]
  ClassificationSvc[ClassificationService]
  EventSvc[EventService]
  MutinySession[Mutiny Session / Vertx Persistence]
  Postgres[PostgreSQL]
  SerialPorts[Serial Port Hardware]

  Caller --> Service
  Service --> SerialPorts
  SerialPorts --> Service
  Service --> ResourceItemSvc
  Service --> ClassificationSvc
  ResourceItemSvc --> MutinySession
  ClassificationSvc --> MutinySession
  MutinySession --> Postgres

  Install --> SystemSvc
  Install --> ResourceItemSvc
  Install --> ClassificationSvc
  Install --> EventSvc
  SystemSvc --> MutinySession
  EventSvc --> MutinySession
```

Trust boundaries
- Hardware boundary: Serial port scanning uses jSerialComm; treat device input as untrusted. Validation/logging occurs in `CerialMasterService`.
- Persistence boundary: Mutiny/Hibernate Reactive sessions wrap all writes; identity tokens from Activity Master (`getISystemToken`) guard access.
- Dependency boundary: Activity Master services (resource, classification, events, systems) control authorization and schema; this module should not bypass them.

Dependency map
- Activity Master Core/Client: system discovery, tokens, resource/classification/events services.
- GuicedEE Vert.x Persistence: Mutiny sessions and database connectivity.
- jSerialComm / nrjavaserial: hardware enumeration and COM port handling.
- Cerial Master Client: provides `ComPortConnection` domain projection and timed sender helpers.
