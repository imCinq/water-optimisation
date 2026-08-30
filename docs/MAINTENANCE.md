# Maintenance and update process

## Routine dependency update

1. Review the official upstream release notes and compatibility requirements.
2. Change only the relevant version profile or dependency declaration.
3. Run a clean build and all tests.
4. Run the repository privacy audit and client-only audit when the implementation exists.
5. Review the produced JAR contents and dependency changes.
6. Record the change in CHANGELOG.md.
7. Merge only after CI passes.

Dependabot may open update pull requests, but updates must be reviewed before merging. Do not auto-merge Minecraft, Fabric, Loom, Mod Menu, Sodium, or rendering-library updates.

## Minecraft version update

Handle a Minecraft update in a dedicated branch and pull request:

1. Add or update the target version profile with Minecraft, Fabric Loader, Fabric API, Loom, Java, mappings, and artifact settings.
2. Use the correct Loom configuration for the target.
3. Compile before changing mixins so mapping or signature failures are visible.
4. Inspect affected client-rendering bytecode and verify every changed hook.
5. Keep version-specific sources isolated.
6. Add tests for changed visibility, culling, particle, or configuration behavior.
7. Run a graphical client with the feature disabled and enabled.
8. Update README.md, AGENTS.md, docs/COMPATIBILITY.md, docs/DISTRIBUTION.md, and CHANGELOG.md.

Compilation alone is not sufficient evidence of Minecraft-version support.

## Release process

Start with the [public release checklist](RELEASE_CHECKLIST.md) and keep its acceptance record with the tagged release.

Before any public artifact:

1. Confirm the repository contains no secrets, private paths, personal information, or generated runtime files.
2. Run a clean build, tests, and all audits.
3. Inspect the exact JAR and calculate SHA-256 checksums.
4. Confirm the version, changelog, metadata, and documentation agree.
5. Validate the disabled mode and all documented visual trade-offs.
6. Smoke-test the exact artifact in a clean client.
7. Review current platform rules and server compatibility wording.

There is no in-mod updater. Releases should be manually reviewed and published from a tagged commit. Never commit publishing tokens or enable publishing automation without an explicit distribution decision.
