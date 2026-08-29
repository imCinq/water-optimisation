# Implementation Plan

Remote-first Codex development is evaluated throughout this plan. It is a delivery method, not a replacement for local Minecraft validation.

## Cross-cutting workstream — Remote-first Codex evaluation

The goal is to determine how much of the project Codex can complete without using storage or Java/Gradle processes on the Mac.

### Remote tasks

Codex should be able to:

- create and modify the Fabric project in a temporary remote workspace;
- create branches and commits directly against the private GitHub repository;
- run Java, Gradle, unit tests, repository audits, and build tasks remotely;
- inspect build output and dependency changes;
- produce a temporary test JAR without committing generated artifacts;
- update documentation and issue plans.

### Local-only tasks

The user should only need to run the final client locally for:

- M2 GPU and macOS graphics-backend behavior;
- real frame-time and FPS measurements;
- visual comparison in the exact Minecraft instance;
- Sodium/resource-pack/modpack interaction;
- DonutSMP connection and server-rule smoke testing.

### Evaluation checklist

- [ ] Create a Codex feature branch without cloning to the Mac.
- [ ] Add the minimal Fabric 26.2 scaffold remotely.
- [ ] Run the remote build and unit tests successfully.
- [ ] Run repository privacy and client-only audits remotely.
- [ ] Review the diff and commit only source, tests, docs, and configuration.
- [ ] Produce a temporary JAR without adding build output to Git.
- [ ] Confirm the Mac needs no Java or Gradle installation for the workflow.
- [ ] Download only the JAR required for local Minecraft testing.
- [ ] Record which results are remote and which are M2/DonutSMP-specific.
- [ ] Decide whether remote-first remains the default after the first implementation cycle.

### Known limitations

Remote builds may use different operating-system, Java, GPU, driver, and dependency-cache conditions. They can prove compilation and automated behavior but cannot prove M2 rendering performance or server acceptance. Remote work also uses Codex execution time and requires regular commits because a temporary workspace may be discarded.

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
