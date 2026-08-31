# Architecture

## Data flow

Client fluid state
→ target-isolated FluidRenderer hook or optional 26.2 Sodium face bridge
→ already-loaded neighbor-state classifier
→ vanilla fluid mesh path, explicit fully-hidden source-water skip, deferred flat-surface prototype, or guarded 26.2-only owned-water mesh
→ shared translucent section buffer and, for eligible far-water sections, a section-owned vertex buffer
→ section compilation and translucent resort diagnostics

Particle state
→ ClientLevel admission hook
→ local water-particle classifier
→ vanilla particle renderer or distance rejection

Diagnostics observe these decisions locally without changing gameplay state.

## Target profiles

The 26.2 and 1.21.1 client APIs are compiled from separate source roots. The 26.2 profile uses the non-remapping Loom plugin and extraction-based GUI/HUD APIs. The 1.21.1 profile uses remapping Loom with official Mojang mappings and its older `GuiGraphics`, `HudRenderCallback`, key-binding, liquid-renderer, and level-refresh APIs. The generated metadata selects only the mixin configuration for the requested target.

## Components

### Client entrypoint

Initializes the local configuration, keybind, native screens, HUD element, and Sodium ownership detection. No server entrypoint is declared.

### Configuration

The common configuration model is independent of Minecraft client classes so defaults, profile reset, clamping, and copy isolation can be unit-tested. ConfigManager loads and atomically saves the JSON file on the client.

Only settings that can change compiled fluid geometry invalidate rendered sections. Particle-distance, particle-enable, fog, diagnostics, and fallback-logging changes take effect at their own hooks and do not trigger a full section rebuild.

### Fluid hooks

The 26.2 FluidRendererMixin targets the current public fluid tessellation method. The 1.21.1 adapter targets `LiquidBlockRenderer` and performs a smaller safe check because that renderer does not expose the same stable local-state hook. Both policies are intentionally narrow:

- vanilla remains responsible for same-fluid face culling; Minecraft already hides those faces before emitting geometry;
- the flat path checks the current block and all six already-loaded neighbor block/fluid states, then cancels tessellation only when each face is hidden by ordinary full source-water or full solid-rendering blocks;
- Reduced-face mode changes only vanilla's optional reverse-face argument at `FluidRenderer.addFace` for ordinary full source-water blocks, preserving the outward face while reducing translucent geometry. It is enabled by Maximum FPS or manual selection and inactive when Sodium owns fluid rendering;
- flowing states, boundaries, waterlogged blocks, partial shapes, overlays, transparent neighbors, and other ambiguous cases return to vanilla.

The 1.21.1 adapter does not manufacture a level/position context for its older solid-render query. It therefore proves only fully enclosed ordinary source water whose six neighbors are also ordinary source water; solid-boundary cases remain vanilla. This reduces the compatibility path’s coverage but keeps its correctness proof simple.

The fully hidden-water optimization is injected immediately before vanilla's first face decision, after the six neighbor states have been loaded. This avoids repeating chunk lookups in the fast path. The reverse-face argument change is isolated to vanilla's face helper and does not read camera state from an asynchronous section compiler. Its thread-local context is touched only while the experimental mode is active, keeping safe and disabled paths free of cleanup calls. The mixin is client-only and isolated in wateroptimisation.client.mixins.json. It does not replace RenderType, RenderPipeline, Sodium, FluidState, or world simulation.

The 26.2 renderer also has an opt-in far-water ownership path around section compilation and fluid tessellation. A conservative preflight accepts only sections with ordinary still source water and no non-air, non-solid model that could introduce mixed translucent terrain. Only upward fluid faces are copied into a `WaterOwnedMesh`; vertical sides and bottoms remain in the shared translucent output because the dedicated pass cannot reproduce vanilla's per-face sort order safely. Owned surfaces are carried from `SectionCompiler.Results` to `CompiledSectionMesh`. A client render callback then draws those meshes after translucent terrain through Minecraft's Blaze3D pipeline, using the current frame camera matrix and translucent target, ordering sections back-to-front, and skipping eligible sections beyond its 320-block bound. Unsupported fluids, mixed translucent sections, Sodium-owned geometry, and the 1.21.1 target remain on their existing paths. The pass is disabled by default and remains an experiment until live visual and frame-time validation proves it safe and useful.

