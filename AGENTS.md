# Repository Guidance

## Purpose

Water Optimisation is a client-side Minecraft rendering project. Work should improve measured frame time in water-heavy scenes without changing gameplay or server-visible behavior.

## Development principles

- Measure before changing rendering behavior.
- Keep conservative behavior as the default until visual correctness is demonstrated.
- Use Minecraft 26.2 Blaze3D, RenderPipeline, RenderType, and Fabric rendering abstractions instead of raw OpenGL.
- Keep version-specific code isolated when mappings or rendering APIs differ.
- Prefer allocation-free hot paths and cached local state during chunk compilation.
- Fall back to vanilla-compatible geometry whenever a block shape, fluid state, overlay, or transparency case is ambiguous.
- Document every intentional visual trade-off.

## Remote-first Codex workflow

When practical, use Codex's temporary remote workspace for implementation, builds, tests, audits, and artifact preparation. Do not require a local clone, Java installation, Gradle installation, or local build cache merely to develop the project.

Commit source, documentation, tests, and reproducible configuration to GitHub. Keep Gradle caches, build outputs, runtime data, logs, screenshots, test worlds, and temporary artifacts out of Git. Commit frequently so work can be recovered if the temporary workspace is discarded.

Remote verification does not replace local hardware verification. The final release candidate still needs a Minecraft run on the target Mac, including M2 GPU behavior, OpenGL/Vulkan behavior, visual correctness, and DonutSMP smoke testing.

## Multiplayer boundary

Do not add networking, custom packets, input automation, movement changes, combat logic, inventory logic, targeting, entity overlays, ESP, radar, freecam, x-ray behavior, macros, or anti-cheat workarounds.

Do not change FluidState, collision, swimming, fluid spread, server ticks, block updates, or world simulation. Rendering decisions must not be used to hide gameplay information.

DonutSMP compatibility must be treated as a validation requirement, not an assumption. A client-only implementation is not automatically permitted by every server.

## Privacy and repository hygiene

- Do not add personal names, personal email addresses, precise locations, credentials, tokens, or private logs.
- Use the GitHub handle imCinq only where repository metadata or ownership needs to be described.
- Never commit generated runtime data, screenshots containing account information, or copied server logs without removing identifiers.
- Do not add telemetry, analytics, update checkers, remote configuration, or outbound network calls.

## Verification

Before considering a change complete:

1. Build the relevant Minecraft target.
2. Run unit tests and repository audits.
3. Test flat water, flowing water, waterlogged blocks, leaves, transparent blocks, caves, and underwater views.
4. Compare visual output with the feature disabled.
5. Record average FPS, 1% lows, long-tail frame time, fluid compilation time, translucent sorting time, and particle counts when relevant.
6. Test with and without Sodium when integration is affected.
7. State whether each check ran remotely or locally.

## Change scope

Keep commits focused. Update the relevant documentation and changelog entry with implementation changes. Do not publish or add a public release workflow until the project has a verified implementation and an explicit distribution decision.
