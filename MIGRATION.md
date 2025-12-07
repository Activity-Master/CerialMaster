# MIGRATION — Forward-Only Notes

- Existing README.md content is superseded by the new RULES/GUIDES/IMPLEMENTATION/diagrams; update inbound links accordingly (no legacy anchors preserved).
- Rules Repository submodule (`rules/`) is authoritative for enterprise rules; host-specific docs now live at the repo root and under `docs/`.
- No source code changes introduced yet; future code changes must respect JPMS exports/opens and CRTP fluent strategy (no builders).
- Keep `.env.example` and CI workflow additions aligned to Activity Master/GuicedEE expectations; remove outdated env placeholders if found during adoption.
