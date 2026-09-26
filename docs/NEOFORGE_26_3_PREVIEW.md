# NeoForge 26.3 preview

Water Optimisation now has a separate NeoForge 26.3 development target under
`neoforge-26.3/`. It is an engineering preview, not part of the public v1.0.0
release yet.

## Why this target can start before stable NeoForge

NeoForge's official `26.3.x` source branch and the 26.3 NeoForm development
artifacts provide enough API and source information to port the mod now. The
preview build is pinned to the local coordinate `26.3.0-alpha.0+local`, which
is produced by building that NeoForge branch locally. No stable NeoForge
runtime is claimed or published by this project.

## Current scope

- Reuse the shared configuration, policy, diagnostics, assets, and particle
  behavior already used by the Fabric 26.3 target.
- Keep NeoForge's native Mods-screen configuration entry point.
- Keep Sodium as the geometry owner when it is present.
- Carry the existing NeoForge renderer mixins into a target-isolated preview,
  with the 26.3 `FluidRenderer.shouldRenderFace` descriptor updated for the
  new neighboring-`FluidState` signature. Further API drift is fixed here
  without touching the released Fabric jars.

The target is a port baseline, not a finished release port yet. Its first
compile still depends on a usable NeoForge 26.3 preview runtime publication.

## Local build

The NeoForge branch must first be available locally and published to the local
Maven cache with the preview coordinate used in
`neoforge-26.3/gradle.properties`. Then run:

```text
cd neoforge-26.3
../gradlew clean test build verifyArtifact --no-daemon --console=plain
```

The first run downloads and prepares Minecraft 26.3 and can take significantly
longer than an ordinary mod build. The output is a preview artifact; it must
not be uploaded as an official NeoForge release until NeoForge publishes a
stable 26.3 runtime coordinate. `verifyArtifact` checks the generated metadata,
client-only package boundary, icon, license, and required mixin classes.

For a published coordinate without editing the target, pass both overrides:

```text
../gradlew clean test build verifyArtifact \
  -Pneo_version_override=VERSION \
  -Pneo_version_range_override='[VERSION,)' \
  --no-daemon --console=plain
```

The same override mechanism is used by the manual GitHub Actions preview
workflow. It does not run on ordinary pushes or pull requests.

At the moment, the official `26.3.x` source checkout can prepare the Minecraft
26.3 artifacts locally, but its NeoForge publication is still blocked by
development-branch source/access-transformation errors. That is an upstream
preview-tooling limitation, not a claim that this mod already has a verified
NeoForge 26.3 JAR.

## Release gate

Before this target becomes official, update the NeoForge coordinate and range,
recheck the renderer mixin descriptors against the released runtime, build the
packaged client artifact, and perform a basic client startup check. FPS
benchmarking is not required for the loader port itself.
