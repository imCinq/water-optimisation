# Architecture

## Data flow

Client fluid state
→ version-isolated FluidRenderer hook
→ conservative source-water classifier
→ vanilla fluid mesh path or explicit interior fast-path skip
→ translucent section buffer

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

### Fluid hooks

FluidRendererMixin targets Minecraft 26.2's public FluidRenderer tessellation and face-decision methods. The policy is intentionally narrow:

- the face decision can force a face hidden only when both states are ordinary full source-water blocks;
- the flat path cancels tessellation only when the current block and all six neighbors are ordinary full source-water blocks;
- flowing states, boundaries, waterlogged blocks, partial shapes, overlays, transparent neighbors, and other ambiguous cases return to vanilla.

The mixin is client-only and isolated in wateroptimisation.client.mixins.json. It does not replace RenderType, RenderPipeline, Sodium, FluidState, or world simulation.

### Particle filter

ClientLevelMixin intercepts only the client-side addParticle overload. It recognizes water-specific particle types, preserves non-water particles, preserves always-visible particles, and applies the local distance policy only when the master switch is enabled.

### Diagnostics

Counters use allocation-free LongAdder increments when diagnostics are enabled. Fluid tessellation timing uses one primitive timing holder per worker thread, and the HUD displays only local counters. Section compiler and translucent resort timings still require Minecraft's profiler/Tracy output because they are not inferred from a per-fluid call.

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
