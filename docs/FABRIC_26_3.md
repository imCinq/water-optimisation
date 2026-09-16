# Fabric 26.3 preview

`0.0.10-26.3-preview.1` is a separate, **unpublished preview**, not an extension of the public 0.0.9 support claim. The public release remains Fabric 26.2/1.21.1 and NeoForge 26.2; no NeoForge 26.3 build is provided.

## Build and launch

Use Java 25, Fabric Loader `0.19.5`, and Fabric API `0.160.6+26.3`. Mod Menu `21.0.0-beta.1` is optional. The target uses `version_263=0.0.10-26.3-preview.1`; the default target and existing release versions remain unchanged.

From the repository root, with Java 25 selected:

```bash
./gradlew -Ptarget_minecraft=26.3 clean test build verifyArtifact
```

Runtime output:

```text
build/26.3/libs/water-optimisation-0.0.10-26.3-preview.1-mc26.3-fabric.jar
```

Install that runtime JAR, not the sources JAR, in an isolated Minecraft 26.3 Fabric client with the dependencies above. Test Mod Menu both installed and absent, and verify the `O` settings shortcut. For an interactive production-client launch with an available display:

```bash
./gradlew -Ptarget_minecraft=26.3 prodClient
```

Close the client manually when finished. For bounded startup testing, run `bash scripts/smoke-production-client.sh 26.3`. The build and production-smoke CI matrices now include 26.3, with a separate artifact upload path. Local packaged-JAR startup passed (initialization marker plus five seconds alive); CI has not yet run on GitHub. Mod Menu/Sodium combination checks remain outstanding.

## Scope and validation

The only target-specific modern client adaptation is the keyboard input adapter; renderer, UI, and other modern client code remain shared with 26.2. This is not a new renderer or a Sodium geometry bridge.

The 16 policy tests pass. That is policy coverage, **not in-world visual validation or FPS validation**; those checks remain outstanding. A successful build or startup does not establish that a rendering hook runs correctly in a world.

Before release, record checks against the exact runtime JAR:

- Compare mod off and on, including Performance and the opt-in Maximum FPS/reduced-inward-face mode. Test order-independent transparency (OIT) both on and off.
- Inspect above-water and underwater views, glass/transparent boundaries, flowing water, waterfalls, waterlogged blocks, enclosed source water, and chunk/block updates. Check missing faces, seams, overlays, and restoration after disabling the mod.
- In diagnostics, distinguish the saved setting, effective path, **hook observed**, and actual hidden-water **skips**. Exercise an eligible enclosed source-water scene; zero skips in an ineligible scene alone is not a hook failure. Disable diagnostics for final timing captures.
- Test without Sodium and, when a compatible 26.3 Sodium build is available, with its exact version recorded. Sodium must own geometry; Water Optimisation remains particle-only on that path. Record unavailable combinations as untested, not passed.
- Exercise both OpenGL and Vulkan on hardware/drivers that support them, with OIT on/off where available. Record unsupported combinations explicitly rather than claiming backend validation.
- Benchmark identical warmed scenes with the mod off/on, keeping camera, settings, backend, and companion mods fixed. Record hardware, versions, average FPS, 1% lows, and p95/p99 frame times using the [benchmark guidance](BENCHMARKING.md). No FPS gain is established or promised.

## Separate preview release

No preview release has been published. Before any manual publication, review the [release checklist](RELEASE_CHECKLIST.md), rerun the checks from a clean reviewed commit, inspect the packaged metadata and contents, and record the runtime JAR's SHA-256 checksum. Keep test/startup evidence separate from outstanding visual and performance checks.

Publish only as a separately labeled Fabric 26.3 **pre-release**, with its own version/tag, runtime JAR, optional matching sources, checksum, dependency pins, and explicit validation gaps. Do not replace 0.0.9 artifacts or label 26.3 as stable support. A stable support claim requires completed in-world visual and backend validation plus recorded mod-off/on benchmarks.
