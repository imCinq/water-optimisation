# Proposed Configuration

These settings are proposals for implementation. They are not active yet.

## Main screen

The main screen should expose only the decisions most users need:

| Setting | Proposed default | Purpose |
| --- | --- | --- |
| enabled | false | Master switch; disabled until the user opts in |
| performanceProfile | balanced | Selects Vanilla, Balanced, or Performance behavior |

### Profiles

| Profile | Intended behavior |
| --- | --- |
| Vanilla | No optimisation behavior; useful as a reference |
| Balanced | Conservative fluid culling, full nearby water effects, and no experimental fast path |
| Performance | Conservative culling plus validated particle and flat-water optimisations |

Experimental behavior must not be silently enabled by a profile. It belongs under Advanced settings and should be clearly labelled.

## Advanced settings

| Setting | Proposed default | Purpose |
| --- | --- | --- |
| fluidCullingMode | conservative | Selects disabled, conservative, or experimental behavior |
| flatWaterFastPath | false | Enables the uniform source-water fast path after validation |
| waterParticles | true | Keeps water-related cosmetic particles enabled |
| particleDistance | 32 | Maximum distance for optional water-particle admission |
| particleFogCulling | false | Skips particles hidden beyond opaque fog when supported |
| diagnosticsHud | false | Shows local counters and timings |
| debugFallbackLogging | false | Logs local renderer fallbacks for development |

## Simplicity rules

- The main screen must not expose internal renderer terminology.
- Every setting needs a short tooltip describing its visual trade-off.
- Advanced settings should be collapsed behind one button.
- Provide a Reset to profile action rather than requiring users to understand dependencies between settings.
- Do not add a required configuration library only to create the screen; prefer Minecraft's native screen APIs.
- Mod Menu is a soft dependency. Its absence must not crash the mod.
- Settings are local, atomic, recoverable, and free of remote configuration.
- The master switch is off by default.
- Done saves changes; Cancel and Escape discard unsaved edits.
- A quick toggle may save immediately, but it must change only the local master switch.
