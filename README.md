# Water Optimisation

Private research and development workspace for a client-side Minecraft Fabric mod focused on water-rendering performance.

## Status

Planning and profiling phase. The repository is not ready for public release.

## Goal

Improve frame-time stability in Minecraft 26.2 water-heavy scenes, including large oceans, waterfalls, flooded caves, farms, and particle-heavy areas, while preserving vanilla gameplay behavior and visual correctness.

The initial target is a client-only mod with DonutSMP-safe boundaries. It must not change server simulation, collision, movement, combat, inventory, networking, or player-information features.

## Initial technical direction

The highest-value targets are:

- translucent fluid geometry and hidden-face culling;
- repeated fluid-neighbor and corner-height work during chunk compilation;
- water-related particle admission and fog/distance culling;
- diagnostics that prove where frame time is actually being spent.

Water physics is not a client-side optimisation target. The server remains authoritative over fluid state.

## Simple user experience

The main settings screen should be understandable without renderer knowledge:

- one master enable switch;
- three performance profiles;
- a short explanation of the visual trade-off;
- one button for Advanced settings.

Mod Menu should be an optional integration, not a required dependency. When installed, it provides the normal Configure button. The core mod must still load safely without Mod Menu and retain local defaults/configuration.

## Roadmap

| Phase | Focus | Status |
| --- | --- | --- |
| 0 | Reproducible water benchmarks and instrumentation | Planned |
| 1 | Fabric 26.2 scaffold, configuration, and Mod Menu screen | Planned |
| 2 | Water-particle distance and fog filtering | Planned |
| 3 | Conservative fluid-face culling | Planned |
| 4 | Flat source-water fast path | Planned |
| 5 | Sodium compatibility and broader validation | Planned |
| 6 | Release review and server-rule documentation | Planned |

## Safety boundary

This project is render-only. It must not add packets, automation, movement changes, combat assistance, ESP, radar, freecam, x-ray behavior, or changes to fluid collision and world state. Client-only does not automatically mean server-approved; current server rules must be checked before use.

## Documentation

- [AGENTS.md](AGENTS.md) — development and maintenance rules
- [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) — project conduct
- [docs/RESEARCH.md](docs/RESEARCH.md) — Minecraft 26.2 water findings
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — proposed design
- [docs/IMPLEMENTATION_PLAN.md](docs/IMPLEMENTATION_PLAN.md) — staged work plan
- [docs/UI_AND_OPTIONS.md](docs/UI_AND_OPTIONS.md) — simple settings and Mod Menu plan
- [docs/BENCHMARKING.md](docs/BENCHMARKING.md) — measurement methodology
- [docs/COMPATIBILITY.md](docs/COMPATIBILITY.md) — Fabric, Sodium, Mod Menu, and DonutSMP boundaries
- [docs/CONFIGURATION.md](docs/CONFIGURATION.md) — proposed settings
- [docs/TESTING.md](docs/TESTING.md) — verification checklist
- [docs/MAINTENANCE.md](docs/MAINTENANCE.md) — update and release process
- [docs/DISTRIBUTION.md](docs/DISTRIBUTION.md) — future distribution plan

## Current baseline

- Minecraft target: 26.2
- Java target: 25
- Mod loader: Fabric
- Provisional mod id: wateroptimisation
- Optional integration: Mod Menu
- Intended distribution: private development first
- License: MIT
