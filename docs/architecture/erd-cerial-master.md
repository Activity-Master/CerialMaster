# ERD — Serial Connection Model

```mermaid
erDiagram
  Enterprise ||--o{ System : owns
  System ||--o{ ResourceItemType : defines
  ResourceItemType ||--o{ ResourceItem : instantiates
  ResourceItem ||--o{ ClassificationValue : stores
  ResourceItem ||--|| ComPortConnection : projects
  System ||--o{ EventType : publishes

  Enterprise {
    uuid id
    string name
  }

  System {
    uuid id
    string name
    string description
  }

  ResourceItemType {
    uuid id
    string name
    string description
  }

  ResourceItem {
    uuid id
    string value
    uuid typeId
  }

  ClassificationValue {
    uuid id
    string key
    string value
  }

  ComPortConnection {
    uuid id
    int comPort
    string comPortType
    string comPortStatus
    int baudRate
    int bufferSize
    int dataBits
    int stopBits
    string parity
  }

  EventType {
    uuid id
    string name
    string description
  }
```

Mapping to code
- Enumerations defining classification keys and resource item types: `services/enumerations/*`.
- Resource item and classification creation flows: `CerialMasterInstall`, `CerialMasterService.addOrUpdateConnection`.
- Event types created during installation: `CerialMasterInstall` (RegisteredANewConnection, ClosedANewConnection, message events).
- ComPortConnection domain projection: `com.guicedee.activitymaster.cerialmaster.client.ComPortConnection` populated in `CerialMasterService.findComPortConnection`.
