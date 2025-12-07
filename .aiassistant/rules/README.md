# AI Assistant Workspace Rules — Cerial Master

- Pin `rules/RULES.md` §§4 (Behavioral), 5 (Technical), Document Modularity Policy, and Forward-Only Change Policy. No host docs inside `rules/`.
- Workflow: documentation-first, stage-gated (Stages 1–3 docs, Stage 4 code/config). Blanket approval noted for this run; still offer STOP options when approvals are required.
- Stack: Java 25 + Maven; Vert.x 5 + GuicedEE (client + cerial); Activity Master Core/Client/Cerial/Cerial Client; CRTP fluent strategy (no builders); Lombok `@Log4j2`; Log4j2 logging; MapStruct optional; JSpecify nullness.
- Glossary precedence: topic-first per `GLOSSARY.md`; defer to selected topic glossaries before host definitions.
- Diagrams: Mermaid only, stored under `docs/architecture`; use Mermaid MCP `https://mcp.mermaidchart.com/mcp`.
- Forward-only: replace legacy anchors, update inbound links in the same change set, no compatibility shims.
