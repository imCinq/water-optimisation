# Benchmark Report

Use this template for reproducible local measurements. Do not commit generated screenshots, runtime logs, world data, account information, or private server details.

## Evidence boundary

A successful build proves compilation and packaging. It does not prove an FPS gain, visual equivalence, or server compatibility.

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

Warm up each scene first and record multiple samples.

| Scene | Mode | Avg FPS | 1% low | p95 ms | p99 ms | Hitches | Fluid compile ms | Section compile ms | Resort ms | Water blocks/skips/faces | Particle candidates/rejected | Visual result |
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
- [ ] Flowing-water orientation and lighting match the disabled reference
- [ ] Waterlogged, leaves, transparent, partial, and cave cases fall back safely
- [ ] Sodium-present and Sodium-absent runs are stable
- [ ] OpenGL and Vulkan runs are stable where available
- [ ] Non-water scenes do not regress
- [ ] Client-only multiplayer smoke test passes
- [ ] Current server rules permit the mod
