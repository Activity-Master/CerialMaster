# RULES — Cerial Master (Host Project)

Scope and policy
- Follow `rules/RULES.md` §§4/5, Document Modularity Policy, and Forward-Only Change Policy; no host docs inside the `rules/` submodule.
- Documentation-first, stage-gated workflow (Stages 1–3 docs, Stage 4 code/config). Blanket approval for this run is recorded; keep STOP options available for future runs.
- Cross-link loop: `PACT.md` ↔ `GLOSSARY.md` ↔ this RULES ↔ `GUIDES.md` ↔ `IMPLEMENTATION.md` ↔ diagrams under `docs/architecture`.

Selected stacks and rule anchors
- Language/Build: Java 25 LTS — `rules/generative/language/java/java-25.rules.md`, Maven build wiring — `rules/generative/language/java/build-tooling.md`.
- Architecture: DDD — `rules/generative/architecture/ddd/README.md`; TDD (docs-first/test-first) — `rules/generative/architecture/tdd/README.md`.
- Reactive backend: Vert.x 5 — `rules/generative/backend/vertx/README.md`; GuicedEE core/client — `rules/generative/backend/guicedee/README.md`, `rules/generative/backend/guicedee/client/README.md`, Vert.x bridge — `rules/generative/backend/guicedee/vertx/README.md`; Cerial domain — `rules/generative/backend/guicedee/cerial/README.md`.
- Activity Master data: Core/Client/Cerial/Cerial Client — `rules/generative/data/activity-master/README.md`, `rules/generative/data/activity-master/core/README.md`, `rules/generative/data/activity-master/client/README.md`, `rules/generative/data/activity-master/cerial-client/README.md`.
- Structural: Fluent API (CRTP) — `rules/generative/backend/fluent-api/crtp.rules.md`; Lombok — `rules/generative/backend/lombok/README.md`; MapStruct — `rules/generative/backend/mapstruct/README.md`; Logging — `rules/generative/backend/logging/README.md`; JSpecify — `rules/generative/backend/jspecify/README.md`.
- Testing: Jacoco — `rules/generative/platform/testing/jacoco.rules.md`; Java Micro Harness — `rules/generative/platform/testing/java-micro-harness.rules.md`.
- CI/CD: Platform CI/CD — `rules/generative/platform/ci-cd/README.md`; GitHub Actions provider — `rules/generative/platform/ci-cd/providers/github-actions.md`.

Fluent API and Lombok
- Fluent strategy is CRTP only; avoid builders and Lombok @Builder. Fluent setters must return `(J) this` with `@SuppressWarnings("unchecked")` where needed.
- Logging uses Lombok `@Log4j2`; avoid other Lombok log annotations. Respect Log4j2 configuration defaults.

JPMS/DI rules
- Keep module exports/opens aligned with `module-info.java` (exports `implementations`, `services`, `services.enumerations`, root package). Add new packages explicitly.
- Guice bindings live in `CerialMasterModule`; include new services via private module exposure and register lifecycle/configuration providers as needed (IGuiceModule/IGuiceConfigurator/IGuiceScanModuleInclusions).
- Activity Master system hooks: `CerialMasterSystem` (IActivityMasterSystem) and `CerialMasterInstall` (ISystemUpdate) drive defaults; reuse Mutiny sessions from callers for all persistence.

Persistence and data rules
- Resource/classification/event operations must route through Activity Master services (no direct SQL). All writes use provided Mutiny sessions.
- Classification keys and resource item types are defined in `services/enumerations/*`; extend via new enums plus installer updates, keeping classification concepts aligned to Activity Master glossaries.
- Hardware enumeration via jSerialComm/nrjavaserial should be side-effect free unless explicitly persisted.

Testing and CI
- Tests should use reactive patterns with Mutiny and leverage Java Micro Harness where applicable. Jacoco coverage reporting should remain enabled/updated per testing rules.
- CI defaults to GitHub Actions; reference shared workflow templates but prefer lightweight module-specific workflows unless maintainer approves shared pipeline adoption.

Glossary and diagrams
- Topic-first glossary precedence is documented in `GLOSSARY.md`; defer to topic glossaries before host definitions.
- Architecture diagrams live under `docs/architecture/` (Mermaid sources only) and are indexed in `docs/architecture/README.md`.
