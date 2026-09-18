# Contributing

Thanks for helping improve Water Optimisation.

## Before opening a change

- Confirm the problem is measurable in a water-heavy scene.
- Keep the change client-side and render-only.
- Preserve vanilla behavior for ambiguous shapes, fluid states, overlays, and transparency cases.
- Document visual trade-offs and compatibility assumptions.
- Do not include personal information, credentials, private server data, generated runtime files, or screenshots containing account information.

## Build and audit

Use Java 25 for the 26.2 and 26.3 targets and Java 21 for 1.21.1, then run each
target profile plus the standalone NeoForge build:

```bash
./gradlew -Ptarget_minecraft=26.2 test build
./gradlew -Ptarget_minecraft=26.3 test build
./gradlew -Ptarget_minecraft=1.21.1 test build
./gradlew -p neoforge-26.2 test build
bash scripts/audit-repository.sh
bash scripts/audit-client-only.sh
```

The root build defaults to 26.2, so a bare `./gradlew test build` only covers
that target. NeoForge 26.2 is a separate Gradle project and is not included in
the root build.

## Pull requests

Include:

- a concise problem statement;
- the chosen hook or rendering path;
- tests and CI results;
- visual comparison notes;
- benchmark results when performance is the goal;
- Fabric, Sodium, resource-pack, and backend compatibility notes.

Keep commits focused and update the relevant documentation and changelog.
