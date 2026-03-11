# Sequence — List Available COM Ports

Flow traced from `CerialMasterService.listAvailableComPorts`.

```mermaid
sequenceDiagram
  participant Caller
  participant Service as CerialMasterService
  participant SerialPorts as Serial Port Hardware
  participant SystemSvc as ActivityMasterSystemSvc
  participant ResourceItemSvc as ResourceItemService

  Caller->>Service: listAvailableComPorts(session, enterprise)
  Service->>SerialPorts: scan system ports (cached jSerialComm)
  SerialPorts-->>Service: [COMx...]
  Service->>SystemSvc: getISystem(session, CerialMasterSystemName)
  Service->>SystemSvc: getISystemToken(session, CerialMasterSystemName)
  Service->>ResourceItemSvc: findByClassificationAll(SerialConnectionPort, ComPortNumber)
  ResourceItemSvc-->>Service: registered COM numbers
  Service-->>Caller: availablePorts = scanned - registered
```

Notes
- Hardware scan caches results in-memory (`comStrings` list) until process restart.
- Registered ports are derived from Activity Master resource items classified with `SerialConnectionPort` and `ComPortNumber`.
- All lookups use the caller-provided Mutiny session to stay within the same transaction.
