# Far-water GPU track

## Status

The 26.2 profile now contains an opt-in rendering experiment named `Limit distant water to 320 blocks?`. It is disabled by default, unavailable when Sodium owns fluid rendering, and not compiled into the 1.21.1 client source set. It uses a hard section-distance cutoff; it does not yet implement a fade, reduced-resolution mesh, or general LOD. The draw uses the current frame's camera matrix and the translucent terrain target so the owned mesh follows camera movement.

The pass is deliberately fail-closed. It can only take ownership of a section after a preflight finds ordinary still source water and no non-air, non-solid model that could introduce mixed translucent terrain. It owns upward surfaces only; vertical sides and bottoms stay vanilla because the separate pass cannot reproduce their per-face sort order safely. Flowing water, waterlogged blocks, glass, leaves, plants, overlays, partial shapes, transparent boundaries, custom translucent models, and any uncertain section stay on the normal path.

## Why this needs its own pass

Water is compiled into a shared translucent buffer alongside glass, leaves, overlays, and other translucent block geometry. A camera-distance test at the section or buffer level cannot tell those quads apart safely. It could hide unrelated terrain, change ordering, or fight asynchronous section compilation.

The first requirement is therefore ownership: water geometry must be represented separately before it can receive a water-only GPU policy. A global translucent-sort bypass is not a substitute for that separation.

## Current implementation

### Stage 1 — Water ownership

During 26.2 section compilation, the existing fluid hooks build a separate `WaterOwnedMesh` only for exact ordinary still-water upward faces in an eligible section. The mesh uses Minecraft's `DefaultVertexFormat.BLOCK` and is transferred with the compiled `SectionMesh` through `SectionCompiler.Results` and `CompiledSectionMesh`. Its GPU vertex buffer is created lazily on the render thread and is closed with the section mesh.

The shared translucent buffer remains authoritative for every unsupported or mixed case. The pass never tries to split an already-mixed translucent buffer after compilation.

### Stage 2 — Dedicated water pass

After Minecraft's translucent terrain has rendered, the 26.2 client registers an `AFTER_TRANSLUCENT_TERRAIN` callback. It draws owned water meshes through `RenderPipelines.TRANSLUCENT_TERRAIN`, reusing the terrain atlas, level lightmap, section uniforms, depth target, blending, and fog bindings exposed by Blaze3D. It uses Minecraft's rendering abstraction rather than raw OpenGL so the path can fail safely on an unsupported backend.

The pass applies its own:

- camera-relative distance bound;
- back-to-front ordering between owned sections;
- fog interaction;
- capability check for the active Minecraft backend.

The first version uses a 320-block section AABB distance bound. Eligible upward surfaces inside the bound are redrawn through the dedicated pass; eligible sections beyond the bound are skipped while their vanilla side and bottom faces remain available. Because this is a hard cutoff, the transition is intentionally exposed for testing rather than hidden behind a stable preset.

### Stage 3 — Controlled LOD

Only after the separate pass is visually stable should it gain optional LODs. The first candidate is a capped flat still-water mesh with a conservative transition band. A later experiment may use reduced vertex density or half-resolution far water, but only if the active pipeline preserves fog, tint, depth, and blending behavior. The current pass does not claim either optimization.

The pass should never silently change the near-water surface. Its fallback is the full vanilla/Sodium path whenever ownership, material, backend, or sort conditions are uncertain.

## Instrumentation needed

Diagnostics for this track measure the decision rather than claim a benefit:

- eligible near and far water sections;
- owned-water uploads, drawn sections, drawn indices, and distance skips;
- ordinary source-water candidate blocks and fallback blocks;
- dedicated pass CPU submission time;
- dedicated pass GPU time where the backend exposes it;
- transition distance and rejected/fallback reasons;
- visual comparison at the transition, underwater, and transparent boundaries.

Existing section-compile and translucent-resort counters cannot prove a far-water win because they measure the shared path. The current counters show whether the owned mesh was uploaded, drawn, or skipped by distance; they do not measure GPU time. The prototype still needs live visual validation and controlled frame-time measurements before it can be enabled by a preset or called a performance improvement.

## Target order

Keep the separate pass on Minecraft 26.2 first, where the current renderer and Fabric rendering guidance are known. Keep the 1.21.1 profile on its conservative compatibility adapter until the same water-ownership contract can be implemented against its older liquid renderer. Do not add a version-independent mixin that guesses at either renderer’s internal buffer layout. Sodium remains authoritative and does not use this pass.

## Acceptance gate

This track is ready for a user-facing option only when a remote-built artifact has:

1. no missing water, glass, leaves, overlays, or terrain;
2. correct above-water, underwater, flowing, waterlogged, cave, and transparent-boundary views;
3. stable behavior with and without Sodium;
4. measurable water-only GPU/frame-time improvement in a controlled far-water scene;
5. an immediate fail-closed fallback when the pass cannot be separated or sorted safely.

Until then, the correct product behavior is to keep the experiment opt-in, keep unsupported sections on the normal renderer, and treat remote build success as compatibility evidence rather than an FPS result.
