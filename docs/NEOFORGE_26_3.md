# NeoForge 26.3

Water Optimisation v1.1.0 includes a NeoForge 26.3 build under
`neoforge-26.3/`. It is a full release built on the published NeoForge
`26.3.0.45-beta` coordinate. The unreleased v1.1.2 candidate moves to stable
NeoForge `26.3.0.86`. v1.1.1 shipped separately for Fabric 26.1.2.

## Scope

- Reuses the shared configuration, policy, diagnostics, assets, and particle
  behavior used by the Fabric 26.3 target, plus the tabbed settings screen in
  `neoforge-26.3/src` (also used by the one-time Forge 26.3 build).
- Opens settings from NeoForge's native Mods screen or the `K` key.
- Keeps Sodium as the geometry owner when it is present.
- Carries the NeoForge renderer mixins, with the NeoForge-patched 26.3
  `FluidRenderer.shouldRenderFace` neighboring-`BlockState` descriptor and the
  NeoForge `SectionCompiler.compile` overload that also takes the
  `AddSectionGeometryEvent` renderers.

## Build

```text
./gradlew -p neoforge-26.3 clean test build verifyArtifact --no-daemon --console=plain
```

This needs Java 25. The first run downloads and prepares Minecraft 26.3 and
takes longer than an ordinary mod build. The runtime JAR is
`neoforge-26.3/build/libs/water-optimisation-1.1.2-mc26.3-neoforge.jar`.
`verifyArtifact` checks the generated metadata, client-only package boundary,
icon, license, and required mixin classes.

To try a different published coordinate without editing the target, pass both
overrides:

```text
./gradlew -p neoforge-26.3 clean test build verifyArtifact \
  -Pneo_version_override=VERSION \
  -Pneo_version_range_override='[VERSION,)' \
  --no-daemon --console=plain
```

## CI

The `neoforge-26-3` job in `.github/workflows/build.yml` builds and verifies
the target, starts the packaged client under `xvfb` with
`wateroptimisation.verifyMixins` so every injection must apply
(`defaultRequire: 1`), and checks that a dedicated server still reaches its
ready state with the client-only JAR present.

## Stable NeoForge candidate (v1.1.2)

The candidate requires NeoForge `26.3.0.86` or newer on Minecraft 26.3.
Before publishing, re-check the renderer hook descriptors against that runtime,
rerun the build and CI smoke, and test the exact JAR in a client.

Validation and release status are recorded below as checks complete. A passing
build does not establish mixin application, visual correctness, or performance.

### Checkpoint — 16 October 2026

- Based on GitHub `main` commit `5c44952875a52b3db05b9b7ee43279c68388316c`.
  No open PR existed when preparation began. At this local-validation
  checkpoint, the candidate had not been pushed, tagged, or published.
- Local Java 25 `test build verifyArtifact --no-daemon --console=plain` passed:
  20 tests, no failures, errors, or skips. The modern Fabric mocked predicate
  fixture is excluded from this standalone target as before.
- Repository privacy audit, client-only boundary audit, and `git diff --check`
  passed. Generated metadata declares mod version `1.1.2`, Minecraft `[26.3]`,
  and NeoForge `[26.3.0.86,)`.
- Inspected stable patched-runtime bytecode: the existing fluid tessellation,
  optional back-face, particle, translucent resort, and five-argument section
  compile targets are present. This is static compatibility evidence; runtime
  mixin application is still unverified for the stable dependency.
- Runtime SHA-256:
  `ce998e43ac195ca7e6af01c83e21e5f7fa96986031520a5d28bd8bd110e457f7`.
- Sources SHA-256:
  `d56539b8fa25de9f06c4b16959c4d0481671b4645a079a820cd88fa769eb4cbf`.
- Existing local changes to `CONTRIBUTING.md`, `docs/TESTING.md`, and
  `gradlew.bat` were preserved without editing. The first two still contain
  outdated NeoForge status text; reconcile those separately without losing
  their existing changes.

Next: submit only the candidate changes for GitHub CI, requiring the packaged
client/mixin audit and dedicated-server smoke on NeoForge `26.3.0.86`.
Previous green run `38293195346` used the beta dependency and is not stable
candidate evidence. Then playtest this exact runtime JAR (Mods settings and
`K`, apply/cancel/persistence, source/flowing/waterlogged and underwater views,
with Sodium absent and present) before publication. No visual or performance
claim has been established. v1.1.0 assets and the Fabric-only v1.1.1 release
must remain unchanged.
