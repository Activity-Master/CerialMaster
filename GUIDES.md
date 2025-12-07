# GUIDES — Applying the Rules

Use these guides alongside `RULES.md`, `GLOSSARY.md`, and the diagrams in `docs/architecture/`.

Architecture and flow
- Review C4 and sequences: `docs/architecture/c4-context.md`, `c4-container.md`, `c4-component-cerial-master.md`, `sequence-add-or-update-com-port.md`, `sequence-list-available-com-ports.md`.
- Data and trust boundaries: `docs/architecture/dataflow-cerial-master.md`, `erd-cerial-master.md`.

Backend and data guides
- Activity Master FSDM usage: `rules/generative/data/activity-master/README.md`, `.../core/README.md`, `.../client/README.md`, `.../cerial-client/README.md`.
- GuicedEE core/client/cerial patterns: `rules/generative/backend/guicedee/README.md`, `.../client/README.md`, `.../cerial/README.md`, Vert.x bridge — `rules/generative/backend/guicedee/vertx/README.md`.
- Reactive flows and persistence: Vert.x/Hibernate Reactive guides — `rules/generative/backend/vertx/README.md`, `rules/generative/backend/vertx/vertx-5-transaction-handling.md`, `rules/generative/backend/vertx/vertx-5-postgres-client.md`.
- Fluent API CRTP usage: `rules/generative/backend/fluent-api/crtp.rules.md`; avoid builders.
- Logging and Lombok: `rules/generative/backend/logging/README.md`, `rules/generative/backend/lombok/README.md` (use `@Log4j2`).
- MapStruct: `rules/generative/backend/mapstruct/README.md` (apply when adding mappers).
- Nullness: `rules/generative/backend/jspecify/README.md`.

Build, test, CI
- Java 25 build rules: `rules/generative/language/java/java-25.rules.md`, `rules/generative/language/java/build-tooling.md`.
- Testing: `rules/generative/platform/testing/java-micro-harness.rules.md`, `rules/generative/platform/testing/jacoco.rules.md`; align reactive tests with Mutiny sessions and Testcontainers examples in `src/test/java`.
- CI/CD: `rules/generative/platform/ci-cd/README.md`, GitHub Actions provider — `rules/generative/platform/ci-cd/providers/github-actions.md`.

-Operational guidance (library)
- Installer defaults and system registration live in `CerialMasterInstall` and `CerialMasterSystem`; extend them with new classifications or event types using the provided Mutiny session.
- Service usage: `CerialMasterService` expects callers to pass a Mutiny session and Activity Master system token; COM port discovery uses jSerialComm and caches results.
- Keep JPMS/Guice bindings updated in `module-info.java` and `CerialMasterModule` for any new public services.

API surface (current)
- `addOrUpdateConnection(session, ComPortConnection, system, token)` — create/update COM port resource item + classifications.
- `updateStatus(session, ComPortConnection, system, token)` — update status classification.
- `findComPortConnection(session, ComPortConnection, system, token)` — populate connection from stored classifications.
- `getComPortConnection(session, comPort, enterprise, timedConfig?)` — retrieve or register server COM port and timed sender.
- `getScannerPortConnection(session, comPort, enterprise, timedConfig?)` — retrieve scanner COM port and timed sender.
- `getComPortConnectionDirect(comPort)` — direct connection with default timed sender (no persistence).
- `listComPorts()` — scan OS ports (cached).
- `listRegisteredComPorts(session, enterprise)` — fetch registered COM port resource items.
- `listAvailableComPorts(session, enterprise)` — diff between scanned and registered ports.

Acceptance and validation checklist
- New behaviors documented with updates to diagrams and glossary entries.
- Reactive flows tested with Mutiny and Testcontainers when persistence is involved.
- Logging retains structured trace/debug/info/error messages; no blocking calls inside reactive chains.
- CI workflows updated when build or testing surfaces change.
