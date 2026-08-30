<p align="center">
  <img src="assets/cinq-water-optimisation-logo.webp" alt="Cinq logo" width="180">
</p>

<h1 align="center">Water Optimisation</h1>

<p align="center">
  A Cinq client-side Fabric mod focused on smoother water-heavy Minecraft scenes.
</p>

<p align="center">
  <a href="https://github.com/imCinq/water-optimisation/actions/workflows/build.yml?query=branch%3Acodex%2Fissue-7-fabric-26-2-scaffold"><img src="https://github.com/imCinq/water-optimisation/actions/workflows/build.yml/badge.svg?branch=codex%2Fissue-7-fabric-26-2-scaffold" alt="Build status"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-2f6fed.svg" alt="MIT License"></a>
  <img src="https://img.shields.io/badge/Minecraft-26.2-2f6fed.svg" alt="Minecraft 26.2">
  <img src="https://img.shields.io/badge/Fabric-client--side-2f6fed.svg" alt="Fabric client-side mod">
</p>

Water Optimisation targets Minecraft 26.2 and focuses on frame-time stability around oceans, waterfalls, flooded caves, waterlogged builds, and particle-heavy scenes.

## Preview status

This is an experimental 0.1.0 preview. The code compiles, packages, and passes automated tests and repository audits. Visual correctness, hardware performance, renderer compatibility, and server-rule acceptance must still be checked in a real Minecraft client.

The project does not promise a universal FPS increase. Compare every enabled mode with the same scene and the feature disabled.

## Features

- Native settings screens with Vanilla, Balanced, and Performance profiles.
- Advanced controls for conservative fluid culling, the flat source-water fast path, water particles, distance limits, diagnostics, and fallback logging.
- Opt-in diagnostics for fluid tessellation, section compilation, translucent resorting, face decisions, fast-path skips, and particle filtering.
- Camera-relative water-particle distance filtering with a safe player-position fallback during camera initialization.
- Conservative source-water face culling and an interior full-source-water fast path.
- Sodium ownership detection that leaves vanilla fluid hooks disabled when Sodium provides the active fluid renderer.
- Optional Mod Menu Configure integration.

The master switch is disabled by default. Flowing, partial, waterlogged, overlay, transparent, and otherwise ambiguous cases fall back to vanilla behavior.

## Installation

Requirements:

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API matching Minecraft 26.2
- Java 25

No stable release has been published yet. For preview testing, download the latest successful build artifact from [GitHub Actions](https://github.com/imCinq/water-optimisation/actions/workflows/build.yml), extract the runtime JAR, and place it in the Fabric `mods` folder with the matching Fabric API.

Mod Menu is optional. The mod remains usable without it.

## Usage

1. Open Water Optimisation from Mod Menu, or assign/use the `O` keybind from Minecraft's Controls menu.
2. Enable the master switch.
3. Start with Balanced.
4. Use Advanced settings only when you understand the stated visual trade-off.
5. Keep Diagnostics HUD enabled while measuring, then disable it for normal play.

The mod is client-only and does not change fluid simulation, collision, movement, packets, world updates, or server state. Check the rules of a multiplayer server before use.

## Compatibility

- The target renderer is Minecraft 26.2's Blaze3D/Fabric rendering path.
- Sodium is detected as a renderer-ownership boundary; vanilla fluid hooks are disabled when Sodium is loaded.
- Mod Menu is a soft dependency.
- OpenGL and Vulkan should be tested separately on Minecraft 26.2.
- Resource packs, shaders, companion performance mods, and server policies can change the result.

## Development

Use Java 25 and run:

```bash
./gradlew test build
bash scripts/audit-repository.sh
bash scripts/audit-client-only.sh
```

Generated build output, runtime data, logs, screenshots, and benchmark results are ignored and should not be committed.

## Documentation

- [Contributing](CONTRIBUTING.md)
- [Privacy](PRIVACY.md)
- [Security](SECURITY.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Configuration](docs/CONFIGURATION.md)
- [Compatibility](docs/COMPATIBILITY.md)
- [Benchmarking](docs/BENCHMARKING.md)
- [Testing](docs/TESTING.md)
- [Research notes](docs/RESEARCH.md)
- [Roadmap](docs/IMPLEMENTATION_PLAN.md)
- [Distribution](docs/DISTRIBUTION.md)
- [Maintenance](docs/MAINTENANCE.md)

## Roadmap

| Area | Status |
| --- | --- |
| Fabric 26.2 client-only implementation | Implemented |
| Configuration and native settings | Implemented |
| Conservative fluid and particle optimisations | Implemented |
| Diagnostics and benchmark tooling | Implemented; live measurements pending |
| Sodium and backend compatibility matrix | Validation pending |
| Public release artifact | Pending visual and compatibility validation |

## License

Water Optimisation is released under the [MIT License](LICENSE) and maintained under Cinq branding.
