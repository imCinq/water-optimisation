# Implementation Plan

## Phase 0 — Baseline

Create repeatable test scenes and record average FPS, 1% lows, long-tail frame time, chunk compilation time, translucent resort time, water blocks and faces emitted, particle counts, backend, render distance, resolution, Java, and companion mods.

Deliverable: a baseline report with no optimisation enabled.

## Phase 1 — Scaffold, configuration, and Mod Menu

Add the smallest buildable Fabric 26.2 client mod. Add local configuration, a native Minecraft settings screen, simple profiles, an opt-in diagnostics overlay, and a separate optional Mod Menu adapter.

Acceptance criteria:

- builds with Java 25;
- Mod Menu provides a Configure button when installed;
- the mod loads and remains usable without Mod Menu;
- the main screen exposes only the master switch and profile;
- Advanced settings are separate and clearly labelled;
- Done, Cancel, Escape, reset, and invalid-config recovery work;
- no server/network code;
- disabled mode is behaviorally unchanged;
- diagnostics contain no personal or server identifiers.

## Phase 2 — Particle filtering

Add optional water-particle distance and fog filtering. Keep nearby particles prioritized and provide a clear visual trade-off in settings.

Acceptance criteria:

- no changes to fluid state or particle physics;
- underwater and near-player effects remain understandable;
- measurable reduction in particle work in stress scenes;
- particle settings are understandable from the UI without technical knowledge.

## Phase 3 — Conservative face culling

Cull only faces proven hidden by identical fluid or fully occluding block shapes. Preserve overlays and waterlogged geometry.

Acceptance criteria:

- flat oceans, waterfalls, caves, leaves, stairs, doors, and transparent blocks match the reference within documented limitations;
- no missing surface planes in flooded caves;
- measurable reduction in emitted translucent geometry;
- Balanced profile remains conservative and stable.

## Phase 4 — Flat source-water fast path

Detect uniform source-water regions and use precomputed geometry only when every required condition is satisfied.

Acceptance criteria:

- irregular levels and flowing edges fall back automatically;
- no visible changes in the reference scenes;
- section compilation time improves under a water-heavy workload;
- the feature is off in Balanced and explicitly labelled in Advanced settings until validated.

## Phase 5 — Sodium compatibility

Test with Sodium present and absent. If Sodium owns fluid compilation, avoid duplicate work and limit the mod to supported extension points or diagnostics.

Acceptance criteria:

- no crashes or duplicate geometry;
- no forced renderer replacement;
- behavior is documented for each tested combination;
- Mod Menu configuration remains usable in both combinations.

## Phase 6 — Release review

Review DonutSMP rules, privacy, client-only audits, benchmark limitations, version metadata, UI wording, and distribution wording before any public artifact is created.
