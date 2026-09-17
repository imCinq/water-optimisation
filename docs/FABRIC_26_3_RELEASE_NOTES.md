> Superseded by the stable [0.0.10 release](https://github.com/imCinq/water-optimisation/releases/tag/v0.0.10). Kept below as the historical preview.2 record.

# Water Optimisation 0.0.10 — STABLE

This is a **supported stable Fabric 26.3 release**, an extension of the public 0.0.9 support matrix. No NeoForge 26.3 artifact is provided. No project-run in-game tests were run for this hardening; CI passed for the current SHA for builds and startup smoke per runs 35237266215 and 35237266249, and a dated user-reported client pass is recorded below. Visual correctness and FPS benefits are not validated.

## Target and installation

- Minecraft **26.3**, Fabric, client only.
- Java **25**, Fabric Loader **0.19.5**.
- Fabric API **0.160.6+26.3**; generated metadata requires `>=0.160.6+26.3` for the 26.3 target only.
- Optional Mod Menu **21.0.0-beta.1**.
- Build selector: `-Ptarget_minecraft=26.3`.
- Runtime artifact target: `build/26.3/libs/water-optimisation-0.0.10-mc26.3-fabric.jar`.

Use the runtime JAR, not the sources JAR, in an isolated matching client. Build and launch instructions are in the [Fabric 26.3 guide](FABRIC_26_3.md). This release does not replace existing 0.0.9 artifacts.

## Hardening changes

- **Pinned build download:** the Gradle 9.7.1 wrapper now pins the distribution SHA-256 checksum.
- **Configuration-log privacy:** modern Fabric, legacy Fabric 1.21.1, and NeoForge use a fixed relative configuration label and generic error categories rather than logging absolute paths, configuration values, or exception payloads.
- **Invocation cleanup:** the shared modern fluid mixin uses a real MixinExtras `@WrapMethod` with try/finally around tessellation. `TessellationContext` clears per-worker eligibility on ordinary returns, cancellation, and exceptions and restores outer eligibility after nested calls. Fluid diagnostics closure uses the entry-time flag rather than re-reading an enabled flag at return.
- **Ceiling-water correction:** shared modern Fabric and NeoForge 26.2 now require ordinary source water **above** before skipping a hidden source-water block. Source water or solid-rendering blocks may still hide the down/side faces. A solid ceiling alone does not hide a water surface below full block height. The earlier dismissal of this visual-review finding was incorrect; the [dated historical audit follow-up](FABRIC_26_3_CHECKLIST_AUDIT.md) records the correction.
- **API minimum:** Fabric 26.3 metadata enforces `>=0.160.6+26.3`; this does not change older targets' API metadata behavior.

The target-specific 26.3 client adaptation remains the keyboard input adapter; other modern client code is shared with 26.2. This is not a new renderer or a Sodium geometry bridge. Cross-target source fixes do not retroactively modify published artifacts.

## Validation status

The newer historical local Fabric 26.3 result is 25 tests passed via `test`: 16 config-model tests, 4 context helper tests, and 5 mocked predicate tests. Earlier evidence separately records `./gradlew -Ptarget_minecraft=26.3 test build verifyArtifact --rerun-tasks` passing with 20 tests, with forced reruns and clean repository/client-only audits and whitespace checks; it does not establish a 25-test full-build pass. A separate NeoForge 26.2 build passed 20 tests before the new mocked predicate fixture was added. Context tests cover ordinary/cancelled returns, exceptional cleanup, nested eligibility restoration, and worker isolation. These are isolated Java tests, not transformed-Mixin or water-scene tests, and config-model tests are not captured logging tests. Transformed-Mixin cancellation/exception behavior remains an open nonvisual gate. Startup success alone does not prove optional local-capture hook execution. This document does not claim 0.0.10 scene or performance validation.

CI passed for the current SHA for builds and startup smoke per runs 35237266215 and 35237266249. No project-run in-game tests were run for this hardening. Earlier local packaged-JAR startup evidence belongs to preview.1 and is not 0.0.10 exact-artifact validation. Packaged-artifact startup evidence for 0.0.10 is the current-SHA smoke run; no transformed-Mixin evidence exists for 0.0.10. Beyond the local test/build results above, the exact 0.0.10 runtime still needs recorded metadata/archive inspection, checksum, startup, hook-observation, and scene evidence under the [release checklist](RELEASE_CHECKLIST.md).

User test on 2026-09-17: CI runtime `water-optimisation-0.0.10-mc26.3-fabric.jar` with SHA-256 `d50d3074a14a0bd70daa7474e91f4f5d54a8751d6ceb5f4a8f84be6ab0db7d24` from build run 35237266215 at source SHA `6ec94a9374a1109cd129afc799ce0781d9868ce8` was reported to pass by a user on Apple M2, macOS 27, OpenGL backend, Sodium absent, Mod Menu present. This records only that tested combination: the OIT on/off matrix, FPS benchmarks, transformed-Mixin proof, and hook observation remain untested, not passed. A follow-up user pass on 2026-09-17 exercised Mod Menu present and most settings with no failures observed on the same Apple M2, macOS 27, OpenGL, Sodium-absent configuration and the same runtime SHA-256 `d50d3074a14a0bd70daa7474e91f4f5d54a8751d6ceb5f4a8f84be6ab0db7d24`. A further user pass on 2026-09-17 covered Sodium present and the Vulkan backend with no failures observed, on Apple M2, macOS 27, Mod Menu present, with runtime SHA-256 `d50d3074a14a0bd70daa7474e91f4f5d54a8751d6ceb5f4a8f84be6ab0db7d24`; the Sodium version and tested scenes were not specified.

## Known limitations and unverified combinations

- **Opt-in underwater backface loss:** Maximum FPS/reduced-inward-face mode deliberately omits optional reverse faces for ordinary source water. Underwater or transparent-boundary views can lose inward faces. The cleanup fix does not remove this visual trade-off; disable reduced geometry for a conservative comparison.
- **Optional local-capture ambiguity:** the hidden-water hook remains `require=0` with `CAPTURE_FAILSOFT`. A mismatch may safely leave vanilla tessellation running without the optimization. Selected/effective-active settings alone do not prove that the hook was applied or observed. Zero skips in an ineligible scene are not proof of failure either.
- **Renderer ownership:** detected Sodium owns geometry and leaves this mod particle-only. Unknown replacement renderers are not covered by that detection; compatibility with them, Iris/shaders, and other transparency/chunk mods is not established.
- **Visual gaps:** ceiling-water scenes, above/underwater views, flowing and waterlogged water, transparent boundaries, overlays, updates, camera transitions, OIT on/off, and supported OpenGL/Vulkan combinations still need exact-artifact checks.
- **Performance gaps:** no validated mod-off/on FPS, frame-time, CPU/GPU, or allocation measurements establish a gain or negligible overhead. No universal FPS improvement is promised.

Water Optimisation remains client-only: no fluid simulation, gameplay, movement, collision, networking, or server-state changes are intended. This release carries no server-approval or publication claim.
