# Benchmarking

## Required comparisons

Compare the same client, world, camera, resolution, render distance, simulation distance, FPS cap, graphics settings, Java runtime, and rendering backend.

Use:

1. vanilla or feature-disabled;
2. conservative mode;
3. aggressive or experimental mode;
4. Sodium absent and present where relevant;
5. OpenGL and Vulkan as separate comparisons on 26.2.

Warm the scene before measuring. Use multiple captures rather than a single F3 screenshot.

## Scenes

- large flat ocean;
- waterfall or flowing channel;
- waterlogged stairs and doors;
- leaves and transparent blocks next to water;
- flooded cave;
- underwater view;
- particle-heavy farm or public-server area;
- ordinary non-water scene to detect regressions.

## Metrics

Record average FPS, 1% low FPS, p95 and p99 frame time, hitch count, fluid section compilation time, translucent resort time, number of water blocks visited, faces emitted by category, particle counts and rejected particles, and memory or allocation changes when measurable.

Minecraft 26.2's Tracy support and particle_render_stats can assist with profiling. Keep the backend fixed within each before-and-after comparison.

## Reporting

Every claimed improvement must state hardware and operating system, Minecraft, loader, Java, and mod versions, renderer/backend, resource packs and shaders, scene and camera, settings, sample duration and warm-up, visual trade-offs, and whether the result is average, tail latency, or an instantaneous observation.

Do not present a machine-specific result as a universal FPS guarantee.
