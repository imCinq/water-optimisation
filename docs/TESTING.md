# Testing

## Automated checks

Run:

```bash
./gradlew test build
bash scripts/audit-repository.sh
bash scripts/audit-client-only.sh
```

The CI workflow validates the Gradle wrapper, Java 25 toolchain, repository privacy, client-only boundary, unit tests, and client/sources JAR packaging.

Unit coverage includes:

- safe configuration defaults;
- profile reset behavior;
- master-switch recovery;
- particle-distance clamping;
- camera-relative particle-distance math;
- conservative fog scaling;
- null enum recovery;
- independent configuration copies.

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
- Mod Menu installed and absent;
- OpenGL and Vulkan where available.

Look for missing planes, seams, z-fighting, incorrect overlays, wrong flow orientation, lighting differences, stale geometry, unreadable labels, clipped descriptions, and settings that fail to persist.

## Performance measurements

Compare identical warmed scenes using the report template. Record average FPS, 1% lows, p95/p99 frame time, hitches, fluid tessellation, section compilation, translucent resorting, water blocks, interior fast-path skips, face counts from Tracy or mesh statistics, particle candidates/rejections, render distance, resolution, Java, backend, and companion mods.

Use the diagnostics HUD as a local cross-check, not as a replacement for frame-time profiling.

## Multiplayer smoke test

Verify that the mod sends no custom packets, changes no controls, does not affect collision or movement, and exposes no player-information features. Check the current server rules before connecting.

Do not commit private server logs or screenshots containing account information.

## Release boundary

CI can prove build and static checks. A stable release also needs a live Minecraft client run on the target hardware and the exact intended modpack/backend combination.
