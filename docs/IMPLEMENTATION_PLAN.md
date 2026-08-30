# Roadmap

Water Optimisation is developed as a conservative, client-only rendering mod. Every optimization must be measurable, visually reviewable, and safe to disable.

## Current preview

The 0.1.0-preview.6 build contains the first GPU-focused experiment for Minecraft 26.2:

- local configuration and native settings screens;
- opt-in particle filtering;
- vanilla same-fluid face decisions with no duplicate face hook;
- a fully hidden source-water fast path;
- an explicitly opt-in vanilla reduced-face mode;
- grouped native settings screens with renderer ownership status;
- section and translucent-resort diagnostics;
- Sodium renderer-ownership protection;
- automated tests and repository audits.

The implementation is build-verified. Local visual, performance, backend, companion-mod, and multiplayer validation remain part of release acceptance.

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

The Experimental culling mode keeps vanilla's outward fluid face and removes only its optional reverse face for ordinary full source-water blocks. Flowing and waterlogged states stay on vanilla. It is deliberately not part of the safe profiles because inside-water and unusual transparency views can change. It is disabled when Sodium owns fluid rendering.

## Next priorities

- Complete local visual and performance validation.
- Add direct tests if the fluid classifier expands beyond the exact source-water subset.
- Consider rolling diagnostics and fallback reason reporting.
- Measure the reduced-face experiment on the target hardware, including underwater and transparent-boundary scenes.
- Keep camera-relative water-distance fading deferred until a separate water render layer or renderer-specific integration can make it correct.
- Re-evaluate broader shape-aware culling only after measurements and visual tests justify it.

## Release gate

A stable release requires a tagged, audited artifact; documented benchmark results; disabled-mode comparison; visual checks across water-heavy scenes; backend and companion-mod checks; and a client-only multiplayer smoke test with current server-rule review.
