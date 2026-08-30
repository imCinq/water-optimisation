# Benchmark Report

This file is a human-completed report template. It intentionally contains no generated screenshots, runtime logs, world data, account information, or performance claim.

## Current evidence

Remote evidence for the implementation branch:

- Minecraft target: 26.2
- Java build: 25
- Fabric Loader: 0.19.3
- Fabric API: 0.158.0+26.2
- Gradle/Loom: Gradle 9.5.1 and Loom 1.17.20
- repository privacy audit: passed remotely
- client-only audit: passed remotely
- unit tests and client build: passed remotely
- local M2, OpenGL/Vulkan, visual, Sodium, modpack, and DonutSMP validation: pending

The current remote build proves that the source compiles and packages. It does not prove an FPS gain or visual equivalence.

## Environment

| Field | Value |
| --- | --- |
| OS and GPU | |
| Minecraft | 26.2 |
| Java | |
| Fabric Loader | |
| Fabric API | |
| Water Optimisation commit/JAR | |
| Sodium and version | |
| Other renderer/performance mods | |
| Resource pack/shaders | |
| Backend | OpenGL or Vulkan |
| Resolution and GUI scale | |
| Render distance/simulation distance | |
| FPS cap/graphics settings | |

## Scene runs

For each scene, warm up first and record multiple samples.

| Scene | Mode | Avg FPS | 1% low | p95 ms | p99 ms | Hitches | Fluid compile ms | Section compile ms | Resort ms | Water blocks/faces | Particle candidates/rejected | Visual result |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | --- | --- | --- |
| Flat ocean | Disabled | | | | | | | | | | | |
| Flat ocean | Balanced | | | | | | | | | | | |
| Flat ocean | Performance | | | | | | | | | | | |
| Waterfall | Disabled | | | | | | | | | | | |
| Waterfall | Balanced | | | | | | | | | | | |
| Flooded cave | Disabled | | | | | | | | | | | |
| Flooded cave | Balanced | | | | | | | | | | | |
| Waterlogged/transparent | Disabled | | | | | | | | | | | |
| Waterlogged/transparent | Balanced | | | | | | | | | | | |
| Particle-heavy | Disabled | | | | | | | | | | | |
| Particle-heavy | Balanced | | | | | | | | | | | |

## Acceptance notes

- [ ] No missing planes, seams, z-fighting, overlay errors, or stale geometry
- [ ] Flowing water orientation and lighting match the disabled reference
- [ ] Waterlogged, leaves, transparent, partial, and cave cases fall back safely
- [ ] Sodium-present and Sodium-absent runs are stable
- [ ] OpenGL and Vulkan runs are stable
- [ ] Non-water scene does not regress
- [ ] Client-only multiplayer smoke test passes
- [ ] Current DonutSMP rules permit the client-only mod
