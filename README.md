<p align="center">
  <img src="assets/cinq-water-optimisation-logo.webp" alt="Cinq creator logo" width="180">
</p>

<h1 align="center">Water Optimisation</h1>

<p align="center">
  A client-side Fabric mod for Minecraft 26.2 focused on water-rendering performance.
</p>

<p align="center">
  <a href="https://github.com/imCinq/water-optimisation/actions/workflows/build.yml"><img src="https://github.com/imCinq/water-optimisation/actions/workflows/build.yml/badge.svg" alt="Build status"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-2f6fed.svg" alt="MIT License"></a>
  <img src="https://img.shields.io/badge/Minecraft-26.2-2f6fed.svg" alt="Minecraft 26.2">
  <img src="https://img.shields.io/badge/Fabric-client--side-2f6fed.svg" alt="Fabric client-side mod">
</p>

Water Optimisation reduces client-side rendering work in water-heavy Minecraft scenes such as oceans, waterfalls, flooded caves, waterlogged builds, and particle-rich environments. It focuses on fluid geometry decisions, water-particle admission, and local rendering diagnostics while preserving vanilla world behavior and gameplay.

## What the mod does

The mod adds an opt-in rendering layer for Minecraft 26.2 with three configuration profiles: Vanilla, Balanced, and Performance. Each profile controls the same rendering features through a native Minecraft settings screen. Advanced controls are grouped into safe performance, experimental GPU rendering, and diagnostics so visual trade-offs are easy to find. Performance also disables cosmetic water particles by default and enables the conservative fog-tightened particle bound if particles are turned back on.

### Fluid geometry

Water Optimisation leaves vanilla's fluid-face predicate in charge. Minecraft already culls faces between equal fluids, so adding a second face hook only adds CPU work without changing the mesh.

A separate fast path recognizes an ordinary source-water block whose six faces are hidden by ordinary source-water blocks or full solid-rendering blocks. It reuses the six block and fluid states that vanilla has already loaded, then skips the rest of tessellation for that fully hidden block. Flowing water, partial levels, waterlogged blocks, overlays, transparent boundaries, and other ambiguous cases continue through vanilla-compatible geometry.

The Advanced settings page also exposes an Experimental reduced-face mode. It applies only to ordinary full source-water blocks, preserves each outward fluid face, and removes vanilla's optional reverse face. This can reduce translucent geometry and overdraw in exchange for possible differences when viewing water from inside or through unusual transparent arrangements. It is off in every preset, and it is automatically inactive when Sodium owns fluid rendering.

### Water particles

Water-particle admission is filtered using camera-relative distance. During camera initialization, the player position provides a fallback reference so filtering remains stable. A conservative fog and distance mode is available for scenes where distant water particles contribute little visible detail.

### Diagnostics

The optional diagnostics HUD exposes local rendering measurements, including fluid tessellation, section compilation, translucent resorting, fully hidden fast-path skips, removed reverse faces, and rejected water particles. Fluid compile timing samples one in sixteen calls to keep the HUD low overhead; use Tracy or mesh statistics for frame-time distributions and total face counts. These counters are intended to explain where frame time is spent rather than alter gameplay or world simulation.

### Renderer integration

When Sodium is present, the mod detects renderer ownership and disables its vanilla fluid hooks so the two rendering paths do not compete. Mod Menu integration is optional; the core configuration remains independent of it.

## Test coverage

### Automated checks

The GitHub Actions suite is passing for the current preview branch. It covers:

| Check | Coverage |
| --- | --- |
| Gradle and Java validation | Gradle wrapper integrity and the Java 25 toolchain |
| Unit tests | Safe defaults, profile reset, master-switch recovery, value clamping, camera-relative particle distance, fog scaling, null recovery, and independent configuration copies |
| Privacy audit | Secrets, personal information, local paths, generated output, and runtime data |
| Client-only audit | Networking, movement, world simulation, and player-information boundaries |
| Build and packaging | Main JAR, sources JAR, tests, and reproducible artifact packaging |

These checks establish that the project compiles, packages, and remains within its intended static boundaries.

### Minecraft runtime coverage

The runtime validation matrix covers flat and ocean water, flowing water and waterfalls, waterlogged blocks, leaves and transparent boundaries, flooded caves, underwater views, chunk loading, block updates, and ordinary non-water scenes. It also includes Sodium present and absent, Mod Menu present and absent, and the available rendering backends.

Runtime evidence is based on visual comparison with the feature disabled and measurements such as average FPS, 1% lows, p95/p99 frame time, hitches, fluid and section compilation time, translucent resorting time, water geometry counts, and particle admission counts. The automated suite is complete; the target-hardware runtime matrix is the remaining source of visual and performance evidence for the 0.1.0-preview.6 build.

## Compatibility

The target environment is Minecraft 26.2, Fabric Loader 0.19.3 or newer, Fabric API 0.158.0+26.2, and Java 25. Resource packs, shaders, companion performance mods, rendering backends, and renderer ownership can affect results.

The implementation uses Minecraft's Blaze3D, RenderPipeline, RenderType, and Fabric rendering abstractions. It does not replace another renderer or use raw OpenGL. Fluid simulation, collision, movement, networking, and server state remain outside the mod's rendering scope.

## Project state

Water Optimisation is a 0.1.0-preview.6 build. The client-side implementation, organized configuration screens, diagnostics, automated tests, privacy audit, client-only audit, and artifact packaging are implemented. Visual and performance results are being established through the Minecraft runtime matrix.

## Reference

- [Architecture](docs/ARCHITECTURE.md)
- [Configuration](docs/CONFIGURATION.md)
- [Compatibility](docs/COMPATIBILITY.md)
- [Benchmarking](docs/BENCHMARKING.md)
- [Testing matrix](docs/TESTING.md)
- [Benchmark report template](docs/BENCHMARK_REPORT.md)

## License

Water Optimisation is released under the [MIT License](LICENSE). Created by Cinq.
