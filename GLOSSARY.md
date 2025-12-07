# Glossary — Cerial Master (Topic-First)

Glossary precedence policy
- Topic glossaries override host definitions for their scope.
- Host glossary acts as an index and adds only project-specific anchors.
- When a term exists in multiple topics, defer to the most specific selected topic (e.g., GuicedEE Cerial over generic Activity Master).
- Prompt language alignment: no enforced component renames beyond CRTP vs Builder routing for fluent APIs (CRTP chosen; avoid `@Builder`).

Selected topic glossaries (authoritative)
- Java 25 — `rules/generative/language/java/GLOSSARY.md`
- Activity Master Core — `rules/generative/data/activity-master/GLOSSARY.md`
- Activity Master Client — `rules/generative/data/activity-master/client/GLOSSARY.md`
- Activity Master Cerial Client — `rules/generative/data/activity-master/cerial-client/GLOSSARY.md`
- GuicedEE (core) — `rules/generative/backend/guicedee/GLOSSARY.md`
- GuicedEE Client — `rules/generative/backend/guicedee/client/GLOSSARY.md`
- GuicedEE Cerial — `rules/generative/backend/guicedee/cerial/GLOSSARY.md`
- Fluent API (CRTP) — `rules/generative/backend/fluent-api/GLOSSARY.md`
- Lombok — `rules/generative/backend/lombok/GLOSSARY.md`
- MapStruct — `rules/generative/backend/mapstruct/GLOSSARY.md`
- JSpecify — `rules/generative/backend/jspecify/GLOSSARY.md`
- Testing (Jacoco, Java Micro Harness) — `rules/generative/platform/testing/GLOSSARY.md`

Project-specific anchors
- Cerial Master: Activity Master addon that manages serial port resources and classifications using GuicedEE/Mutiny. See `CerialMasterService` and `CerialMasterInstall`.
- SerialConnectionPort: Resource item type for COM ports; created in `CerialMasterInstall` and used in `CerialMasterService.addOrUpdateConnection`.
- ComPortConnection: Domain projection for a COM port connection (port number, type, status, baud rate, buffer size, data bits, stop bits, parity); populated from classifications in `CerialMasterService.findComPortConnection`.
- CerialMasterSystemName: Constant identifying the system within Activity Master; used to fetch system context and tokens.
- Classification keys: `ComPort`, `ComPortNumber`, `ComPortDeviceType`, `ComPortStatus`, `BaudRate`, `BufferSize`, `DataBits`, `StopBits`, `Parity`, `ComPortAllowedCharacters`, `ComPortEndOfMessage` — created during installation and applied in service flows.
- Event types: `SendMessageToComPort`, `Message`, `MessageReceivedFromComPort`, `RegisteredANewConnection`, `ClosedANewConnection` — provisioned in `CerialMasterInstall`.

LLM usage guidance
- Use CRTP-style fluent setters for new APIs; do not introduce builders.
- Default logging via Lombok @Log4j2; keep structured trace/debug/info messages consistent with existing patterns.
- When interpreting terms, prefer topic glossaries above; host entries are supplemental.
