# Compatibility

## Minecraft and Fabric

The project has isolated target profiles so the incompatible client APIs cannot be loaded across versions.

| Target | Java | Fabric Loader | Fabric API | Mod Menu | Runtime geometry scope |
| --- | --- | --- | --- | --- | --- |
| Minecraft 26.2 | 25+ | 0.19.3+ | 0.158.0+26.2 | Optional 19.0.0-alpha.1 | Vanilla conservative/reduced-face hooks; reviewed Sodium bridge when matched. |
| Minecraft 1.21.1 | 21+ | 0.16.13+ | 0.116.12+1.21.1 | Optional 11.0.4 | Conservative source-water fast path and particles; no Sodium geometry bridge yet. |

The 1.21.1 profile uses the remapping Loom plugin and official Mojang mappings, while 26.2 uses the non-remapping Loom profile. Its older GUI, HUD, key-binding, and liquid-renderer APIs live under `src/1.21.1/client/java` and `wateroptimisation.legacy.mixins.json`.

## Mod Menu

Mod Menu is an optional compile-only dependency: 19.0.0-alpha.1 for 26.2 and 11.0.4 for 1.21.1. The core mod loads without it and retains the keybind and native settings screen.

## Sodium

Sodium has its own optimized fluid renderer. When the Sodium mod id is detected, Water Optimisation disables its vanilla fluid geometry hooks. A separate optional bridge is reviewed for Sodium 0.9.x on Minecraft 26.2: it changes only the boolean that marks Sodium's reversed quad copy for ordinary source water when Experimental reduced-face mode is selected. It does not replace Sodium's renderer or duplicate its visibility, fluid shaping, lighting, hidden-fluid culling, or translucent sorting. The 1.21.1 profile deliberately has no equivalent bridge yet; it retains particle filtering and otherwise lets Sodium own fluid geometry.

The bridge is version-gated and fail-closed. If its class or method hooks do not match, or the build is outside Sodium 0.9.x for Minecraft 26.2, geometry remains Sodium-owned and only local particle settings apply. The main settings screen reports the effective path; the two vanilla geometry controls remain unavailable when no compatible bridge is active.

## Rendering backends

Minecraft 26.2 can use different rendering backends. The implementation uses Minecraft's Blaze3D/Fabric abstractions and does not call raw OpenGL. Test OpenGL and Vulkan separately when both are available.

## Multiplayer

The mod is client-only and render-focused. It does not add packets, change movement or collision, alter fluid simulation, modify world updates, or expose player-information features. Server rules are separate from technical client-only behavior and must be checked before use.

## Fallback behavior

If a hook is unavailable, another renderer owns the fluid path, Mod Menu is absent, or a shape cannot be classified safely, the relevant feature preserves normal behavior. Flowing, partial, waterlogged, overlay, transparent, and unusual states remain on the normal renderer. The 26.2 far-water pass is unavailable with Sodium and is disabled for the 1.21.1 profile; its section preflight also falls back whenever mixed translucent content is possible.
