# Architecture

## Data flow

Client fluid state
→ version-isolated FluidRenderer hook or optional Sodium face bridge
→ already-loaded neighbor-state classifier
→ vanilla fluid mesh path or explicit fully-hidden source-water skip
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
- the flat path checks the current block and all six already-loaded neighbor block/fluid states, then cancels tessellation only when each face is hidden by ordinary full source-water or full solid-rendering blocks;
- Reduced-face mode changes only vanilla's optional reverse-face argument at `FluidRenderer.addFace` for ordinary full source-water blocks, preserving the outward face while reducing translucent geometry. It is enabled by Maximum FPS or manual selection and inactive when Sodium owns fluid rendering;
- flowing states, boundaries, waterlogged blocks, partial shapes, overlays, transparent neighbors, and other ambiguous cases return to vanilla.

The fully hidden-water optimization is injected immediately before vanilla's first face decision, after the six neighbor states have been loaded. This avoids repeating chunk lookups in the fast path. The reverse-face argument change is isolated to vanilla's face helper and does not read camera state from an asynchronous section compiler. Its thread-local context is touched only while the experimental mode is active, keeping safe and disabled paths free of cleanup calls. The mixin is client-only and isolated in wateroptimisation.client.mixins.json. It does not replace RenderType, RenderPipeline, Sodium, FluidState, or world simulation.

### Particle filter

ClientLevelMixin intercepts only the client-side addParticle overload. It exits immediately while the mod is disabled or on the Vanilla preset, then recognizes water-specific particle types, preserves non-water particles, preserves always-visible particles, and applies a cached camera-relative distance policy only when the master switch is enabled. The filter snapshot is rebuilt only when configuration changes. If the render camera is not initialized, the player position is used as a safe lifecycle fallback.

### Diagnostics

	Counters use allocation-free LongAdder increments when diagnostics are enabled. Fluid tessellation, section compilation, and translucent resort timing use primitive timing holders per worker thread; fluid timing samples one in sixteen calls to avoid adding clock reads to every fluid block while the HUD remains open. The HUD snapshots and formats its lines at most four times per second, then reuses the immutable components between refreshes. Only removed reverse faces are counted, so the measurement does not add a callback to every vanilla face decision. Tracy is still required for frame-time distributions, tail latency, total face counts, and independent verification.

## Compatibility strategy

- Fabric API is the primary client integration surface.
- Mod Menu is optional and contains no renderer logic.
- Sodium ownership is detected before normal gameplay and disables the vanilla fluid hooks. A separate version-gated bridge can remove only the reversed copy of ordinary source-water quads on reviewed Sodium 0.9.x/Minecraft 26.2 builds; unknown builds fall back to Sodium unchanged.
- The implementation uses Minecraft's renderer and GUI abstractions; it does not call raw OpenGL.
- Every uncertain classification falls back to vanilla behavior.

## Non-goals

- changing server fluid simulation;
- changing collision or movement;
- replacing water with air;
- altering global translucent ordering;
- hiding entities, players, items, or server information.
