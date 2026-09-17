# Public release checklist

Use this checklist before publishing a Water Optimisation artifact. A passing CI build is necessary but does not by itself establish visual equivalence, an FPS improvement, or server compatibility.

For the separate **Fabric 26.3 `0.0.10-26.3-preview.2`** target, use the [preview guide](FABRIC_26_3.md) and [PREVIEW release notes](FABRIC_26_3_RELEASE_NOTES.md). Target the exact runtime JAR at `build/26.3/libs/water-optimisation-0.0.10-26.3-preview.2-mc26.3-fabric.jar`; do not substitute a default-target artifact or overwrite public 0.0.9 artifacts. The newer historical local Fabric 26.3 result is 25 tests passed via `test`: 16 config-model tests, 4 context helper tests, and 5 mocked predicate tests. Earlier evidence separately records `./gradlew -Ptarget_minecraft=26.3 test build verifyArtifact --rerun-tasks` passing with 20 tests, with forced reruns and clean repository/client-only audits and whitespace checks; it is not a 25-test full-build result. A separate NeoForge 26.2 build passed 20 tests before the new mocked predicate fixture was added. Current-SHA CI passed for builds and startup smoke per runs 35237266215 and 35237266249, and no project-run in-game tests were run for this hardening. Transformed-Mixin cancellation/exception validation remains an open nonvisual gate; wrapper helper tests do not demonstrate it, and startup success alone does not prove optional local-capture hook execution. All gates below remain unchecked; record unavailable checks as untested, not passed, and do not claim stable support. The [checklist audit](FABRIC_26_3_CHECKLIST_AUDIT.md) is historical and includes a dated correction, not a current acceptance record. **User test — 17 September 2026:** a user reported a general pass on Apple M2, macOS 27, OpenGL, Sodium absent, Mod Menu present, against the exact CI runtime SHA-256 `d50d3074a14a0bd70daa7474e91f4f5d54a8751d6ceb5f4a8f84be6ab0db7d24`. This is a single general client pass on one configuration; it does not establish the unchecked visual, backend, companion-mod, or performance gates below.

## Product and metadata

- [ ] The release version is consistent in `gradle.properties`, generated Fabric/NeoForge metadata, `CHANGELOG.md`, README release notes, and the GitHub Release.
- [ ] The runtime product name and description remain focused on Water Optimisation; creator attribution is limited to project metadata, the license, and project documentation.
- [ ] The README displays the creator logo from `assets/`, and the packaged runtime icon at `src/main/resources/assets/wateroptimisation/icon.png` is referenced by `fabric.mod.json`.
- [ ] Installation requirements state the exact release target: Minecraft 26.3 Fabric PREVIEW with Java 25, Fabric Loader 0.19.5+, Fabric API 0.160.6+26.3 (metadata floor `>=0.160.6+26.3` for 26.3 only), and optional Mod Menu 21.0.0-beta.1; Minecraft 26.2 Fabric with Java 25, Fabric Loader 0.19.3+, Fabric API 0.158.0+26.2, and optional Mod Menu 19.0.0-alpha.1; Minecraft 26.2 NeoForge with Java 25 and NeoForge 26.2.0.77+; or Minecraft 1.21.1 Fabric with Java 21, Fabric Loader 0.16.13+, Fabric API 0.116.12+1.21.1, and optional Mod Menu 11.0.4.
- [ ] Known limitations, visual trade-offs, fallback behavior, and the client-only boundary are documented.

## Source and privacy

- [ ] `bash scripts/audit-repository.sh` passes.
- [ ] `bash scripts/audit-client-only.sh` passes.
- [ ] No credentials, tokens, personal contact details, local filesystem paths, account identifiers, server data, screenshots with identifiers, or generated runtime files are included.
- [ ] No telemetry, update checker, outbound network call, custom packet, movement change, or world-simulation change was added.

## Automated build

