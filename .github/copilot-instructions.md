# Copilot Instructions — Cerial Master

- Follow `rules/RULES.md` §§4/5, Document Modularity Policy, and Forward-Only Change Policy; do not place project docs inside `rules/`.
- Workflow is documentation-first and stage-gated (Stages 1–3 docs, Stage 4 code/config). Blanket approval recorded for this run; keep STOP options available for future approvals.
- Stack: Java 25 + Maven; Vert.x 5 + GuicedEE (client + cerial); Activity Master data (Core/Client/Cerial/Cerial Client); CRTP fluent strategy (no builders); Lombok `@Log4j2`; Log4j2 logging; MapStruct optional; JSpecify nullness.
- Glossary precedence: topic-first per `GLOSSARY.md`; use topic glossaries before host terms.
- Diagrams: Mermaid only, stored under `docs/architecture`; use Mermaid MCP endpoint `https://mcp.mermaidchart.com/mcp`.
- Forward-only edits: update all inbound links when renaming/removing; avoid legacy shims or duplicate anchors.