The flat-surface prototype is retained as source but currently disabled for visual safety. A single atlas quad cannot preserve vanilla's repeated water texture without shader-side tiling support; stretching the source sprite or extrapolating atlas UVs produces visible artifacts. It will not be exposed as an active 26.2 capability until an implementation can preserve exact vanilla tiling and patch-boundary geometry.

### Particle filter

ClientLevelMixin intercepts only the client-side addParticle overload. It exits immediately while the mod is disabled or on the Vanilla preset, then recognizes water-specific particle types, preserves non-water particles, preserves always-visible particles, and applies a cached camera-relative distance policy only when the master switch is enabled. The filter snapshot is rebuilt only when configuration changes. If the render camera is not initialized, the player position is used as a safe lifecycle fallback.

An optional budget resets at the start of each client tick and is reserved only after water-particle distance admission succeeds. This prevents the budget from paying for particles already rejected by the cheaper checks. The budget is local to the client and does not alter particle simulation or non-water particles. The forced-particle switch makes the normally preserved `alwaysShow` water subset obey the same cosmetic policy when explicitly requested.

### Diagnostics

	Counters use allocation-free LongAdder increments when diagnostics are enabled. Fluid tessellation, section compilation, and translucent resort timing use primitive timing holders per worker thread; fluid timing samples one in sixteen calls to avoid adding clock reads to every fluid block while the HUD remains open. The HUD snapshots and formats its lines at most four times per second, then reuses the immutable components between refreshes. Face, patch, and particle-budget counters are diagnostics-only and remain gated. Tracy is still required for frame-time distributions, tail latency, total face counts, and independent verification.

## Compatibility strategy

- Fabric API is the primary client integration surface.
- Mod Menu is optional and contains no renderer logic.
- Sodium ownership is detected before normal gameplay and disables the vanilla fluid hooks. A separate version-gated bridge can remove only the reversed copy of ordinary source-water quads on reviewed Sodium 0.9.x/Minecraft 26.2 builds; unknown builds fall back to Sodium unchanged.
- On Minecraft 1.21.1, Sodium ownership disables the vanilla fluid hooks and leaves geometry entirely to Sodium. The compatibility profile adds no unreviewed Sodium mixin.
- The implementation uses Minecraft's renderer and GUI abstractions; it does not call raw OpenGL.
- Every uncertain classification falls back to vanilla behavior.

## Far-water architecture boundary

Far-water is an early GPU/fill-rate track, but it cannot safely be implemented as a distance test inside the current translucent section buffer. That buffer contains water together with glass, leaves, overlays, and other translucent geometry. A section-level or shared-buffer cutoff would either remove unrelated visuals or require a brittle per-vertex rewrite after asynchronous compilation.

The intended design is a separate water-owned representation and pass:

1. identify only the exact water geometry that can be separated without changing non-water translucency;
2. keep near water on the full-quality path and route only far water through an independently bounded pass;
3. apply water-only distance, fog, and later LOD/half-resolution decisions through Blaze3D/Minecraft abstractions;
4. preserve vanilla/Sodium ownership and fail closed when the renderer cannot provide that separation.

This track has a compiled-mesh ownership handoff and a guarded 26.2-only separate draw in `docs/FAR_WATER_PASS.md`. The runtime distance cutoff is opt-in, hard-bounded, and fail-closed; it is not a general shared-translucent cull or a stable preset.

## Non-goals

- changing server fluid simulation;
- changing collision or movement;
- replacing water with air;
- altering global translucent ordering;
- hiding entities, players, items, or server information.
