<p align="center">
  <img src="./assets/cinq-water-optimisation-logo.webp" alt="Water Optimisation logo" width="192">
</p>

<h1 align="center">Water Optimisation</h1>

<p align="center">
  <strong>Client-side water rendering for Minecraft 26.2</strong><br>
  Reduce water-related rendering work while keeping the world, gameplay, and server unchanged.
</p>

<p align="center">
  <a href="https://github.com/imCinq/water-optimisation/releases/latest"><img src="https://img.shields.io/github/v/release/imCinq/water-optimisation?display_name=tag&sort=semver&color=2563eb&label=latest%20release" alt="Latest release"></a>
  <a href="https://github.com/imCinq/water-optimisation/actions/workflows/build.yml"><img src="https://github.com/imCinq/water-optimisation/actions/workflows/build.yml/badge.svg" alt="Build status"></a>
  <img src="https://img.shields.io/badge/Minecraft-26.2-2563eb.svg" alt="Minecraft 26.2">
  <img src="https://img.shields.io/badge/Fabric-client--side-2563eb.svg" alt="Fabric client-side mod">
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-2563eb.svg" alt="MIT License"></a>
</p>

Water Optimisation is a small, opt-in Fabric mod for water-heavy Minecraft scenes. It focuses on client rendering and cosmetic water particles: oceans, flooded caves, waterfalls, and large pools can spend less work building or drawing water geometry.

> This mod only changes what is rendered on the client. It does not change fluid simulation, terrain, collision, movement, networking, gameplay, or server state.

## Start here

1. Download the [latest release](https://github.com/imCinq/water-optimisation/releases/latest).
2. Put the runtime JAR in the client’s `mods` folder.
3. Launch Minecraft 26.2 with Fabric.
4. Open the settings from Mod Menu, or press `O` in the client.
5. Enable the mod and start with the `Performance` preset.

The current stable package is [0.0.2](https://github.com/imCinq/water-optimisation/releases/tag/v0.0.2); `0.0.1` was the first official release. The supplied water-block logo is used in this README and is also packaged as the mod icon. The mod requires Minecraft 26.2, Java 25, Fabric Loader 0.19.3 or newer, and a matching Fabric API build (`0.158.0+26.2`). Mod Menu `19.0.0-alpha.1` is optional.

## Pick a preset

The preset selector gives the common choices clear names. Selecting a preset resets its advanced values.

| Preset | What it changes | Best for |
| --- | --- | --- |
| **Vanilla** | Turns the mod off and restores vanilla behavior. | A reference comparison. |
| **Balanced** | Keeps vanilla fluid geometry and nearby particles. | A conservative starting point. |
| **Performance** | Skips only fully hidden source-water blocks and disables water particles by default. | Safer performance testing. |
| **Maximum FPS** | Includes the Performance path and removes optional inward water faces for ordinary full source water. | Testing the strongest available water-rendering reduction. |

`Maximum FPS` is intentionally opt-in. It can change unusual inside-water or transparent-boundary views. Flowing water, waterlogged blocks, partial shapes, overlays, and ambiguous cases retain vanilla geometry decisions.

## What it improves

### Fully hidden water blocks

During section compilation, the mod reuses the six neighbor states that Minecraft has already loaded. If an ordinary full source-water block is hidden on every side by source water or a full solid-rendering block, the mod skips its fluid tessellation entirely.

Open-surface water does not qualify, so visible top faces remain. Flowing water, waterlogged blocks, transparent boundaries, partial shapes, and uncertain states fall back to vanilla.

### Optional inward-face reduction

The `Maximum FPS` preset keeps Minecraft’s normal outward water faces but removes the optional reverse copy for ordinary full source water. That can reduce translucent vertices, triangles, and overdraw in water-heavy views.

The trade-off is visual: reverse faces can matter when looking from inside a fluid volume or through unusual transparent arrangements. The normal water surface, fluid state, textures, tint, fog, and simulation are not replaced.

### Cosmetic water particles

The client can reject water-only particles before they are created. Non-water particles and always-visible particles are preserved. When enabled, the filter uses a cached camera-relative distance bound and squared-distance math; an optional fog setting tightens that bound conservatively.

Maximum FPS disables ordinary water particles by default. Particle settings continue to work when Sodium owns fluid rendering.

## Sodium and compatibility

Sodium has its own fluid renderer. When Sodium is detected, Water Optimisation disables its vanilla fluid hooks instead of competing with or replacing Sodium’s geometry. The local settings, particle filter, and diagnostics remain available.

The mod is client-only. It declares no server entrypoint, custom packets, world updates, movement changes, collision changes, player-information features, telemetry, update checker, or outbound network service. Non-water rendering is outside its scope.

## Measure before drawing conclusions

The optional diagnostics HUD reports fluid blocks, fully hidden skips, removed reverse faces, section compilation, translucent resorting, and particle admission. It is a cross-check, not a benchmark tool; disable it for final FPS measurements.

For a fair comparison, warm the same scene and compare `Vanilla`, `Performance`, and `Maximum FPS` from the same camera. Record average FPS, 1% lows, frame time, and visual correctness above water, underwater, around flowing water, in caves, and with Sodium present and absent. The mod is designed to reduce work, but no universal FPS gain is promised across hardware, shaders, resource packs, or backends.

## Requirements

| Component | Supported target |
| --- | --- |
| Minecraft | 26.2 |
| Java | 25 or newer |
| Fabric Loader | 0.19.3 or newer |
| Fabric API | `0.158.0+26.2` or matching 26.2 build |
| Mod Menu | Optional: `19.0.0-alpha.1` |
| Environment | Client only |

## Documentation

- [Configuration guide](docs/CONFIGURATION.md) — every preset and advanced option.
- [Compatibility](docs/COMPATIBILITY.md) — Sodium, Mod Menu, backends, and fallback behavior.
- [Testing matrix](docs/TESTING.md) — visual and performance checks.
- [Benchmark template](docs/BENCHMARK_REPORT.md) — repeatable measurements.
- [Architecture](docs/ARCHITECTURE.md) — renderer and client-boundary details.
- [Privacy](PRIVACY.md) and [security policy](SECURITY.md).

## Contributing

Useful changes should be measurable, client-side, and conservative around fluid shapes and transparency. See [CONTRIBUTING.md](CONTRIBUTING.md), open an issue with the exact scene and versions, or use the repository’s issue templates.

## License

Water Optimisation is released under the [MIT License](LICENSE). Created by Cinq.
