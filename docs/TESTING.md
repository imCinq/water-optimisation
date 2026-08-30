# Testing

## Automated tests

The current remote workflow runs:

- Gradle wrapper validation;
- repository privacy audit;
- client-only boundary audit;
- Java 25 Minecraft 26.2 compilation;
- JUnit configuration tests;
- client and sources JAR packaging.

Implemented unit coverage includes:

- safe defaults and Balanced profile;
- profile reset behavior;
- Vanilla master-switch recovery;
- particle-distance clamping;
- null enum recovery;
- independent configuration copies.

Minecraft runtime behavior still needs the manual matrix below. More classifier tests should be added if the policy grows beyond the exact source-water subset.

## Manual visual tests

Run with the feature disabled and enabled in:

- flat source-water pools and large oceans;
- flowing water and waterfalls;
- waterlogged stairs, doors, slabs, and signs;
- leaves and transparent blocks;
- flooded caves and enclosed surfaces;
- underwater view;
- chunk loading and block updates;
- Sodium absent and present;
- Mod Menu installed and absent;
- OpenGL and Vulkan on Minecraft 26.2.

Look for missing top planes, disappearing sides, incorrect overlays, seams, z-fighting, wrong flow orientation, lighting differences, stale geometry after updates, unreadable labels, clipped descriptions, and settings that do not persist correctly.

## Performance measurements

Use the report template and compare identical warmed scenes with:

- feature disabled;
- Balanced;
- Performance;
- Advanced particle settings;
- Sodium absent/present;
- OpenGL/Vulkan separately.

Record average FPS, 1% lows, p95/p99 frame time, hitches, fluid tessellation time, section compilation time, translucent resort time, water blocks/faces, particle candidates/rejections, render distance, resolution, Java, backend, and companion mods. Use the diagnostics HUD averages as a local cross-check, not as a substitute for frame-time profiling.

## Multiplayer smoke test

Use a normal client-only session. Verify that the mod sends no custom packets, changes no controls, does not affect collision or movement, and exposes no player-information features. Check the current server rules before connecting.

Do not use private server logs or screenshots containing account information in the repository.

## Verification boundary

The current evidence is remote build/test/audit success, including compilation of the section and translucent-resort diagnostics hooks. The counters have not yet been validated in a live client. The local M2, graphics backend, visual, modpack, and DonutSMP checks are not marked complete until they are run.
