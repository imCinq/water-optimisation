# Public release checklist

Use this checklist before publishing a Water Optimisation artifact. A passing CI build is necessary but does not by itself establish visual equivalence, an FPS improvement, or server compatibility.

For **v1.1.0**, the release targets are Fabric 26.3 (`build/26.3/libs/water-optimisation-1.1.0-mc26.3-fabric.jar`), Fabric 1.21.11 (`build/libs/water-optimisation-1.1.0.jar`, published as `water-optimisation-1.1.0-mc1.21.11-fabric.jar`), NeoForge 1.21.11 (`neoforge-1.21.11/build/libs/water-optimisation-1.1.0-mc1.21.11-neoforge.jar`), and Forge 26.3 (`forge-26.3/build/libs/water-optimisation-1.1.0-mc26.3-forge.jar`, one-time release). Build them with `./gradlew -Ptarget_minecraft=<26.3|1.21.11> clean test build verifyArtifact` and `./gradlew -p <forge-26.3|neoforge-1.21.11> clean test build verifyArtifact`. Fabric 26.3 and Fabric 1.21.11 write to different folders, but a `clean` of one Fabric target removes the other's output, so copy each jar out before building the next. Fabric 26.2, Fabric 1.21.1, and NeoForge 26.2 are historical 0.0.10 targets.

## Product and metadata

- [ ] The release version is consistent in `gradle.properties`, generated Fabric/NeoForge metadata, `CHANGELOG.md`, README release notes, and the GitHub Release.
- [ ] The runtime product name and description remain focused on Water Optimisation; creator attribution is limited to project metadata, the license, and project documentation.
- [ ] The README displays the creator logo from `assets/`, and the packaged runtime icon at `src/main/resources/assets/wateroptimisation/icon.png` is referenced by `fabric.mod.json`.
- [ ] Installation requirements state the exact v1.0.0 release targets: Fabric 26.3 with Java 25, Fabric Loader 0.19.5+, Fabric API 0.160.6+26.3, and optional Mod Menu 21.0.0-beta.1; or Fabric 1.21.11 with Java 21, Fabric Loader 0.18.5+, Fabric API 0.141.4+1.21.11, and optional Mod Menu 17.0.1-beta.1.
- [ ] Known limitations, visual trade-offs, fallback behavior, and the client-only boundary are documented.

## Source and privacy

- [ ] `bash scripts/audit-repository.sh` passes.
- [ ] `bash scripts/audit-client-only.sh` passes.
- [ ] No credentials, tokens, personal contact details, local filesystem paths, account identifiers, server data, screenshots with identifiers, or generated runtime files are included.
- [ ] No telemetry, update checker, outbound network call, custom packet, movement change, or world-simulation change was added.

## Automated build

- [ ] Clean target-specific builds pass: `./gradlew -Ptarget_minecraft=26.3 clean test build verifyArtifact` and `./gradlew -Ptarget_minecraft=1.21.11 clean test build verifyArtifact`.
- [ ] The Gradle wrapper validation passes, including the pinned distribution checksum.
- [ ] The exact commit, test results (historical local Fabric 26.3: 25 passed via `test`—16 config-model, 4 context helper, 5 mocked predicate; earlier forced `test build verifyArtifact` pass: 20; separate NeoForge 26.2 build: 20 before the new fixture), and current-SHA CI runs are recorded; current-SHA builds and startup smoke passed per runs 35237266215 and 35237266249.
- [ ] The runtime JAR and sources JAR are identified in the selected target's output: `build/26.3/libs/` for Fabric 26.3 and `build/libs/` for Fabric 1.21.11.
- [ ] The runtime JAR contents are inspected and contain no build cache, logs, screenshots, or private data.
- [ ] SHA-256 checksums are calculated for the files that will be published.
- [ ] The production startup smoke workflow passes for every artifact being released, including the exact Fabric 26.3 artifact when selected; sanitized logs are retained with the release evidence.
- [ ] NeoForge 26.3 is not included in v1.0.0; its separate preview/release checks are tracked below.

## NeoForge 26.3 preview and future release

- [ ] The pinned NeoForge coordinate in `neoforge-26.3/gradle.properties` resolves from the official release repository or a documented local preview publication.
- [ ] From `neoforge-26.3/`, `../gradlew clean test build verifyArtifact --no-daemon --console=plain` passes on Java 25.
- [ ] The generated `META-INF/neoforge.mods.toml` contains the final NeoForge and Minecraft version ranges, and the target-local artifact verifier passes.
- [ ] The exact preview/runtime JAR starts a client and logs the Water Optimisation initialization marker without mixin audit failures.
- [ ] The preview package contains no Fabric metadata, private files, logs, screenshots, or generated runtime data.
- [ ] The renderer hook descriptors are checked against the exact released NeoForge/Minecraft runtime; the 26.3 `FluidRenderer.shouldRenderFace` neighboring-`FluidState` signature is specifically covered.
- [ ] The preview is labelled pre-release until the target has documented client validation and publication evidence comparable to the Fabric release line.

## Local Minecraft validation

