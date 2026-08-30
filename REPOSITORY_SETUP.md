# Development Setup

## Requirements

- Git
- Java Development Kit 25
- A Minecraft 26.2 Fabric client for local runtime validation
- Fabric Loader 0.19.3 or newer
- Fabric API matching Minecraft 26.2

The repository includes the Gradle wrapper, so a separate Gradle installation is not required.

## Build and test

```bash
./gradlew test build
bash scripts/audit-repository.sh
bash scripts/audit-client-only.sh
```

The build produces client and sources JARs under `build/libs/`. Build output, caches, runtime data, logs, screenshots, and benchmark results are ignored by Git.

## Project layout

- `src/main` — shared configuration and pure policies.
- `src/client` — Minecraft client screens, diagnostics, renderer hooks, and mixins.
- `src/test` — unit tests for configuration and pure policy logic.
- `docs` — architecture, compatibility, benchmarking, and release documentation.
- `scripts` — repository and client-only audits.

## Change checklist

1. Identify a measured rendering or diagnostics problem.
2. Keep the implementation within Minecraft's Blaze3D/Fabric abstractions.
3. Preserve vanilla fallbacks for ambiguous cases.
4. Add or update tests and documentation.
5. Run the build and both repository audits.
6. Validate the exact JAR in a local Minecraft client before release.
