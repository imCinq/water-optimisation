# Roadmap

Note: the 0.0.7 baseline, 0.0.8 candidate, and 0.0.10 release below are historical; v1.0.0 is the current release.
See README.md, docs/DISTRIBUTION.md, and docs/FABRIC_26_3.md for current support.

Water Optimisation is developed as a conservative, client-only rendering mod. Every optimization must be measurable, visually reviewable, and safe to disable.

## v1.0.0 direction

The active release line is moving to three intended targets:

- Fabric 26.3;
- Fabric 1.21.11;
- NeoForge 26.3, once its development beta and build tooling stabilise.

Fabric 26.2, Fabric 1.21.1, and NeoForge 26.2 are historical compatibility targets only. Their existing artifacts remain documented for users of 0.0.10, but new v1.0.0 features and UI work should not be designed around them.

The implementation order is:

1. finish the isolated 1.21.11 build/API baseline and make 26.3 the default development profile;
2. keep one shared settings-presentation model so Fabric and NeoForge expose the same meanings;
3. finish the main screen with clear preset descriptions, a derived Custom state, capability-aware status text, and preserved Apply/Cancel behavior;
4. finish the Advanced screen with the two player-facing rendering toggles, Sodium/target unavailable states, and a bounded scrollable layout for small windows;
5. port the same presentation and entrypoint behavior to NeoForge 26.3 after its stable tooling is available;
6. only then decide whether a new rendering feature is justified by a clear correctness case. No Sodium geometry bridge or custom water renderer is part of v1.0.0 by default.

The first four settings/UI steps are now implemented in source: the 1.21.11 profile compiles, 26.3 is the default target, the shared presentation rules drive all current adapters, and the Advanced screen has bounded scrolling with a fixed footer. The next version-specific work is the NeoForge 26.3 port after its toolchain stabilises.

Testing is not a prerequisite for continuing this structural work. Builds and focused source checks remain the acceptance gate for compatibility changes; optional in-game smoke or performance checks can be performed later by the user without turning an FPS result into a promise.

## Current release baseline

The 0.0.7 release is the public baseline for the client-only implementation for Minecraft 26.2 and the target-isolated compatibility implementation for Minecraft 1.21.1:

- local configuration and native settings screens;
- opt-in particle filtering;
- vanilla same-fluid face decisions with no duplicate face hook;
- a fully hidden source-water fast path;
- an explicitly opt-in vanilla reduced-face mode;
- a Maximum FPS profile that enables the reduced-face mode together with the fast path;
- grouped native settings screens with plain-language controls and responsive layout;
- section and translucent-resort diagnostics;
- Sodium renderer-ownership protection;
- automated tests and repository audits.
- packaged Fabric mod icon matching the public project logo.

The 0.0.7 release packages both target-specific builds from the same reviewed source line. Remote build, test, privacy, client-only, and artifact checks pass. Local visual, performance, backend, companion-mod, and multiplayer measurements remain evidence work for the target hardware and exact modpack.

The Minecraft 1.21.1 profile uses Java 21, remapping Loom, official Mojang mappings, and target-isolated client sources. Its geometry path is intentionally more conservative, and Sodium remains the permanent geometry owner when present; no Sodium geometry bridge is planned unless project scope is formally reconsidered.

## v0.0.8 candidate scope

The current candidate keeps the rendering proof and compatibility boundary unchanged while tightening the implementation around measurable overhead and truthful diagnostics:

- remove disabled-path observer, thread-local, and unnecessary particle-budget work;
- bind all fluid diagnostics for one invocation to its entry counter generation;
- invalidate HUD lines immediately after diagnostics resets and configuration changes;
- distinguish configured, effective, observed, and actually skipped fast-path state;
- preserve the 1.21.1 center-source and upward-neighbor early rejection before the remaining conservative probe;
- document the difference between 26.2 reused renderer locals and 1.21.1 explicit neighbor reads;
- require remote dual-target CI plus exact-artifact runtime and visual validation before publication.

## Active Phase 1–3 extension

This pass keeps the original conservative phases intact while adding measurable controls around them:

- version the local configuration and migrate older files without changing explicit user choices;
- resolve requested settings into renderer capabilities and expose the effective path in the UI and diagnostics;
- add bounded water-particle admission, including a per-client-tick budget and an explicit policy for particles that normally bypass distance limits;
- keep experimental GPU/fill-rate work out of the release path until a separate water-owned design has repeatable visual and frame-time evidence.

## Completed phases

### Phase 0 — Instrumentation

Opt-in counters and timings cover fluid tessellation, section compilation, translucent resorting, fully hidden fast-path skips, removed reverse faces, and particle filtering. Total face counts remain an external Tracy or mesh-statistics metric so instrumentation does not add a callback to every vanilla face decision. Benchmark templates define repeatable scenes and metrics.

### Phase 1 — Configuration and UI

Native Minecraft screens, presets, grouped Advanced settings, atomic JSON persistence, invalid-file recovery, a keybind, and optional Mod Menu integration are implemented.

### Phase 2 — Particle filtering

Water-specific particle admission can use camera-relative distance, with a lifecycle-safe player fallback and conservative fog/distance tightening. Fluid state, particle physics, and non-water particles are untouched.

### Phase 3 — Conservative fluid visibility

Only exact ordinary full source-water cases with fully hidden faces can be forced hidden. Flowing, partial, waterlogged, overlay, transparent, and ambiguous states use vanilla behavior.

### Phase 4 — Interior source-water fast path

Only ordinary source-water blocks whose six neighboring faces are hidden by ordinary source-water blocks or full solid-rendering blocks can skip fluid tessellation.

### Phase 5 — Renderer compatibility

Sodium ownership detection disables the vanilla fluid hooks rather than replacing or duplicating another renderer.

### Phase 6 — Experimental reduced faces

The reduced-face mode keeps vanilla's outward fluid face and removes only its optional reverse face for ordinary full source-water blocks. Flowing and waterlogged states stay on vanilla. It is available manually and in the Maximum FPS profile because inside-water and unusual transparency views can change. It is disabled when Sodium owns fluid rendering; no Sodium geometry bridge is planned, and Sodium remains the permanent geometry owner unless project scope is formally reconsidered.

## Next priorities

- Revisit a separate water-owned GPU/fill-rate path only after a new design has a correctness proof and a repeatable win; the previous prototype was removed after visual failures.
- Complete local visual and performance validation.
- Do not add a Sodium geometry bridge; issue #30 is closed as not planned. Sodium remains the permanent geometry owner unless project scope is formally reconsidered.
- Add direct tests if the fluid classifier expands beyond the exact source-water subset.
- Keep diagnostics generation and hook-observation state truthful across reset, toggle, and target changes.
- Measure the reduced-face experiment on the target hardware, including underwater and transparent-boundary scenes.
- Keep camera-relative water-distance fading deferred until a separate water-owned pass is independently proven safe and useful.
- Re-evaluate broader shape-aware culling only after measurements and visual tests justify it.

## Release gate

A stable release requires a tagged, audited artifact; documented benchmark results; disabled-mode comparison; visual checks across water-heavy scenes; backend and companion-mod checks; and a client-only multiplayer smoke test with current server-rule review.
