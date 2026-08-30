# Architecture

## Data flow

Client fluid state
→ version-isolated FluidRenderer hook
→ already-loaded neighbor-state classifier
→ vanilla fluid mesh path or explicit interior fast-path skip
→ translucent section buffer
→ section compilation and translucent resort diagnostics

Particle state
→ ClientLevel admission hook
→ local water-particle classifier
→ vanilla particle renderer or distance rejection

Diagnostics observe these decisions locally without changing gameplay state.

## Components

### Client entrypoint

Initializes the local configuration, keybind, native screens, HUD element, and Sodium ownership detection. No server entrypoint is declared.

### Configuration

The common configuration model is independent of Minecraft client classes so defaults, profile reset, clamping, and copy isolation can be unit-tested. ConfigManager loads and atomically saves the JSON file on the client.

Only settings that can change compiled fluid geometry invalidate rendered sections. Particle-distance, particle-enable, fog, diagnostics, and fallback-logging changes take effect at their own hooks and do not trigger a full section rebuild.

### Fluid hooks

FluidRendererMixin targets Minecraft 26.2's public FluidRenderer tessellation method. The policy is intentionally narrow:

- vanilla remains responsible for same-fluid face culling; Minecraft already hides those faces before emitting geometry;
- the flat path checks the current block and all six already-loaded neighbor block/fluid states, then cancels tessellation only when they are ordinary full source-water blocks;
- flowing states, boundaries, waterlogged blocks, partial shapes, overlays, transparent neighbors, and other ambiguous cases return to vanilla.

The optimization is injected immediately before vanilla's first face decision, after the six neighbor states have been loaded. This avoids repeating chunk lookups in the fast path. The mixin is client-only and isolated in wateroptimisation.client.mixins.json. It does not replace RenderType, RenderPipeline, Sodium, FluidState, or world simulation.

### Particle filter

ClientLevelMixin intercepts only the client-side addParticle overload. It exits immediately while the mod is disabled or on the Vanilla preset, then recognizes water-specific particle types, preserves non-water particles, preserves always-visible particles, and applies a cached camera-relative distance policy only when the master switch is enabled. The filter snapshot is rebuilt only when configuration changes. If the render camera is not initialized, the player position is used as a safe lifecycle fallback.

### Diagnostics

	Counters use allocation-free LongAdder increments when diagnostics are enabled. Fluid tessellation, section compilation, and translucent resort timing use primitive timing holders per worker thread; fluid timing samples one in sixteen calls to avoid adding clock reads to every fluid block while the HUD remains open. The HUD snapshots and formats its lines at most four times per second, then reuses the immutable components between refreshes. Face decisions are deliberately not instrumented because a per-face callback costs more than the redundant optimization it replaced. Tracy is still required for frame-time distributions, tail latency, face counts, and independent verification.

## Compatibility strategy

- Fabric API is the primary client integration surface.
- Mod Menu is optional and contains no renderer logic.
- Sodium ownership is detected before normal gameplay and disables the vanilla fluid hooks.
- The implementation uses Minecraft's renderer and GUI abstractions; it does not call raw OpenGL.
- Every uncertain classification falls back to vanilla behavior.

## Non-goals

- changing server fluid simulation;
- changing collision or movement;
- replacing water with air;
- altering global translucent ordering;
- hiding entities, players, items, or server information.
