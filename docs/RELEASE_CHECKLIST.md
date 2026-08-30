# Public release checklist

Use this checklist before publishing a Water Optimisation artifact. A passing CI build is necessary but does not by itself establish visual equivalence, an FPS improvement, or server compatibility.

## Product and metadata

- [ ] The release version is consistent in `gradle.properties`, generated `fabric.mod.json`, `CHANGELOG.md`, README release notes, and the GitHub Release.
- [ ] The runtime product name and description remain focused on Water Optimisation; creator attribution is limited to project metadata, the license, and project documentation.
- [ ] The README displays the creator logo from `assets/`; the logo is not packaged as a runtime mod resource.
- [ ] Installation requirements state Minecraft 26.2, Java 25, Fabric Loader 0.19.3 or newer, Fabric API 0.158.0+26.2, and optional Mod Menu 19.0.0-alpha.1.
- [ ] Known limitations, visual trade-offs, fallback behavior, and the client-only boundary are documented.

## Source and privacy

- [ ] `bash scripts/audit-repository.sh` passes.
- [ ] `bash scripts/audit-client-only.sh` passes.
- [ ] No credentials, tokens, personal contact details, local filesystem paths, account identifiers, server data, screenshots with identifiers, or generated runtime files are included.
- [ ] No telemetry, update checker, outbound network call, custom packet, movement change, or world-simulation change was added.

## Automated build

- [ ] A clean `./gradlew clean test build` passes.
- [ ] The Gradle wrapper validation passes.
- [ ] The exact commit and CI run are recorded.
- [ ] The runtime JAR and sources JAR are identified under `build/libs/`.
- [ ] The runtime JAR contents are inspected and contain no build cache, logs, screenshots, or private data.
- [ ] SHA-256 checksums are calculated for the files that will be published.

## Local Minecraft validation

- [ ] The exact artifact is tested in a clean Minecraft 26.2 Fabric client.
- [ ] Disabled, Balanced, and Performance modes are compared in the same warmed scenes.
- [ ] Flat water, oceans, flowing water, waterfalls, waterlogged blocks, leaves, transparent blocks, flooded caves, and underwater views are checked.
- [ ] No missing planes, seams, z-fighting, overlay errors, lighting differences, wrong flow orientation, stale geometry, or clipped settings text are observed.
- [ ] A normal non-water scene shows no unacceptable regression.
- [ ] Sodium absent and present are tested with the exact companion versions.
- [ ] OpenGL and Vulkan are tested separately where available.
- [ ] Mod Menu present and absent, keybind behavior, persistence, Cancel, and Escape are checked.
- [ ] Average FPS, 1% lows, p95/p99 frame time, hitches, fluid compilation, section compilation, translucent resorting, and particle counters are recorded where relevant.

## Multiplayer and publication

- [ ] The client-only multiplayer smoke test confirms no custom packets, movement or collision changes, player-information features, or fluid-simulation changes.
- [ ] Current server rules are checked before making a multiplayer compatibility statement.
- [ ] The changelog, compatibility notes, release notes, and artifact checksum are ready.
- [ ] The GitHub Release is created from the accepted tag and clearly marked preview or stable according to the evidence.
- [ ] No universal FPS or server-approval claim appears in the listing.
- [ ] Modrinth or another distribution platform is used only after its project permissions and package metadata are reviewed.

## Evidence record

Record the accepted commit, artifact filenames, checksums, CI run, target hardware, operating system, renderer backend, companion mods, resource packs/shaders, scene settings, sample duration, and visual result. Keep private logs and account information out of the repository.
