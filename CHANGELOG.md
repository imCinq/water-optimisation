# Changelog

All notable changes to this project will be documented here.

## Unreleased

- Created the private planning and research workspace.
- Documented the Minecraft 26.2 water-rendering pipeline.
- Added the initial implementation roadmap and benchmark methodology.
- Added the MIT license and project conduct policy.
- Added maintenance, distribution, and repository privacy-audit guidance.
- Added the optional Mod Menu, simple-profile, and Advanced-settings plan.
- Added the remote-first Codex implementation and verification plan.
- Added the initial Fabric 26.2 client-only build scaffold and remote build workflow on the issue #7 branch.
- Added a client-only boundary audit for the scaffold.
- Implemented local JSON configuration with safe defaults, atomic replacement, clamping, profile reset, and invalid-file recovery.
- Implemented native main and Advanced settings screens, a registered configuration keybind, optional Mod Menu integration, and English translations.
- Implemented opt-in diagnostics for fluid tessellation, section compilation, translucent resort timing, face decisions, fast-path skips, and particle filtering.
- Implemented camera-relative water-particle admission filtering with a player-position lifecycle fallback and an explicitly conservative fog/distance mode.
- Implemented exact source-water face culling and an explicit full-interior source-water fast path with vanilla fallbacks.
- Added Sodium renderer-ownership detection that disables vanilla fluid hooks without replacing the active renderer.
- Added configuration unit tests and updated the client-only audit to distinguish local chat components from packet APIs.
- Local Minecraft visual/performance, OpenGL/Vulkan, Sodium, modpack, and DonutSMP validation remain required before release.
