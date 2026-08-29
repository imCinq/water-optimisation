# Compatibility

## Minecraft

The first implementation target is Minecraft 26.2 with Java 25 and Fabric. Version-specific rendering internals must be isolated because mappings and pipeline APIs can change.

## Sodium

Sodium has its own optimized fluid renderer. When Sodium is installed, Water Optimisation must not blindly replace or duplicate Sodium's fluid mesh path. Compatibility should be tested explicitly and documented by version.

## DonutSMP

The intended multiplayer boundary is client-side rendering only. The mod must not add movement, inventory, combat, targeting, ESP, radar, freecam, macros, packet manipulation, or server-state changes.

DonutSMP's current rules should be checked before use. Server approval is separate from technical client-only behavior.

References:

- DonutSMP listing: https://modrinth.com/server/donutsmp
- DonutSMP store and rules: https://store.donutsmp.net/

The mod should not assume the server's internal version matches the client's renderer. It must work from the fluid and block state available to the client.

## Fallback behavior

If a hook is unavailable, another renderer owns the section, or a shape cannot be classified safely, the feature must disable itself for that case and preserve normal rendering.
