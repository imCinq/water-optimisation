# Fabric 26.3

`v1.0.0` is the current public Fabric release for Minecraft 26.3. The release also provides a separate Fabric 1.21.11 artifact; NeoForge 26.3 is deferred and no NeoForge artifact is included.

## Build and launch

Use Java 25, Fabric Loader `0.19.5`, and Fabric API `0.160.6+26.3`. The generated API metadata floor is `>=0.160.6+26.3`. Mod Menu `21.0.0-beta.1` is optional. The target uses `version_263=1.0.0` and produces the current v1.0.0 artifact.

From the repository root, with Java 25 selected:

```bash
./gradlew -Ptarget_minecraft=26.3 clean test build verifyArtifact
```

Runtime output:

```text
build/26.3/libs/water-optimisation-1.0.0-mc26.3-fabric.jar
```

Install that runtime JAR, not the sources JAR, in an isolated Minecraft 26.3 Fabric client with the dependencies above. Test Mod Menu both installed and absent, and verify the `O` settings shortcut. For an interactive production-client launch with an available display:

```bash
./gradlew -Ptarget_minecraft=26.3 prodClient
```

Close the client manually when finished. For bounded startup testing, run `bash scripts/smoke-production-client.sh 26.3`. The build and production-smoke CI matrices include 26.3 and 1.21.11. Historical preview startup evidence is retained below; it is not a substitute for exact-artifact v1.0.0 validation. Mod Menu/Sodium combination checks remain user validation.

## Scope and validation

The only target-specific modern client adaptation is the keyboard input adapter; renderer, UI, and other modern client code remain shared with the Fabric 26.3 path. This is not a new renderer or a Sodium geometry bridge.

0.0.10 pins the Gradle distribution checksum and makes configuration logs privacy-safe in modern Fabric, legacy Fabric 1.21.1, and NeoForge. The shared modern fluid mixin now uses a real MixinExtras `@WrapMethod` try/finally with `TessellationContext`: ordinary returns, cancellation, and exceptions clear invocation eligibility, while nested calls restore the outer eligibility. Fluid diagnostics closure uses the entry-time flag. The modern and NeoForge hidden-water predicates now require ordinary source water above, while still allowing solid-rendering down/side neighbors. A solid ceiling alone does not hide the water surface beneath it; see the explicit correction in the [historical audit follow-up](FABRIC_26_3_CHECKLIST_AUDIT.md).

The newer historical local Fabric 26.3 result is 25 tests passed via `test`: 16 config-model tests, 4 context helper tests, and 5 mocked predicate tests. Earlier evidence separately records `./gradlew -Ptarget_minecraft=26.3 test build verifyArtifact --rerun-tasks` passing with 20 tests, with forced reruns and clean repository/client-only audits and whitespace checks; this is not a 25-test full-build claim. A separate NeoForge 26.2 build passed 20 tests before the new mocked predicate fixture was added. No project-run in-game tests were run for this hardening; current-SHA CI passed for builds and startup smoke per runs 35237266215 and 35237266249. Context tests cover ordinary/cancelled returns, exceptional exit, nested eligibility, and worker isolation—not a transformed Minecraft renderer. Transformed-Mixin cancellation/exception behavior remains an open nonvisual gate. Startup success alone does not prove that the optional local-capture hook executed. In-world visual and FPS checks remain outstanding. A successful build or startup does not establish that a rendering hook runs correctly in a world.

User test on 2026-09-17: CI runtime `water-optimisation-0.0.10-mc26.3-fabric.jar` with SHA-256 `d50d3074a14a0bd70daa7474e91f4f5d54a8751d6ceb5f4a8f84be6ab0db7d24` from build run 35237266215 at source SHA `6ec94a9374a1109cd129afc799ce0781d9868ce8` was reported to pass by a user on Apple M2, macOS 27, OpenGL backend, Sodium absent, Mod Menu present. This records only that tested combination: the OIT on/off matrix, FPS benchmarks, transformed-Mixin proof, and hook observation remain untested, not passed. A follow-up user pass on 2026-09-17 exercised Mod Menu present and most settings with no failures observed on the same Apple M2, macOS 27, OpenGL, Sodium-absent configuration and the same runtime SHA-256 `d50d3074a14a0bd70daa7474e91f4f5d54a8751d6ceb5f4a8f84be6ab0db7d24`. A further user pass on 2026-09-17 covered Sodium present and the Vulkan backend with no failures observed, on Apple M2, macOS 27, Mod Menu present, with runtime SHA-256 `d50d3074a14a0bd70daa7474e91f4f5d54a8751d6ceb5f4a8f84be6ab0db7d24`; the Sodium version and tested scenes were not specified.

Known limitations remain: opt-in reduced backfaces can cause underwater inward-face loss; the optional local-capture fast-path hook can fail soft, so selected/active is not proof of hook availability; and Sodium detection does not establish compatibility with unknown replacement renderers. Hardening does not resolve these limitations or establish visual equivalence.

Before release, record checks against the exact runtime JAR:

- Compare mod off and on, including Performance and the opt-in Maximum FPS/reduced-inward-face mode. Test order-independent transparency (OIT) both on and off.
- Inspect above-water and underwater views, water directly beneath solid ceilings, glass/transparent boundaries, flowing water, waterfalls, waterlogged blocks, enclosed source water, and chunk/block updates. Check missing faces, seams, overlays, and restoration after disabling the mod.
- In diagnostics, distinguish the saved setting, effective path, **hook observed**, and actual hidden-water **skips**. Exercise an eligible enclosed source-water scene; zero skips in an ineligible scene alone is not a hook failure. Disable diagnostics for final timing captures.
- Test without Sodium and, when a compatible 26.3 Sodium build is available, with its exact version recorded. Sodium must own geometry; Water Optimisation remains particle-only on that path. Record unavailable combinations as untested, not passed.
- Exercise both OpenGL and Vulkan on hardware/drivers that support them, with OIT on/off where available. Record unsupported combinations explicitly rather than claiming backend validation.
- Benchmark identical warmed scenes with the mod off/on, keeping camera, settings, backend, and companion mods fixed. Record hardware, versions, average FPS, 1% lows, and p95/p99 frame times using the [benchmark guidance](BENCHMARKING.md). No FPS gain is established or promised.

## Stable release

Before any manual publication, review the [release checklist](RELEASE_CHECKLIST.md), rerun the checks from a clean reviewed commit, inspect the packaged metadata and contents, and record the runtime JAR's SHA-256 checksum. Keep test/startup evidence separate from outstanding visual and performance checks.

Publish as a stable Fabric 26.3 release, with its own version/tag, runtime JAR, optional matching sources, checksum, dependency pins, and explicit validation gaps. Do not replace 0.0.9 artifacts. A stable support claim requires completed in-world visual and backend validation plus recorded mod-off/on benchmarks.
