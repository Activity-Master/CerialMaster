# C4 Level 3 — Cerial Master Components

```mermaid
graph TD
  CerialMasterService[CerialMasterService (ICerialMasterService)]
  ResourceItemSvc[ResourceItemService]
  ClassificationSvc[ClassificationService]
  EventSvc[EventService]
  SystemSvc[SystemsService]
  SerialPorts[Serial Port Hardware]
  ComPortConnection[ComPortConnection domain object]

  CerialMasterService --> ResourceItemSvc
  CerialMasterService --> ClassificationSvc
  CerialMasterService --> ComPortConnection
  CerialMasterService --> SerialPorts
  CerialMasterService --> SystemSvc

  CerialMasterInstall[CerialMasterInstall (ISystemUpdate)]
  CerialMasterInstall --> ClassificationSvc
  CerialMasterInstall --> ResourceItemSvc
  CerialMasterInstall --> EventSvc
  CerialMasterInstall --> SystemSvc

  CerialMasterSystem[CerialMasterSystem (IActivityMasterSystem)]
  CerialMasterSystem --> SystemSvc

  CerialMasterModule[CerialMasterModule (Guice bindings)]
  CerialMasterModule --> CerialMasterService

  CerialMasterGuiceConfig[CerialMasterGuiceConfig (scan config)]
  CerialMasterInclusionModule[CerialMasterInclusionModule (module inclusion)]
```

Evidence
- Service logic: `src/main/java/com/guicedee/activitymaster/cerialmaster/CerialMasterService.java`
- System registration: `CerialMasterSystem`, `CerialMasterInstall`
- DI wiring: `CerialMasterModule`, `CerialMasterGuiceConfig`, `CerialMasterInclusionModule`, `module-info.java`
- Domain enumerations: `services/enumerations/*`
