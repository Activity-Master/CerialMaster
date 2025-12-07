# Cerial Master

Addon for Activity Master that manages serial port configuration, persistence, and lifecycle using GuicedEE, Vert.x 5, and Hibernate Reactive/Mutiny.

## Documentation
- Pact — `PACT.md`
- Rules — `RULES.md`
- Guides — `GUIDES.md`
- Implementation — `IMPLEMENTATION.md`
- Glossary — `GLOSSARY.md`
- Architecture diagrams — `docs/architecture/README.md`
- Prompt reference — `docs/PROMPT_REFERENCE.md`
- Migration notes — `MIGRATION.md`

## Rules Repository
- Submodule: `rules/` (`https://github.com/GuicedEE/ai-rules.git`)
- Update: `git submodule update --init --recursive`
- Do not place host project docs inside `rules/`; host docs live at the repo root or under `docs/`.

## Stack
- Java 25 LTS + Maven
- GuicedEE (client + cerial) on Vert.x 5; Activity Master Core/Client/Cerial/Cerial Client data model
- CRTP fluent API strategy (no builders); Lombok `@Log4j2`; Log4j2 logging; MapStruct; JSpecify

## Environment and CI
- Sample env vars: `.env.example` (aligned to `rules/generative/platform/secrets-config/env-variables.md`)
- CI: GitHub Actions workflow `/.github/workflows/maven.yml` (Java 25, `mvn -B -ntp test`)

## Stage-Gated Workflow
- Documentation-first: Stages 1–3 are docs-only; Stage 4 introduces code/config. Blanket approval for this adoption run is recorded, but STOP options remain available for future work.
