# Cursor Rules — Cerial Master

- Apply `rules/RULES.md` §§4/5, Document Modularity Policy, and Forward-Only Change Policy. Host docs stay outside `rules/`.
- Stage-gated workflow: Stages 1–3 are docs-only; Stage 4 permits code/config. Blanket approval noted for this run; retain STOP options for future checkpoints.
- Stack: Java 25 + Maven; Vert.x 5 + GuicedEE (client + cerial); Activity Master data (Core/Client/Cerial/Cerial Client); CRTP fluent API (no builders); Lombok `@Log4j2`; Log4j2 logging; MapStruct optional; JSpecify nullness.
- Glossary precedence: topic-first per `GLOSSARY.md`; use topic glossaries before host entries.
- Diagrams: Mermaid only, stored under `docs/architecture`; Mermaid MCP `https://mcp.mermaidchart.com/mcp`.
- Forward-only edits with updated links; avoid compatibility shims.
