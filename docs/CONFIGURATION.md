# Active Configuration

The configuration is local to the client and is written to config/wateroptimisation.json. It contains no server identifiers, telemetry, remote configuration, or network behavior. The master switch is disabled by default.

## Main screen

| Setting | Default | Behavior |
| --- | --- | --- |
| enabled | false | Master switch. When off, rendering and particle hooks preserve vanilla behavior. |
| performanceProfile | balanced | Selects Vanilla, Balanced, Performance, or Maximum FPS defaults. |

The main screen keeps changes in memory until Done. Cancel and Escape discard unsaved edits. The configuration file is replaced through a temporary file and an atomic move when the filesystem supports it.

### Profiles

| Profile | Behavior |
| --- | --- |
| Vanilla | Disables the master switch and all optimization paths. Use it as the reference state. |
| Balanced | Keeps vanilla fluid-face decisions, keeps nearby water particles, and leaves the flat-water fast path off. |
| Performance | Keeps vanilla fluid-face decisions, enables the explicitly labelled flat source-water fast path, and disables cosmetic water particles by default with a 16-block fog-tightened bound if re-enabled. |
| Maximum FPS | Enables the flat source-water fast path and optional reduced-face mode, and disables cosmetic water particles by default with a 16-block fog-tightened bound if re-enabled. The reduced-face mode can change inside-water views. On a reviewed Sodium 0.9.x/Minecraft 26.2 build, the same reduced-face experiment is applied through the optional Sodium bridge. |

Selecting a preset resets its Advanced settings values. Choosing Vanilla also turns off the master switch. Presets do not promise a particular FPS result.

## Advanced settings

| Setting | Default | Behavior |
| --- | --- | --- |
| fluidCullingMode | conservative | Disabled, Conservative, or Experimental. Disabled turns off fluid hooks; Conservative enables only the fully hidden source-water fast path; Experimental additionally removes vanilla's optional reverse fluid faces for ordinary full source-water blocks. |
| flatWaterFastPath | false | Skips only an ordinary source-water block whose six neighboring faces are hidden by ordinary source-water blocks or full solid-rendering blocks. |
| waterParticles | true | Keeps or rejects cosmetic water particles after the master switch is enabled. The Performance preset sets this to false. |
| particleDistance | 32 | Maximum camera-relative admission distance in blocks; clamped to 8–128. The Performance preset uses 16. During camera initialization, the player position is used as a lifecycle fallback. |
| particleFogCulling | false | Tightens the camera-relative distance bound to 75% as a conservative fog approximation; it does not reproduce backend-specific fog math. The Performance preset enables it. |
| diagnosticsHud | false | Shows local counters plus fluid tessellation, section-compilation, translucent-resort averages, and removed reverse-face counts. |
| debugFallbackLogging | false | Logs local fallback decisions where a future hook reports one. |

The reduced-face setting is optional because it can change how ordinary source water looks from inside a fluid volume or through unusual transparent arrangements. It is enabled only by Maximum FPS and by manually selecting it; Sodium ownership disables the vanilla hook, while a reviewed Sodium 0.9.x/Minecraft 26.2 build can use the optional bridge. Flowing water, waterlogged blocks, overlays, transparent boundaries, and ambiguous shapes still use the renderer's normal geometry decisions; the mode only changes the optional reverse-face emission after the renderer has selected a fluid face.

## Compatibility behavior

If Sodium is detected, the vanilla fluid hooks are disabled so Water Optimisation does not duplicate or replace Sodium's fluid renderer. On a reviewed Sodium 0.9.x/Minecraft 26.2 build, Maximum FPS may use the optional reduced-face bridge; unknown or unmatched builds remain particle-only. The local particle and settings paths remain available. The main screen and diagnostics HUD report the renderer ownership state.

Waterlogged blocks, flowing edges, partial shapes, transparent neighbors, overlays, and unusual block states are left to vanilla behavior. No setting changes FluidState, collision, movement, world updates, or server state.
