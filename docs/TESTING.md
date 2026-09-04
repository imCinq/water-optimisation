# Testing

## Automated checks

Run:

```bash
./gradlew test build
bash scripts/audit-repository.sh
bash scripts/audit-client-only.sh
```

The CI workflow validates the Gradle wrapper, Java 25/21 target toolchains, repository privacy, client-only boundary, unit tests, and client/sources JAR packaging.

The release smoke workflow (`production-smoke.yml`) launches Loom's production
client task for both targets. That task assembles the same user-facing runtime
JAR path (the remapped JAR for 1.21.1 and the ordinary JAR for the no-remap
26.2 profile), supplies the matching Fabric API as a production mod, and
requires the mod's initialization log line to appear before the client remains
alive for a short grace period. It runs on version tags or by manual dispatch;
the smoke is a startup/crash check, not visual or performance evidence.

To run one target remotely or on a machine with the matching Java runtime and
an available display, use:

```bash
bash scripts/smoke-production-client.sh 26.2
bash scripts/smoke-production-client.sh 1.21.1
```

Set `PRODUCTION_SMOKE_USE_XVFB=true` on a headless Linux machine. The script
keeps its log under `build/production-smoke/` and bounds the client lifetime.

The remote build matrix also compiles Minecraft 1.21.1 with Java 21 and its target-isolated client sources. This proves packaging and API compatibility only; it does not replace live visual validation.

Unit coverage includes:

- safe configuration defaults;
- profile reset behavior;
- master-switch recovery;
- particle-distance clamping;
- camera-relative particle-distance math;
- conservative fog scaling;
- null enum recovery;
- future configuration versions are preserved without migration;
- independent configuration copies;
- separation of cosmetic settings from fluid-section invalidation.

## Manual visual matrix

Test with the feature disabled and enabled in:

- flat source-water pools and large oceans;
- flowing water and waterfalls;
- waterlogged stairs, doors, slabs, and signs;
- leaves and transparent blocks;
- flooded caves and enclosed surfaces;
- underwater views;
- chunk loading and block updates;
- Sodium absent and present;
- Minecraft 1.21.1 compatibility artifact, with Sodium absent and present;
- Mod Menu installed and absent;
- OpenGL and Vulkan where available.

For the Maximum FPS/reduced-face mode, additionally compare above-water, underwater, waterlogged, glass/leaves, cave, and surface-transition views. Confirm that flowing and waterlogged water remain vanilla, the outward source-water face remains present, note any missing inward faces, and verify that switching back to Performance rebuilds the sections.

Look for missing planes, seams, z-fighting, incorrect overlays, wrong flow orientation, lighting differences, stale geometry, unreadable labels, clipped descriptions, and settings that fail to persist.

## Performance measurements

Compare identical warmed scenes using the report template. Record average FPS, 1% lows, p95/p99 frame time, hitches, fluid tessellation, section compilation, translucent resorting, water blocks, fully hidden fast-path skips, removed reverse faces, total face counts from Tracy or mesh statistics, particle candidates/rejections, render distance, resolution, Java, backend, and companion mods.

Use the diagnostics HUD as a local cross-check, not as a replacement for frame-time profiling.

## Multiplayer smoke test

Verify that the mod sends no custom packets, changes no controls, does not affect collision or movement, and exposes no player-information features. Check the current server rules before connecting.

Do not commit private server logs or screenshots containing account information.

## Release boundary

CI can prove build and static checks. A stable release also needs a live Minecraft client run on the target hardware and the exact intended modpack/backend combination.
