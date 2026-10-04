# NeoForge 26.3

Water Optimisation v1.1.0 includes a NeoForge 26.3 build under
`neoforge-26.3/`. It is a full release built on the published NeoForge
`26.3.0.45-beta` coordinate. v1.1.1 will move it to the first stable NeoForge
26.3 release.

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
`neoforge-26.3/build/libs/water-optimisation-1.1.0-mc26.3-neoforge.jar`.
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

## Moving to stable NeoForge (v1.1.1)

When NeoForge publishes a stable 26.3 coordinate, update `neo_version` and
`neo_version_range` in `neoforge-26.3/gradle.properties`, re-check the renderer
hook descriptors against that runtime, rerun the build and CI smoke, and test
the exact JAR in a client before publishing.
