# Compatibility

## Minecraft and Fabric

The project has isolated target profiles so the incompatible client APIs cannot be loaded across versions.

| Target | Java | Fabric Loader | Fabric API | Mod Menu | Runtime geometry scope |
| --- | --- | --- | --- | --- | --- |
| Minecraft 26.2 | 25+ | 0.19.3+ | 0.158.0+26.2 | Optional 19.0.0-alpha.1 | Vanilla conservative/reduced-face hooks; Sodium-owned geometry when present. |
| Minecraft 1.21.1 | 21+ | 0.16.13+ | 0.116.12+1.21.1 | Optional 11.0.4 | Conservative source-water fast path and particles; Sodium-owned geometry when present. |

The 1.21.1 profile uses the remapping Loom plugin and official Mojang mappings, while 26.2 uses the non-remapping Loom profile. Its older GUI, HUD, key-binding, and liquid-renderer APIs live under `src/1.21.1/client/java` and `wateroptimisation.legacy.mixins.json`.

## Mod Menu

Mod Menu is an optional compile-only dependency: 19.0.0-alpha.1 for 26.2 and 11.0.4 for 1.21.1. The core mod loads without it and retains the keybind and native settings screen.

## Sodium

Sodium has its own optimized fluid renderer. When the Sodium mod id is detected, Water Optimisation disables all of its vanilla fluid geometry hooks. Sodium remains fully responsible for visibility, fluid shaping, lighting, hidden-fluid culling, translucent collection, and sorting; Water Optimisation applies only local particle settings. The reduced-inward-face experiment is available only on the vanilla renderer. No Sodium geometry bridge is planned; Sodium remains the geometry owner unless project scope is formally reconsidered. The main settings screen reports the effective path and labels overlapping geometry controls as unavailable while Sodium renders water.

## Rendering backends

Minecraft 26.2 can use different rendering backends. The implementation uses Minecraft's Blaze3D/Fabric abstractions and does not call raw OpenGL. Test OpenGL and Vulkan separately when both are available.

## Multiplayer

The mod is client-only and render-focused. It does not add packets, change movement or collision, alter fluid simulation, modify world updates, or expose player-information features. Server rules are separate from technical client-only behavior and must be checked before use.

## Fallback behavior

If a hook is unavailable, another renderer owns the fluid path, Mod Menu is absent, or a shape cannot be classified safely, the relevant feature preserves normal behavior. Hidden-water skipping leaves flowing, partial, waterlogged, overlay, transparent, and unusual states on the normal renderer. Experimental inward-face reduction is separate and may change underwater or transparent-boundary views for eligible ordinary source water.
