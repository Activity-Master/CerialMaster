---
version: 2.0
date: 2025-12-07
title: Cerial Master — Human–AI Collaboration Pact
project: ActivityMaster / Cerial Master
authors: [ActivityMaster maintainers, Codex, AI Assistants]
---

# 🤝 Pact.md (v2)
### The Human–AI Collaboration Pact
*(Human × AI Assistant — “The Pact” Developer Edition)*

## 1. Purpose

This pact anchors how we collaborate on Cerial Master — an Activity Master addon that manages serial port configuration and lifecycle with GuicedEE, Vert.x 5, Hibernate Reactive/Mutiny, and the ActivityMaster FSDM. We document-first, diagram-first, and keep Rules ↔ Guides ↔ Implementation in lockstep. The run is under blanket approval; stage gates are recorded as auto-approved per policy.

> We don’t *vibe code* — we *vibe engineer* for serial connectivity in Activity Master.

## 2. Principles

### 🧭 Continuity
- Context persists across sessions; RULES.md (host) pins rules/rules.md §§4/5, Document Modularity, Forward-Only.
- Diagrams live under docs/architecture and stay in source (Mermaid-first).
- MCP: Mermaid MCP `https://mcp.mermaidchart.com/mcp` is the registered diagram server (see .mcp.json).

### 🪶 Finesse
- Java 25 LTS + Maven only; CRTP fluent strategy with Lombok @Log4j2.
- Naming and module labels follow the selected glossaries (topic-first).
- Logging defaults to Log4j2 with Lombok @Log4j2 (no other Lombok loggers).

### 🌿 Non-Transactional Flow
- Documentation-first by mandate: Stage 1–3 are docs-only; Stage 4 introduces code/config changes.
- Close loops: PACT ↔ GLOSSARY ↔ RULES ↔ GUIDES ↔ IMPLEMENTATION cross-link the same modules.

### 🔁 Closing Loops
- Each artifact cites its parent: RULES.md links to rules/ indexes; GUIDES.md points to chosen modular guides; IMPLEMENTATION.md references code paths and diagrams.
- Glossary precedence: topic glossaries override host terms; host GLOSSARY.md aggregates and routes.

## 3. Structure of Work

| Layer | Description | Artifact |
|-------|-------------|----------|
| Pact | Shared culture and run-specific constraints | `PACT.md` |
| Rules | Host rules linking to the Rules Repository topics | `RULES.md` |
| Guides | How-to flows mapped to selected stacks | `GUIDES.md` |
| Implementation | Code layout, status, and backlinks to guides | `IMPLEMENTATION.md` |

## 4. Behavioral Agreements

- Language: precise, technical English.
- Tone: friendly teammate; transparent about unknowns.
- Boundaries: no assumptions beyond observed repo code/config; legacy docs are treated as outdated unless validated.
- Attribution: humans + AI share authorship; note MCP usage when diagrams are generated.

## 5. Developer Culture: *Vibe Engineering for Cerial Master*

### 🧠 Practiced Through
- Tool literacy: GuicedEE modules, Mutiny, Hibernate Reactive, ActivityMaster FSDM classification/resource APIs.
- Meta-awareness: apply CRTP fluent strategy (no Lombok @Builder), respect JPMS exports/opens, and Log4j2 defaults.
- Documentation-as-code: Mermaid diagrams in docs/architecture with sources only; no binary assets committed.

### 💡 Motto
> Engineering the *vibe* means making the *craft* visible in serial port lifecycle, persistence, and telemetry.

## 6. Technical Commitments

- Format: Markdown everywhere; diagrams in Mermaid fenced blocks (no parentheses in node labels).
- Forward-only: remove/replace legacy anchors; update inbound links in the same change set.
- Source of truth: Selected Rules Repository topics drive behavior; host RULES.md enumerates them.
- Build/runtime: Java 25 LTS, Maven; reactive stack via Vert.x persistence + Mutiny; Log4j2 logging.
- CI/Env: GitHub Actions preferred; `.env.example` aligns to rules/generative/platform/secrets-config/env-variables.md.

## 7. Shared Goals

1. Document the current Cerial Master architecture (C4, sequences, ERD) from observable code/tests.
2. Align RULES/GLOSSARY/GUIDES to selected topics (Activity Master Core/Client/Cerial/Cerial Client, GuicedEE Vert.x Client/Cerial, Java 25, MapStruct, Lombok, JSpecify, Vert.x 5).
3. Plan implementation changes without fiction: map to existing modules and tests, note unknowns explicitly.
4. Keep telemetry, classification, and resource-item flows traceable through docs and diagrams.

## 8. Closing Note

> “We’ve moved beyond prompt engineering into *vibe engineering* — where the prompt is the tool, the conversation is the method, and the craft is the output.” — *Human × AI Assistant Collaboration, Cerial Master Edition*
