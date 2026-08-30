# Compatibility

## Minecraft

The implementation target is Minecraft 26.2 with Java 25 and Fabric. Version-specific rendering internals are isolated in a client-only mixin configuration because mappings and pipeline APIs can change.

## Mod Menu

Mod Menu 19.0.0-alpha.1 is a suggested, compile-only dependency for the target. The core mod loads without it and retains the keybind and native screen. The adapter only returns the native configuration screen.

## Sodium

Sodium has its own optimized fluid renderer. When the Sodium mod id is detected, Water Optimisation disables its vanilla FluidRenderer face and tessellation hooks. It does not force a renderer replacement or attempt to call Sodium internals. The particle and configuration paths remain independent.

This guard is a compatibility boundary, not proof of a complete Sodium integration. Test the exact Sodium build with the target Minecraft version before enabling any fluid optimization.

## DonutSMP

The intended multiplayer boundary is client-side rendering only. The mod must not add movement, inventory, combat, targeting, ESP, radar, freecam, macros, packet manipulation, or server-state changes.

DonutSMP's current rules should be checked before use. Server approval is separate from technical client-only behavior.

References:

- DonutSMP listing: https://modrinth.com/server/donutsmp
- DonutSMP store and rules: https://store.donutsmp.net/

The mod should not assume the server's internal version matches the client's renderer. It works only from the fluid and block state already available to the client.

## Fallback behavior

If a hook is unavailable, another renderer owns the section, Mod Menu is absent, or a shape cannot be classified safely, the relevant feature disables itself for that case and preserves normal behavior. The absence of Mod Menu never prevents the core mod from launching.
