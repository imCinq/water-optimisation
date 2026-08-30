# Water Optimisation

Private research and development workspace for a client-side Minecraft Fabric mod focused on water-rendering performance.

## Status

The first opt-in implementation is now on the remote feature branch. It builds and passes automated tests and repository audits remotely, but it is not a public release and still needs the local Minecraft visual/performance matrix.

## Goal

Improve frame-time stability in Minecraft 26.2 water-heavy scenes, including large oceans, waterfalls, flooded caves, farms, and particle-heavy areas, while preserving vanilla gameplay behavior and visual correctness.

The mod is client-only and render-focused. It must not change server simulation, collision, movement, combat, inventory, networking, or player-information features.

## Implemented opt-in features

- Native Minecraft settings screens with a master switch, Vanilla/Balanced/Performance profiles, Advanced controls, Reset to profile, and atomic local configuration.
- Optional local diagnostics HUD for fluid tessellation counters, face decisions, fast-path skips, fluid/section compile timing, translucent-resort timing, and rejected water particles.
- Optional water-particle admission filtering by camera-relative distance, with a player-position fallback during camera initialization and a conservative fog/distance tightening mode.
- Conservative fluid-face decisions limited to equal full source-water blocks.
- An explicit flat source-water fast path that skips only fully interior ordinary source-water blocks.
- Sodium detection that disables the vanilla fluid hooks so the mod does not compete with a renderer-owned fluid path.
- Optional Mod Menu Configure integration; Mod Menu is not required at runtime.

Every rendering shortcut falls back to vanilla behavior for flowing, partial, waterlogged, overlay, transparent, or otherwise ambiguous cases. The master switch is disabled by default.

## Simple user experience

Open Water Optimisation from Mod Menu when it is installed, or use the registered keybind from the Controls menu. The main screen exposes the master switch and profile. Advanced settings contain the experimental controls and diagnostics.

The settings describe trade-offs but do not promise a universal FPS increase. Compare the same scene with the feature disabled before drawing conclusions.

## Roadmap status

| Phase | Focus | Status |
| --- | --- | --- |
| 0 | Reproducible water benchmarks and instrumentation | Remote instrumentation and report template complete; local measurements pending |
| 1 | Fabric 26.2 scaffold, configuration, and Mod Menu screen | Implemented and remotely build-verified; local UI checks pending |
| 2 | Water-particle distance and fog filtering | Implemented behind the opt-in master switch; local visual and metric checks pending |
| 3 | Conservative fluid-face culling | Implemented for the exact safe source-water subset; local visual and metric checks pending |
| 4 | Flat source-water fast path | Implemented as an explicit opt-in path; local visual and compile-time checks pending |
| 5 | Sodium compatibility and broader validation | Renderer-ownership guard implemented; Sodium/backend/modpack matrix pending |
| 6 | Release review and server-rule documentation | Documentation is in place; DonutSMP rule review and distribution decision pending |

## Remote-first development

The implementation branch is built and audited through GitHub Actions with Java 25 and the Minecraft 26.2 Fabric toolchain. This avoids requiring Java, Gradle, or a local clone merely to implement and compile the mod. The final local run is still required for the M2 GPU, OpenGL/Vulkan, visual, modpack, and DonutSMP checks.

See [docs/REMOTE_CODEX_WORKFLOW.md](docs/REMOTE_CODEX_WORKFLOW.md) and [docs/BENCHMARK_REPORT.md](docs/BENCHMARK_REPORT.md).

## Safety boundary

This project is render-only. It must not add packets, automation, movement changes, combat assistance, ESP, radar, freecam, x-ray behavior, or changes to fluid collision and world state. Client-only does not automatically mean server-approved; current server rules must be checked before use.

## Documentation

- [AGENTS.md](AGENTS.md) — development and maintenance rules
- [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) — project conduct
- [docs/RESEARCH.md](docs/RESEARCH.md) — Minecraft 26.2 water findings
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — implemented design and fallbacks
- [docs/IMPLEMENTATION_PLAN.md](docs/IMPLEMENTATION_PLAN.md) — staged work plan
- [docs/REMOTE_CODEX_WORKFLOW.md](docs/REMOTE_CODEX_WORKFLOW.md) — remote implementation and verification limits
- [docs/UI_AND_OPTIONS.md](docs/UI_AND_OPTIONS.md) — settings behavior and Mod Menu
- [docs/BENCHMARKING.md](docs/BENCHMARKING.md) — measurement methodology
- [docs/BENCHMARK_REPORT.md](docs/BENCHMARK_REPORT.md) — report template and current evidence boundary
- [docs/COMPATIBILITY.md](docs/COMPATIBILITY.md) — Fabric, Sodium, Mod Menu, and DonutSMP boundaries
- [docs/CONFIGURATION.md](docs/CONFIGURATION.md) — active settings
- [docs/TESTING.md](docs/TESTING.md) — verification matrix
- [docs/MAINTENANCE.md](docs/MAINTENANCE.md) — update and release process
- [docs/DISTRIBUTION.md](docs/DISTRIBUTION.md) — future distribution plan

## Current target

- Minecraft: 26.2
- Java: 25
- Mod loader: Fabric
- Mod id: wateroptimisation
- Optional integration: Mod Menu 19.0.0-alpha.1
- Intended distribution: private development first
- License: MIT
