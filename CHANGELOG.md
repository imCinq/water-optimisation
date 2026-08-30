# Changelog

All notable changes to Water Optimisation are documented here.

## 0.1.0-preview.4

- Sampled fluid timing in the diagnostics HUD to reduce measurement overhead.
- Made the Performance preset disable cosmetic water particles by default and enable its conservative fog-tightened bound.
- Clarified that diagnostics are for cross-checking and should be disabled for final FPS samples.

## 0.1.0-preview.3

- Added the Fabric 26.2 client-only project scaffold.
- Added native settings screens with Vanilla, Balanced, and Performance profiles.
- Added local JSON configuration with safe defaults, atomic replacement, clamping, profile reset, and invalid-file recovery.
- Added optional Mod Menu integration and a configuration keybind.
- Added a conservative interior full-source-water fast path.
- Added camera-relative water-particle admission filtering with a lifecycle-safe player fallback.
- Added opt-in diagnostics for fluid tessellation, section compilation, translucent resorting, fast-path skips, and particle filtering.
- Added Sodium renderer-ownership detection so the mod does not compete with an active Sodium fluid renderer.
- Added configuration and particle-distance unit tests, privacy audits, and client-only boundary audits.
- Added public documentation, benchmark templates, and the creator logo asset.
- Added public release checklist and exact preview build-profile documentation.
- Removed redundant setup and agent-only repository files from the public tree.
- Replaced the plan-oriented README with an implementation and testing guide.
- Fixed the SectionCompiler mixin target to match vanilla Minecraft 26.2.
- Cached configuration gates in fluid and diagnostics hot paths, reset diagnostics on saved test configuration changes, and exposed active fluid hooks and face-override counters in the diagnostics HUD.
- Added an early return for inactive fluid hook paths so disabled diagnostics and non-fast-path profiles do not perform unnecessary per-fluid policy work.
- Made the conservative interior-water probe fail fast on the open-facing side and reuse ordinary water block state data instead of performing a second fluid-region lookup.
- Made diagnostics resets generation-safe and invalidate compiled water geometry after an effective configuration change, so post-toggle counters exclude in-flight pre-toggle work and newly selected settings are rendered immediately.
- Routed settings-triggered render refreshes through Minecraft 26.2's level-extractor lifecycle to avoid clearing visible terrain directly from the settings callback.
- Removed the redundant same-fluid face override after confirming vanilla already culls those faces, eliminating the per-face policy and diagnostics callbacks.
- Moved the interior fast-path decision to vanilla's first face call so it reuses the six neighbor states already loaded by `FluidRenderer` instead of repeating chunk lookups.
- Combined the fluid-block diagnostic increment with the existing timing counter lookup to reduce diagnostic overhead when profiling is enabled.
- Reduced the Performance profile's cosmetic water-particle admission radius from 48 to 24 blocks to lower particle work while retaining nearby effects.
- Avoided rebuilding every rendered section when only particle or diagnostics settings change.
- Added a no-op fast exit for the particle hook while the mod is disabled or on the Vanilla preset.
- Cached particle-filter settings and the squared distance bound to reduce repeated CPU work for water-particle admission.
- Simplified the in-game settings labels and clarified the Sodium/vanilla fallback behavior.

This preview still requires visual, performance, renderer, and multiplayer compatibility validation before a stable release.