- [ ] A clean target-specific build passes: `./gradlew -Ptarget_minecraft=26.3 clean test build verifyArtifact` for the Fabric 26.3 preview; use the matching selector for other targets.
- [ ] The Gradle wrapper validation passes, including the pinned distribution checksum.
- [ ] The exact commit, test results (historical local Fabric 26.3: 25 passed via `test`—16 config-model, 4 context helper, 5 mocked predicate; earlier forced `test build verifyArtifact` pass: 20; separate NeoForge 26.2 build: 20 before the new fixture), and current-SHA CI runs are recorded; current-SHA builds and startup smoke passed per runs 35237266215 and 35237266249.
- [ ] The runtime JAR and sources JAR are identified in the selected target's output: `build/26.3/libs/` for Fabric 26.3, not the default `build/libs/`.
- [ ] The runtime JAR contents are inspected and contain no build cache, logs, screenshots, or private data.
- [ ] SHA-256 checksums are calculated for the files that will be published.
- [ ] The production startup smoke workflow passes for every artifact being released, including the exact Fabric 26.3 preview artifact when selected; sanitized logs are retained with the release evidence.
- [ ] If releasing NeoForge 26.2, its runtime JAR passes archive-content, packaged-client Mixin audit, and packaged dedicated-server smoke checks; no NeoForge 26.3 artifact is implied.

## Local Minecraft validation

- [ ] The exact artifacts are tested in clean clients matching the selected targets, including Minecraft 26.3 Fabric for this preview; shared modern changes also receive 26.2 regression checks.
- [ ] Disabled, Balanced, and Performance modes are compared in the same warmed scenes; opt-in Maximum FPS/reduced geometry is evaluated separately with its underwater backface-loss caveat.
- [ ] Flat water, water beneath solid ceilings, oceans, flowing water, waterfalls, waterlogged blocks, leaves, transparent blocks, flooded caves, and underwater views are checked. — 17 September 2026: a user general pass was reported on the stated configuration (Apple M2, macOS 27, OpenGL, Sodium absent, Mod Menu present); this item-by-item scene matrix was not individually verified.
- [ ] Fabric 26.3 OIT on/off is compared where available; hook observation and actual skips are recorded in an eligible scene rather than inferred from selected/active settings. — untested: no OIT on/off comparison was performed.
- [ ] Optional local-capture availability and unknown-renderer/mod-combination limitations are recorded without inferring compatibility from the Sodium gate.
- [ ] No missing planes, seams, z-fighting, overlay errors, lighting differences, wrong flow orientation, stale geometry, or clipped settings text are observed.
- [ ] A normal non-water scene shows no unacceptable regression.
- [ ] Sodium absent and present are tested with the exact companion versions. — untested: only Sodium absent was exercised; Sodium present was not.
- [ ] OpenGL and Vulkan are tested separately where available. — untested: only OpenGL was exercised; Vulkan was not.
- [ ] Mod Menu present and absent, keybind behavior, persistence, Cancel, and Escape are checked.
- [ ] Average FPS, 1% lows, p95/p99 frame time, hitches, fluid compilation, section compilation, translucent resorting, and particle counters are recorded where relevant. — untested: no FPS, frame-time, compilation, or counter measurements were recorded.

## Multiplayer and publication

- [ ] The client-only multiplayer smoke test confirms no custom packets, movement or collision changes, player-information features, or fluid-simulation changes.
- [ ] Current server rules are checked before making a multiplayer compatibility statement.
- [ ] The changelog, compatibility notes, release notes, and artifact checksum are ready.
- [ ] The GitHub Release is created from the accepted tag and clearly marked preview or stable according to the evidence.
- [ ] No universal FPS or server-approval claim appears in the listing.
- [ ] Modrinth or another distribution platform is used only after its project permissions and package metadata are reviewed.

## Evidence record

Record the accepted commit, artifact filenames, checksums, CI run, target hardware, operating system, renderer backend, companion mods, resource packs/shaders, scene settings, sample duration, and visual result. Keep private logs and account information out of the repository.

- 17 September 2026 user general pass. Accepted commit: `6ec94a9374a1109cd129afc799ce0781d9868ce8`. Artifact: `build/26.3/libs/water-optimisation-0.0.10-26.3-preview.2-mc26.3-fabric.jar`. Artifact SHA-256: `d50d3074a14a0bd70daa7474e91f4f5d54a8751d6ceb5f4a8f84be6ab0db7d24`.
- CI runs: https://github.com/imCinq/water-optimisation/actions/runs/35237266215 and https://github.com/imCinq/water-optimisation/actions/runs/35237266249.
- User configuration: Apple M2 hardware, macOS 27, OpenGL renderer backend, Sodium absent, Mod Menu present. Resource packs/shaders, scene settings, and sample duration were not recorded.
- Visual result: a general client pass was reported on the configuration above. The full scene matrix, OIT on/off, Sodium-present, Vulkan, and all FPS/frame-time/compilation/particle benchmark gates remain unchecked and untested; no stable acceptance is implied.
