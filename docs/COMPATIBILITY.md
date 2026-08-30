# Compatibility

## Minecraft and Fabric

The target is Minecraft 26.2 with Java 25, Fabric Loader 0.19.3 or newer, and a matching Fabric API build. Version-specific rendering hooks are isolated in the client mixin configuration.

## Mod Menu

Mod Menu 19.0.0-alpha.1 is an optional compile-only dependency for the target. The core mod loads without it and retains the keybind and native settings screen.

## Sodium

Sodium has its own optimized fluid renderer. When the Sodium mod id is detected, Water Optimisation disables its vanilla FluidRenderer face, reduced-face, and tessellation hooks. It does not force a renderer replacement or call Sodium internals. The particle and configuration paths remain independent. The Experimental reduced-face setting therefore has no effect while Sodium is present.

This is a renderer-ownership guard, not proof of complete Sodium integration. Test the exact Sodium build and companion-mod combination before enabling fluid optimization.

## Rendering backends

Minecraft 26.2 can use different rendering backends. The implementation uses Minecraft's Blaze3D/Fabric abstractions and does not call raw OpenGL. Test OpenGL and Vulkan separately when both are available.

## Multiplayer

The mod is client-only and render-focused. It does not add packets, change movement or collision, alter fluid simulation, modify world updates, or expose player-information features. Server rules are separate from technical client-only behavior and must be checked before use.

## Fallback behavior

If a hook is unavailable, another renderer owns the fluid path, Mod Menu is absent, or a shape cannot be classified safely, the relevant feature preserves normal behavior. Flowing, partial, waterlogged, overlay, transparent, and unusual states remain on the vanilla path.
