# IMPLEMENTATION — Cerial Master

Context
- Module: `com.guicedee.activitymaster.cerialmaster` (JPMS) with Guice bindings (`CerialMasterModule`, `CerialMasterInclusionModule`, `CerialMasterGuiceConfig`).
- Core services: `CerialMasterService` (ICerialMasterService), `CerialMasterInstall` (ISystemUpdate), `CerialMasterSystem` (IActivityMasterSystem).
- Domain enums: `services/enumerations/*` define classification keys, event types, and resource item types.
- Dependencies (pom.xml): Activity Master (`activity-master`), Cerial Master Client, nrjavaserial, tests: testcontainers, junit. Logging uses Log4j2 via Lombok.
- Diagrams: see `docs/architecture/README.md` for C4, sequences, ERD, data flow.

Current file layout
- `src/main/java` — service implementation, Guice wiring, JPMS module-info.
- `src/main/resources/META-INF/services` — SPI registrations (Guice lifecycle, scan inclusions, Activity Master system).
- `src/test/java` — Mutiny/Testcontainers-based integration tests for enterprise setup, COM port flows, timed sender behaviors.
- `src/test/resources` — persistence.xml, SQL seeds for Postgres test container, cache config.

Scaffolding and config plan (forward-only)
- Environment sample: add `.env.example` aligned to `rules/generative/platform/secrets-config/env-variables.md` with Activity Master database, auth, and testing toggles.
- CI: add minimal GitHub Actions workflow reference for Maven build/test; document required secrets (no shared workflow auto-adoption without maintainer approval).
- AI workspace: add `.aiassistant/rules/`, `.github/copilot-instructions.md`, `.cursor/rules.md` summarizing RULES §4/5, Document Modularity, Forward-Only, stage-gate policy.
- README: update to reflect rules adoption, link PACT/RULES/GUIDES/IMPLEMENTATION/GLOSSARY, submodule usage, and diagrams.
- Migration notes: capture any removed/updated docs in `MIGRATION.md` (forward-only, no legacy anchors).

Build/annotation wiring
- Java 25 + Maven; annotation processors provided by Lombok/MapStruct if added. Keep JPMS exports/opens consistent when adding new packages.
- Reactive persistence via Mutiny/Hibernate Reactive supplied by Activity Master/Vertx persistence; no direct JDBC usage.
- Logging configuration should be Log4j2-aligned; avoid mixing logging frameworks.

Rollout and validation
- Validate installer flows with Mutiny session + Activity Master token (existing tests cover enterprise setup and port registration). Extend or add micro-harness tests for new behaviors.
- Confirm `.env.example` variables map to testcontainer defaults and Activity Master configuration needs.
- CI gate: Maven test + coverage; ensure workflows respect JPMS module naming and submodule checkout for `rules/`.

Backlinks
- Rules: `RULES.md`
- Guides: `GUIDES.md`
- Glossary: `GLOSSARY.md`
- Diagrams: `docs/architecture/README.md`
- Prompt reference: `docs/PROMPT_REFERENCE.md`
