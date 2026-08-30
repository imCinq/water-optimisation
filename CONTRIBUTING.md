# Contributing

Thanks for helping improve Water Optimisation.

## Before opening a change

- Confirm the problem is measurable in a water-heavy scene.
- Keep the change client-side and render-only.
- Preserve vanilla behavior for ambiguous shapes, fluid states, overlays, and transparency cases.
- Document visual trade-offs and compatibility assumptions.
- Do not include personal information, credentials, private server data, generated runtime files, or screenshots containing account information.

## Build and audit

Use Java 25 and run:

```bash
./gradlew test build
bash scripts/audit-repository.sh
bash scripts/audit-client-only.sh
```

## Pull requests

Include:

- a concise problem statement;
- the chosen hook or rendering path;
- tests and CI results;
- visual comparison notes;
- benchmark results when performance is the goal;
- Fabric, Sodium, resource-pack, and backend compatibility notes.

Keep commits focused and update the relevant documentation and changelog.
