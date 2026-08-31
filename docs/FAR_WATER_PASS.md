# Far-water GPU track

## Status

This is an early implementation design, not an enabled gameplay or rendering option. The current mod still uses Minecraft’s normal translucent section buffer. No distance cutoff, half-resolution draw, or water-only fade is active.

## Why this needs its own pass

Water is compiled into a shared translucent buffer alongside glass, leaves, overlays, and other translucent block geometry. A camera-distance test at the section or buffer level cannot tell those quads apart safely. It could hide unrelated terrain, change ordering, or fight asynchronous section compilation.

The first requirement is therefore ownership: water geometry must be represented separately before it can receive a water-only GPU policy. A global translucent-sort bypass is not a substitute for that separation.

## Proposed stages

### Stage 1 — Water ownership

Create a water-specific mesh or submesh representation only for exact ordinary source-water geometry. Preserve the current vanilla path for flowing water, waterlogged blocks, overlays, partial shapes, transparent boundaries, and ambiguous states. Keep Sodium’s renderer authoritative when Sodium is present unless an exact integration supplies the same separation.

The representation must retain enough information for correct nearby rendering:

- still versus flowing material;
- surface height and corner heights;
- light and tint inputs;
- face orientation and inward/outward policy;
- section bounds and sort metadata;
- whether a quad is eligible for the far-water path.

### Stage 2 — Dedicated water pass

Draw near water with the current full-quality behavior. Route only eligible far-water geometry through a separate water-owned pass that has its own:

- camera-relative distance bound;
- fog interaction;
- near/far transition band;
- translucent ordering policy;
- capability check for the active Minecraft backend.

The pass must use Minecraft/Blaze3D rendering abstractions. It must not assume raw OpenGL state, because Minecraft 26.2 can use a Vulkan backend.

### Stage 3 — Controlled LOD

Only after the separate pass is visually stable should it gain optional LODs. The first candidate is a capped flat still-water mesh with a conservative transition band. A later experiment may use reduced vertex density or half-resolution far water, but only if the active pipeline preserves fog, tint, depth, and blending behavior.

The pass should never silently change the near-water surface. Its fallback is the full vanilla/Sodium path whenever ownership, material, backend, or sort conditions are uncertain.

## Instrumentation needed

Diagnostics for this track should measure the decision rather than claim a benefit:

- eligible near and far water quads;
- water mesh bytes and vertex counts;
- dedicated pass CPU submission time;
- dedicated pass GPU time where the backend exposes it;
- transition distance and rejected/fallback reasons;
- visual comparison at the transition, underwater, and transparent boundaries.

Existing section-compile and translucent-resort counters cannot prove a far-water win because they measure the shared path. The prototype needs water-owned counters before a user-facing setting is justified.

## Target order

Prototype the separate pass on Minecraft 26.2 first, where the current renderer and Fabric rendering guidance are known. Keep the 1.21.1 profile on its conservative compatibility adapter until the same water-ownership contract can be implemented against its older liquid renderer. Do not add a version-independent mixin that guesses at either renderer’s internal buffer layout.

## Acceptance gate

This track is ready for a user-facing option only when a remote-built artifact has:

1. no missing water, glass, leaves, overlays, or terrain;
2. correct above-water, underwater, flowing, waterlogged, cave, and transparent-boundary views;
3. stable behavior with and without Sodium;
4. measurable water-only GPU/frame-time improvement in a controlled far-water scene;
5. an immediate fail-closed fallback when the pass cannot be separated or sorted safely.

Until then, the correct product behavior is to keep the idea documented and the runtime unchanged.
