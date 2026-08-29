# Proposed Configuration

These settings are proposals for implementation. They are not active yet.

| Setting | Proposed default | Purpose |
| --- | --- | --- |
| enabled | false | Master switch |
| fluidCullingMode | conservative | Selects disabled, conservative, or experimental behavior |
| flatWaterFastPath | false | Enables the uniform source-water fast path |
| waterParticles | true | Keeps water-related cosmetic particles enabled |
| particleDistance | 32 | Maximum distance for optional water-particle admission |
| particleFogCulling | false | Skips particles hidden beyond opaque fog when supported |
| diagnosticsHud | false | Shows local counters and timings |
| debugFallbackLogging | false | Logs local renderer fallbacks for development |

The UI should clearly explain that stronger culling can create visual differences. Settings must be local, atomic, recoverable, and free of remote configuration.

The first release candidate should prefer a small number of understandable performance profiles over exposing every internal switch.
