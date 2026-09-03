# Architecture

## Data flow

Client fluid state
→ target-isolated FluidRenderer hook, with Sodium ownership detected before vanilla hooks run
→ already-loaded neighbor-state classifier
→ vanilla fluid mesh path or explicit fully-hidden source-water skip
→ shared translucent section buffer
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
- Reduced-face mode changes only vanilla's optional reverse-face argument at `FluidRenderer.addFace` for ordinary full source-water blocks, preserving the outward face while reducing translucent geometry. It is enabled by Maximum FPS or manual selection and inactive when Sodium owns fluid rendering. No Sodium geometry mixin is installed;
- the hidden-water path checks the current block and all six already-loaded neighbor block/fluid states, then cancels tessellation only when each face is hidden by ordinary full source-water or full solid-rendering blocks;
- flowing states, boundaries, waterlogged blocks, partial shapes, overlays, transparent neighbors, and other ambiguous cases return to vanilla.

The 1.21.1 adapter does not manufacture a level/position context for its older solid-render query. It therefore proves only fully enclosed ordinary source water whose six neighbors are also ordinary source water; solid-boundary cases remain vanilla. This reduces the compatibility path’s coverage but keeps its correctness proof simple.

The fully hidden-water optimization is injected immediately before vanilla's first face decision, after the six neighbor states have been loaded. This avoids repeating chunk lookups in the fast path. The reverse-face argument change is isolated to vanilla's face helper and does not read camera state from an asynchronous section compiler. The 26.2 local capture is optional and fail-soft: if the renderer's locals change, the optimization is skipped instead of crashing the client. The mixin is client-only and isolated in wateroptimisation.client.mixins.json. It does not replace RenderType, RenderPipeline, Sodium, FluidState, or world simulation.

### Particle filter

ClientLevelMixin intercepts only the client-side addParticle overload. It exits immediately while the mod is disabled or on the Vanilla preset, then recognizes water-specific particle types, preserves non-water particles, preserves always-visible particles, and applies a cached camera-relative distance policy only when the master switch is enabled. The filter snapshot is rebuilt only when configuration changes. If the render camera is not initialized, the player position is used as a safe lifecycle fallback.

An optional budget resets at the start of each client tick and is reserved only after water-particle distance admission succeeds. This prevents the budget from paying for particles already rejected by the cheaper checks. The budget is local to the client and does not alter particle simulation or non-water particles. The forced-particle switch makes the normally preserved `alwaysShow` water subset obey the same cosmetic policy when explicitly requested.

### Diagnostics

	Counters use allocation-free LongAdder increments when diagnostics are enabled. Fluid tessellation, section compilation, and translucent resort timing use primitive timing holders per worker thread; fluid timing samples one in sixteen calls to avoid adding clock reads to every fluid block while the HUD remains open. The HUD snapshots and formats its lines at most four times per second, then reuses the immutable components between refreshes. Face and particle-budget counters are diagnostics-only and remain gated. Tracy is still required for frame-time distributions, tail latency, total face counts, and independent verification.

## Compatibility strategy

- Fabric API is the primary client integration surface.
- Mod Menu is optional and contains no renderer logic.
- Sodium ownership is detected before normal gameplay and disables the vanilla fluid hooks. Sodium remains fully responsible for water geometry; the mod has no Sodium geometry bridge until an exact renderer-specific cancellation hook is reviewed and validated.
- On Minecraft 1.21.1, Sodium ownership disables the vanilla fluid hooks and leaves geometry entirely to Sodium. The compatibility profile adds no unreviewed Sodium mixin.
- The implementation uses Minecraft's renderer and GUI abstractions; it does not call raw OpenGL.
- Every uncertain classification falls back to vanilla behavior.

## Non-goals

- changing server fluid simulation;
- changing collision or movement;
- replacing water with air;
- altering global translucent ordering;
- hiding entities, players, items, or server information.
