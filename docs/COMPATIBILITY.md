# Compatibility

## Minecraft and Fabric

The target is Minecraft 26.2 with Java 25, Fabric Loader 0.19.3 or newer, and a matching Fabric API build. Version-specific rendering hooks are isolated in the client mixin configuration.

## Mod Menu

Mod Menu 19.0.0-alpha.1 is an optional compile-only dependency for the target. The core mod loads without it and retains the keybind and native settings screen.

## Sodium

Sodium has its own optimized fluid renderer. When the Sodium mod id is detected, Water Optimisation disables its vanilla FluidRenderer face and tessellation hooks. A separate optional bridge is reviewed for Sodium 0.9.x on Minecraft 26.2: it changes only the boolean that marks Sodium's reversed quad copy for ordinary source water when Experimental reduced-face mode is selected. It does not replace Sodium's renderer or duplicate its visibility, fluid shaping, lighting, hidden-fluid culling, or translucent sorting.

The bridge is version-gated and fail-closed. If its class or method hooks do not match, or the build is outside Sodium 0.9.x for Minecraft 26.2, geometry remains Sodium-owned and only local particle settings apply. The main settings screen reports the effective path; the two vanilla geometry controls remain unavailable when no compatible bridge is active.

## Rendering backends

Minecraft 26.2 can use different rendering backends. The implementation uses Minecraft's Blaze3D/Fabric abstractions and does not call raw OpenGL. Test OpenGL and Vulkan separately when both are available.

## Multiplayer

The mod is client-only and render-focused. It does not add packets, change movement or collision, alter fluid simulation, modify world updates, or expose player-information features. Server rules are separate from technical client-only behavior and must be checked before use.

## Fallback behavior

If a hook is unavailable, another renderer owns the fluid path, Mod Menu is absent, or a shape cannot be classified safely, the relevant feature preserves normal behavior. Flowing, partial, waterlogged, overlay, transparent, and unusual states remain on the vanilla path.
