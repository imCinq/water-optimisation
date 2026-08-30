# Active Configuration

The configuration is local to the client and is written to config/wateroptimisation.json. It contains no server identifiers, telemetry, remote configuration, or network behavior. The master switch is disabled by default.

## Main screen

| Setting | Default | Behavior |
| --- | --- | --- |
| enabled | false | Master switch. When off, rendering and particle hooks preserve vanilla behavior. |
| performanceProfile | balanced | Selects Vanilla, Balanced, or Performance defaults. |

The main screen keeps changes in memory until Done. Cancel and Escape discard unsaved edits. The configuration file is replaced through a temporary file and an atomic move when the filesystem supports it.

### Profiles

| Profile | Behavior |
| --- | --- |
| Vanilla | Disables the master switch and all optimization paths. Use it as the reference state. |
| Balanced | Keeps vanilla fluid-face decisions, keeps nearby water particles, and leaves the flat-water fast path off. |
| Performance | Keeps vanilla fluid-face decisions, enables the explicitly labelled flat source-water fast path, and disables cosmetic water particles by default with a 16-block fog-tightened bound if re-enabled. |

Selecting a preset resets its More settings values. Choosing Vanilla also turns off the master switch. Presets do not promise a particular FPS result.

## More settings

| Setting | Default | Behavior |
| --- | --- | --- |
| fluidCullingMode | conservative | Disabled, Conservative, or Experimental (currently the same safe subset). It gates the interior fast path; vanilla remains responsible for ordinary same-fluid face culling. |
| flatWaterFastPath | false | Skips only an ordinary source-water block surrounded on all six sides by ordinary source-water blocks. |
| waterParticles | true | Keeps or rejects cosmetic water particles after the master switch is enabled. The Performance preset sets this to false. |
| particleDistance | 32 | Maximum camera-relative admission distance in blocks; clamped to 8–128. The Performance preset uses 16. During camera initialization, the player position is used as a lifecycle fallback. |
| particleFogCulling | false | Tightens the camera-relative distance bound to 75% as a conservative fog approximation; it does not reproduce backend-specific fog math. The Performance preset enables it. |
| diagnosticsHud | false | Shows local counters plus fluid tessellation, section-compilation, and translucent-resort averages. |
| debugFallbackLogging | false | Logs local fallback decisions where a future hook reports one. |

The experimental label is intentional. The current safe subset is narrow; ambiguous shapes are not classified by this mod. The face-culling mode does not add a second face predicate because that was measured as redundant overhead.

## Compatibility behavior

If Sodium is detected, the vanilla fluid hooks are disabled so Water Optimisation does not duplicate or replace Sodium's fluid renderer. The local particle and settings paths remain available. The diagnostics HUD reports the renderer ownership state.

Waterlogged blocks, flowing edges, partial shapes, transparent neighbors, overlays, and unusual block states are left to vanilla behavior. No setting changes FluidState, collision, movement, world updates, or server state.
