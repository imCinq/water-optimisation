# Repository Setup

This repository was created as a private planning workspace based on the structure and maintenance standards used by FPS Tune.

## Current state

The first planning commit contains documentation only. A Fabric 26.2 build scaffold should be added as the first implementation task after the benchmark plan is finalized.

## Planned project baseline

- Minecraft 26.2
- Java 25
- Fabric Loader and Fabric API versions matching the target
- Optional Mod Menu integration through a suggested dependency
- Native Minecraft configuration screen
- Optional Sodium compatibility
- Official Mojang mappings where supported by the selected toolchain
- No runtime dependency on DonutSMP

## Remote-first development

The preferred development experiment is:

1. Codex creates a feature branch and works in a temporary remote workspace.
2. Java, Gradle, tests, audits, and builds run remotely.
3. Only source, documentation, tests, and configuration are committed to GitHub.
4. Build caches, runtime folders, generated JARs, and benchmark artifacts remain outside the repository.
5. A final JAR is downloaded only when a local Minecraft test is needed.

This avoids local Java and Gradle setup on the Mac. It does not remove the need to run Minecraft locally for M2 graphics, frame-time, visual, and DonutSMP validation.

## User experience baseline

The main screen should contain only the master switch and a profile selector. Advanced renderer and particle controls should be separated, documented, and disabled by default when experimental.

## Development order

1. Establish the reproducible benchmark scene and diagnostics.
2. Add the smallest buildable client-only scaffold.
3. Add configuration, profiles, and the optional Mod Menu adapter.
4. Evaluate the remote-first Codex workflow.
5. Implement low-risk particle controls.
6. Add conservative fluid geometry optimisation.
7. Add fast paths only after correctness tests exist.
8. Validate companion-mod behavior and DonutSMP safety.
