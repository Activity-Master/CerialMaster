# Sequence — Add or Update COM Port

Flow traced from `CerialMasterService.addOrUpdateConnection` using a provided Mutiny session and Activity Master system token.

```mermaid
sequenceDiagram
  participant Caller
  participant Service as CerialMasterService
  participant ResourceItemSvc as ResourceItemService
  participant ClassificationSvc as ClassificationService
  participant SystemSvc as ActivityMasterSystemSvc
  participant SerialPorts as Serial Port Hardware

  Caller->>Service: addOrUpdateConnection(session, comPort, system, token)
  Service->>ResourceItemSvc: findResourceItemType(SerialConnectionPort)
  ResourceItemSvc-->>Service: resourceItemType
  Service->>ResourceItemSvc: create(resource item with ComPort)
  ResourceItemSvc-->>Service: resourceItem persisted
  Service->>ClassificationSvc: add ComPort + ComPortNumber + DeviceType + Status
  Service->>ClassificationSvc: add BaudRate + BufferSize + DataBits + StopBits + Parity
  ClassificationSvc-->>Service: classifications applied
  Service-->>Caller: ComPortConnection with id and classifications
```

Notes
- Failure handling is logged via Log4j2; invalid comPort inputs short-circuit with UnsupportedOperationException.
- Hardware enumeration is implicit: com port identity comes from `ComPortConnection` supplied by caller (created via jSerialComm discovery).
- All persistence is executed with the provided Mutiny session to share a transaction boundary with upstream workflows.
