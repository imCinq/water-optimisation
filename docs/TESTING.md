# Testing

## Automated tests

Planned unit coverage includes:

- configuration defaults, clamping, recovery, and atomic writes;
- same-fluid face visibility;
- full-block and partial-shape occlusion;
- waterlogged and overlay fallbacks;
- corner-height and flat-surface classification;
- particle distance and fog decisions;
- disabled-mode no-op behavior;
- diagnostics counter boundaries.

## Manual visual tests

Test with the feature disabled and enabled in:

- flat source-water pools;
- flowing water and waterfalls;
- waterlogged stairs, doors, slabs, and signs;
- leaves and transparent blocks;
- flooded caves and enclosed surfaces;
- underwater view;
- chunk loading and block updates;
- Sodium absent and present;
- OpenGL and Vulkan on Minecraft 26.2.

Look for missing top planes, disappearing sides, incorrect overlays, seams, z-fighting, wrong flow orientation, lighting differences, and stale geometry after updates.

## Multiplayer smoke test

Use a normal client-only session. Verify that the mod sends no custom packets, changes no controls, does not affect collision or movement, and exposes no player-information features.

Do not use private server logs or screenshots containing account information in the repository.
