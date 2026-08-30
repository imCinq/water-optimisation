# Changelog

All notable changes to Water Optimisation are documented here.

## 0.1.0-preview

- Added the Fabric 26.2 client-only project scaffold.
- Added native settings screens with Vanilla, Balanced, and Performance profiles.
- Added local JSON configuration with safe defaults, atomic replacement, clamping, profile reset, and invalid-file recovery.
- Added optional Mod Menu integration and a configuration keybind.
- Added conservative source-water face culling and an interior full-source-water fast path.
- Added camera-relative water-particle admission filtering with a lifecycle-safe player fallback.
- Added opt-in diagnostics for fluid tessellation, section compilation, translucent resorting, face decisions, fast-path skips, and particle filtering.
- Added Sodium renderer-ownership detection so the mod does not compete with an active Sodium fluid renderer.
- Added configuration and particle-distance unit tests, privacy audits, and client-only boundary audits.
- Added public documentation, benchmark templates, and the creator logo asset.
- Added public release checklist and exact preview build-profile documentation.
- Removed redundant setup and agent-only repository files from the public tree.
- Replaced the plan-oriented README with an implementation and testing guide.

This preview still requires visual, performance, renderer, and multiplayer compatibility validation before a stable release.
