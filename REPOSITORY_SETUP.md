# Repository Setup

This repository was created as a private planning workspace based on the structure and maintenance standards used by FPS Tune.

## Current state

The first planning commit contains documentation only. A Fabric 26.2 build scaffold should be added as the first implementation task after the benchmark plan is finalized.

## Planned project baseline

- Minecraft 26.2
- Java 25
- Fabric Loader and Fabric API versions matching the target
- Optional Mod Menu integration
- Optional Sodium compatibility
- Official Mojang mappings where supported by the selected toolchain
- No runtime dependency on DonutSMP

## Development order

1. Establish the reproducible benchmark scene and diagnostics.
2. Add the smallest buildable client-only scaffold.
3. Implement low-risk particle controls.
4. Add conservative fluid geometry optimisation.
5. Add fast paths only after correctness tests exist.
6. Validate companion-mod behavior and DonutSMP safety.
