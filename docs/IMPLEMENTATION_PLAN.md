# Roadmap

Water Optimisation is developed as a conservative, client-only rendering mod. Every optimization must be measurable, visually reviewable, and safe to disable.

## Current release

The 0.0.4 preview contains the current client-only implementation for Minecraft 26.2 and the target-isolated compatibility implementation for Minecraft 1.21.1:

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

The 26.2 implementation is the stable 0.0.3 baseline; the 0.0.4 preview packages both target-specific builds from the same reviewed source line. Remote build, test, privacy, client-only, and artifact checks pass. Local visual, performance, backend, companion-mod, and multiplayer measurements remain evidence work for the target hardware and exact modpack.

The Minecraft 1.21.1 profile uses Java 21, remapping Loom, official Mojang mappings, and target-isolated client sources. Its geometry path is intentionally more conservative and has no Sodium geometry bridge yet.

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

The reduced-face mode keeps vanilla's outward fluid face and removes only its optional reverse face for ordinary full source-water blocks. Flowing and waterlogged states stay on vanilla. It is available manually and in the Maximum FPS profile because inside-water and unusual transparency views can change. It is disabled in the vanilla hook when Sodium owns fluid rendering. A separate, version-gated Sodium 0.9.x/Minecraft 26.2 bridge applies the same narrow face reduction without replacing Sodium's renderer; unknown builds remain on the fallback.

## Next priorities

- Start the dedicated far-water pass prototype early for fill-rate-bound scenes. First separate water ownership from the shared translucent buffer; do not expose a distance/LOD toggle until that representation exists.
- Define the far-water pass around independent water-only distance, fog, and later LOD/half-resolution controls through the Minecraft rendering abstraction. Keep the prototype separate from both the 26.2 shared translucent path and the 1.21.1 compatibility adapter.
- Complete local visual and performance validation.
- Validate the optional Sodium reduced-face bridge on the exact 26.2 Sodium build and keep it disabled if either hook does not match.
- Add direct tests if the fluid classifier expands beyond the exact source-water subset.
- Consider rolling diagnostics and fallback reason reporting.
- Measure the reduced-face experiment on the target hardware, including underwater and transparent-boundary scenes.
- Keep camera-relative water-distance fading deferred until the separate water-owned pass is correct.
- Re-evaluate broader shape-aware culling only after measurements and visual tests justify it.

## Release gate

A stable release requires a tagged, audited artifact; documented benchmark results; disabled-mode comparison; visual checks across water-heavy scenes; backend and companion-mod checks; and a client-only multiplayer smoke test with current server-rule review.
