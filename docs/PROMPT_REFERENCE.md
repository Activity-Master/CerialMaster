# Prompt Reference — Cerial Master

Load this file (plus PACT/RULES/GLOSSARY/GUIDES/IMPLEMENTATION) before running any future AI prompts for this repository.

- MCP servers: Mermaid MCP `https://mcp.mermaidchart.com/mcp` (`type: http`) registered for diagrams (see `.mcp.json`).
- Stage gates: Documentation-first. Blanket approval granted for this run; record auto-approvals, but keep STOP options in replies when approvals are not waived.
- Java LTS: Java 25 + Maven (no substitutions).
- Fluent strategy: CRTP (no Lombok @Builder); Lombok @Log4j2 for logging.
- Logging: Log4j2 by default.
- Architecture: SDD + DDD + TDD (docs-first, test-first); Vert.x 5 + Hibernate Reactive/Mutiny via GuicedEE.
- Data domains: Activity Master Core/Client/Cerial/Cerial Client (topic-first glossaries); resource/classification model drives persistence.
- Reactive stack: GuicedEE Vert.x client + GuicedEE Cerial.
- Structural: MapStruct, Lombok, Logging, JSpecify.
- Testing: Jacoco, Java Micro Harness.
- CI/CD: GitHub Actions preferred provider.

Diagram index
- C4 Context — `docs/architecture/c4-context.md`
- C4 Container — `docs/architecture/c4-container.md`
- C4 Component — `docs/architecture/c4-component-cerial-master.md`
- Sequences — `docs/architecture/sequence-add-or-update-com-port.md`, `docs/architecture/sequence-list-available-com-ports.md`
- ERD — `docs/architecture/erd-cerial-master.md`

Loop closures
- Pact — `PACT.md`
- Glossary precedence — `GLOSSARY.md` (topic-first, enforced precedence documented)
- Rules — `RULES.md` (links to Rules Repository topics)
- Guides — `GUIDES.md`
- Implementation overview — `IMPLEMENTATION.md`
