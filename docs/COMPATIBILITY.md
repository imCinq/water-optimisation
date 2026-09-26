# Compatibility

## Minecraft and loaders

The v1.1.0 line has isolated target profiles so incompatible client APIs cannot be loaded across versions. Fabric 26.2, Fabric 1.21.1, and NeoForge 26.2 are historical 0.0.10 targets and are no longer active development targets.

| Target | Java | Fabric Loader | Fabric API | Mod Menu | Runtime geometry scope |
| --- | --- | --- | --- | --- | --- |
| Minecraft 26.3 | 25+ | 0.19.5+ | 0.160.6+26.3 | Optional 21.0.0-beta.1 | Vanilla conservative/reduced-face hooks; Sodium-owned geometry when present. |
| Minecraft 1.21.11 | 21+ | 0.18.5+ | 0.141.4+1.21.11 | Optional 17.0.1-beta.1 | Conservative source-water fast path and particles; Sodium-owned geometry when present. |
| Minecraft 1.21.11 | NeoForge 21.11.45+ | 21+ | — | Native Mods screen | Same scope as Fabric 1.21.11; shares its renderer code. |
| Minecraft 26.3 | Forge 66.0.2+ | 25+ | — | Native Mods screen | Same scope as Fabric 26.3; one-time release in v1.1.0. |
| Minecraft 26.3 | NeoForge development preview | 25+ | — | Native Mods screen | Local preview only; planned once NeoForge 26.3 is stable. |

The 1.21.11 profile uses the remapping Loom plugin and official Mojang mappings, while Fabric 26.3 uses the non-remapping Loom profile. Its older GUI, HUD, key-binding, and liquid-renderer APIs live under `src/1.21.11/client/java` and `wateroptimisation.legacy.mixins.json`. NeoForge 26.3 is developed as a separate preview under `neoforge-26.3/` and is not included in the public v1.1.0 release. NeoForge 1.21.11 lives under `neoforge-1.21.11/` and reuses the Fabric 1.21.11 client sources with its own NeoForge entry point; Forge 26.3 lives under `forge-26.3/`.

## Mod Menu

Mod Menu is an optional compile-only dependency: 21.0.0-beta.1 for 26.3 and 17.0.1-beta.1 for 1.21.11. The core mod loads without it and retains the keybind and native settings screen. NeoForge and Forge use their native Mods screen rather than Mod Menu. On every loader the settings also open with `K`.

## Sodium

Sodium has its own optimized fluid renderer. When the Sodium mod id is detected, Water Optimisation disables all of its vanilla fluid geometry hooks. Sodium remains fully responsible for visibility, fluid shaping, lighting, hidden-fluid culling, translucent collection, and sorting; Water Optimisation applies only local particle settings. The reduced-inward-face experiment is available only on the vanilla renderer. No Sodium geometry bridge is planned; Sodium remains the geometry owner unless project scope is formally reconsidered. The main settings screen reports the effective path and labels overlapping geometry controls as unavailable while Sodium renders water.

## Rendering backends

Minecraft 26.3 can use different rendering backends. The implementation uses Minecraft's Blaze3D/Fabric abstractions and does not call raw OpenGL. Test backends separately when both are available.

## Multiplayer

The mod is client-only and render-focused. It does not add packets, change movement or collision, alter fluid simulation, modify world updates, or expose player-information features. Server rules are separate from technical client-only behavior and must be checked before use.

## Fallback behavior

If a hook is unavailable, another renderer owns the fluid path, Mod Menu is absent, or a shape cannot be classified safely, the relevant feature preserves normal behavior. Hidden-water skipping leaves flowing, partial, waterlogged, overlay, transparent, and unusual states on the normal renderer. Experimental inward-face reduction is separate and may change underwater or transparent-boundary views for eligible ordinary source water.
