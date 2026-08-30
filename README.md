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

Water rendering can add frame-time cost in large oceans, waterfalls, flooded caves, waterlogged builds, and particle-heavy scenes. Water Optimisation targets that client-side rendering work while leaving fluid physics and gameplay unchanged.

## Current status

The 0.1.0 preview implementation is present and builds successfully. Automated tests and repository audits run in GitHub Actions. Live Minecraft testing is intentionally still pending, so this is an experimental preview rather than a stable release or a guaranteed FPS improvement.

The next step is to compare the exact preview artifact in a real Minecraft 26.2 client with the feature disabled, Balanced, and Performance modes. The test matrix is documented below and in [docs/TESTING.md](docs/TESTING.md).

## What is implemented

- Native Minecraft settings screens with Vanilla, Balanced, and Performance profiles.
- Advanced controls for fluid culling, the flat source-water fast path, water particles, particle distance, conservative fog/distance tightening, diagnostics, and fallback logging.
- Conservative fluid-face decisions limited to equal full source-water blocks.
- An interior fast path that skips only ordinary source-water blocks surrounded on all six sides by ordinary source-water blocks.
- Camera-relative water-particle admission filtering with a player-position fallback during camera initialization.
- Local diagnostics for fluid tessellation, section compilation, translucent resorting, face decisions, fast-path skips, and particle filtering.
- Sodium renderer-ownership detection that disables the vanilla fluid hooks when Sodium is active.
- Optional Mod Menu Configure integration; the core mod remains usable without Mod Menu.

## How it stays safe

The master switch is disabled by default. Flowing, partial, waterlogged, overlay, transparent, and otherwise ambiguous cases return to vanilla behavior. The mod does not change FluidState, fluid spread, collision, swimming, movement, packets, world updates, server state, or player-information features.

The implementation uses Minecraft's Blaze3D/Fabric rendering abstractions and does not replace another renderer or call raw OpenGL. Client-only does not automatically mean server-approved; check the rules of a multiplayer server before use.

## Automated tests already run

Every push and pull request runs the following checks:

- Gradle wrapper validation and a Java 25 toolchain check.
- Repository privacy audit for secrets, personal information, local paths, generated output, and unwanted runtime data.
- Client-only boundary audit for networking, movement, world simulation, and disallowed player-information behavior.
- Unit tests for safe configuration defaults, profile reset, master-switch recovery, value clamping, camera-relative particle-distance math, conservative fog scaling, null recovery, and independent configuration copies.
- `./gradlew test build`, including runtime and sources JAR packaging.

These checks prove that the project compiles, packages, and stays within its static boundaries. They do not prove visual equivalence, an FPS improvement, renderer compatibility, or server approval.

## Local Minecraft tests still required

Use the exact preview JAR and compare identical warmed scenes in this order:

1. Feature disabled as the reference.
2. Balanced profile.
3. Performance profile.
4. Advanced particle and culling settings only when their trade-offs are being measured.

Check flat water and oceans, flowing water and waterfalls, waterlogged stairs/doors/slabs/signs, leaves and transparent blocks, flooded caves, underwater views, chunk loading, block updates, and a normal non-water scene.

Look for missing planes, seams, z-fighting, overlay errors, lighting differences, wrong flow orientation, stale geometry, clipped labels, and settings that fail to persist. Repeat relevant runs with Sodium absent and present, Mod Menu absent and present, and OpenGL/Vulkan separately where available.

Record average FPS, 1% lows, p95/p99 frame time, hitches, fluid and section compilation time, translucent resorting time, water blocks/faces, and particle candidates/rejections. Keep generated screenshots, logs, world data, account information, and server data out of the repository.

## Installation

Requirements:

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API 0.158.0+26.2
- Java 25

No stable release has been published yet. For preview testing only, download the latest successful build artifact from [GitHub Actions](https://github.com/imCinq/water-optimisation/actions/workflows/build.yml). Workflow artifacts are temporary verification outputs, not stable releases; extract the runtime JAR and place it in the Fabric `mods` folder with Fabric API 0.158.0+26.2.

Mod Menu 19.0.0-alpha.1 is optional.

## Usage

1. Open Water Optimisation from Mod Menu, or use the `O` keybind from Minecraft's Controls menu.
2. Enable the master switch.
3. Start with Balanced.
4. Use Advanced settings only when measuring a stated visual trade-off.
5. Disable the Diagnostics HUD for normal play after measurements are complete.

## Compatibility

- The target is Minecraft 26.2 with Fabric Loader 0.19.3 or newer, Fabric API 0.158.0+26.2, and Java 25.
- Sodium is treated as a renderer-ownership boundary; vanilla fluid hooks are disabled when Sodium is loaded.
- Mod Menu is a soft dependency.
- Resource packs, shaders, companion performance mods, and rendering backends can change results.
- Test current multiplayer server rules before use.

## Development

Use Java 25 and run:

```bash
./gradlew test build
bash scripts/audit-repository.sh
bash scripts/audit-client-only.sh
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for change expectations and [docs/RELEASE_CHECKLIST.md](docs/RELEASE_CHECKLIST.md) for the release gate.

## Documentation

- [Contributing](CONTRIBUTING.md)
- [Privacy](PRIVACY.md)
- [Security](SECURITY.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Configuration](docs/CONFIGURATION.md)
- [Compatibility](docs/COMPATIBILITY.md)
- [Benchmarking](docs/BENCHMARKING.md)
- [Benchmark report template](docs/BENCHMARK_REPORT.md)
- [Testing matrix](docs/TESTING.md)
- [Release checklist](docs/RELEASE_CHECKLIST.md)
- [Distribution](docs/DISTRIBUTION.md)
- [Maintenance](docs/MAINTENANCE.md)

## Release status

| Area | Status |
| --- | --- |
| Fabric 26.2 client-only implementation | Implemented |
| Automated tests, build, and audits | Passing in CI |
| Local visual and performance validation | Pending |
| Sodium, backend, and companion-mod matrix | Pending |
| Stable release artifact | Not published |

## License

Water Optimisation is released under the [MIT License](LICENSE). Created by Cinq.
