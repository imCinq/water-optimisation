# Proposed Architecture

## Data flow

Client fluid state
→ local neighbor and shape classifier
→ conservative visibility decision
→ fluid mesh path
→ translucent section buffer

Particle state
→ local distance and fog filter
→ particle admission decision
→ normal particle renderer

Diagnostics observe both paths without changing gameplay state.

## Components

### Client entrypoint

Initializes local configuration, diagnostics, optional Mod Menu integration, and version-specific hooks.

### Configuration

Stores local settings with safe defaults. The master feature should start disabled until the implementation has passed visual and multiplayer validation.

### Fluid visibility classifier

Receives the current fluid state and neighboring block/fluid states. It can reject faces only when the evidence is sufficient. Ambiguous cases use the vanilla-compatible fallback.

### Fluid fast path

Handles only uniform source-water surfaces with no height variation, overlays, waterlogged shape complications, or unusual transparency. All other fluids and shapes use the normal path.

### Particle filter

Limits only cosmetic particle admission or extraction based on distance and fog visibility. It must not alter fluid state, particle physics, gameplay feedback, or server behavior.

### Diagnostics

Tracks water blocks visited, faces emitted by category, section compilation time, translucent resort time, particle groups, and fallback counts. Diagnostics are local and opt-in.

## Compatibility strategy

- Fabric API is the primary integration surface.
- Sodium should own its renderer when it is present, unless a documented compatible hook is available.
- Do not run two fluid renderers for one block.
- Keep version-specific mixins isolated and tested against exact mappings.
- Use feature detection and graceful fallback rather than assuming another renderer's internals.

## Non-goals

- changing server fluid simulation;
- changing collision or movement;
- replacing water with air;
- altering global translucent ordering;
- hiding entities, players, items, or server information.