- [ ] The exact artifacts are tested in clean clients matching the selected v1.0.0 targets, including Minecraft 26.3 Fabric and 1.21.11 Fabric.
- [ ] Disabled, Balanced, and Performance modes are compared in the same warmed scenes; opt-in Maximum FPS/reduced geometry is evaluated separately with its underwater backface-loss caveat.
- [ ] Flat water, water beneath solid ceilings, oceans, flowing water, waterfalls, waterlogged blocks, leaves, transparent blocks, flooded caves, and underwater views are checked. — 17 September 2026: a user general pass was reported on the stated configuration (Apple M2, macOS 27, OpenGL, Sodium absent, Mod Menu present); this item-by-item scene matrix was not individually verified.
- [ ] Fabric 26.3 OIT on/off is compared where available; hook observation and actual skips are recorded in an eligible scene rather than inferred from selected/active settings. — untested: no OIT on/off comparison was performed.
- [ ] Optional local-capture availability and unknown-renderer/mod-combination limitations are recorded without inferring compatibility from the Sodium gate.
- [ ] No missing planes, seams, z-fighting, overlay errors, lighting differences, wrong flow orientation, stale geometry, or clipped settings text are observed.
- [ ] A normal non-water scene shows no unacceptable regression.
- [ ] Sodium absent and present are tested with the exact companion versions. — untested: only Sodium absent was exercised; 17 September 2026: Sodium present was tested with the version unrecorded and no failures were observed. Exact companion versions, resource packs, and shaders remain untested.
- [ ] OpenGL and Vulkan are tested separately where available. — untested: only OpenGL was exercised; 17 September 2026: Vulkan was tested on Apple M2 with no failures observed. Exact version details remain unrecorded and untested.
- [ ] Mod Menu present and absent, keybind behavior, persistence, Cancel, and Escape are checked. — 17 September 2026: Mod Menu present was tested with most settings and no failures were observed; Mod Menu absent, keybind behavior, persistence, Cancel, and Escape remain untested.
- [ ] Average FPS, 1% lows, p95/p99 frame time, hitches, fluid compilation, section compilation, translucent resorting, and particle counters are recorded where relevant. — untested: no FPS, frame-time, compilation, or counter measurements were recorded.

## Multiplayer and publication

- [ ] The client-only multiplayer smoke test confirms no custom packets, movement or collision changes, player-information features, or fluid-simulation changes.
- [ ] Current server rules are checked before making a multiplayer compatibility statement.
- [ ] The changelog, compatibility notes, release notes, and artifact checksum are ready.
- [ ] The GitHub Release is created from the accepted tag and clearly marked stable, with any remaining limitations stated.
- [ ] No universal FPS or server-approval claim appears in the listing.
- [ ] Modrinth or another distribution platform is used only after its project permissions and package metadata are reviewed.

## Evidence record

Record the accepted commit, artifact filenames, checksums, CI run, target hardware, operating system, renderer backend, companion mods, resource packs/shaders, scene settings, sample duration, and visual result. Keep private logs and account information out of the repository.

- 17 September 2026 user general pass. Accepted commit: `6ec94a9374a1109cd129afc799ce0781d9868ce8`. Artifact: `build/26.3/libs/water-optimisation-0.0.10-26.3-preview.2-mc26.3-fabric.jar`. Artifact SHA-256: `d50d3074a14a0bd70daa7474e91f4f5d54a8751d6ceb5f4a8f84be6ab0db7d24`.
- CI runs: https://github.com/imCinq/water-optimisation/actions/runs/35237266215 and https://github.com/imCinq/water-optimisation/actions/runs/35237266249.
- User configuration: Apple M2 hardware, macOS 27, OpenGL renderer backend, Sodium absent, Mod Menu present. Resource packs/shaders, scene settings, and sample duration were not recorded.
- Visual result: a general client pass was reported on the configuration above. The full scene matrix, OIT on/off, the unrecorded Sodium version and Vulkan detail checks, and all FPS/frame-time/compilation/particle benchmark gates remain unchecked and untested; no stable acceptance is implied.
- 17 September 2026 follow-up user pass: Mod Menu present and most settings were covered with no failures observed, on the same configuration and artifact SHA-256 `d50d3074a14a0bd70daa7474e91f4f5d54a8751d6ceb5f4a8f84be6ab0db7d24`.
- 17 September 2026 further user pass: Sodium present and the Vulkan backend were covered with no failures observed, against artifact SHA-256 `d50d3074a14a0bd70daa7474e91f4f5d54a8751d6ceb5f4a8f84be6ab0db7d24`. Sodium version and scenes were not specified.
- 22 September 2026 local regression pass: Fabric 26.3 `clean test build verifyArtifact` and Fabric 1.21.11 `clean test build verifyArtifact` both passed after the NeoForge 26.3 release-preparation changes. No NeoForge runtime JAR was produced because the pinned `26.3.0-alpha.0+local` coordinate is not available.
- The same local build produced Fabric 26.3 runtime SHA-256 `8643fe4748feab5abb53bc3f8ed71a0fe9026b0a4aafc92f13c6016b797bfaf8` and Fabric 1.21.11 runtime SHA-256 `09daccab6e514a8c48631e1b6b986161d3c8806e4bf91f433703e8a15b6cb6fa`; package spot checks found the expected metadata, mixin descriptor, icon, and license entries.
