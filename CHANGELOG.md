# Changelog

All notable changes to Water Optimisation are documented here.

## Unreleased

No changes yet.

## 0.0.4 — 2026-08-31

- Package target-specific artifacts for Minecraft 26.2 and 1.21.1 in the same GitHub Release.
- Add a target-isolated Minecraft 1.21.1 compatibility profile using Java 21, remapping Loom, official Mojang mappings, older client GUI/HUD APIs, and a conservative source-water/particle path.
- Keep Minecraft 1.21.1 Sodium geometry on a particle-only fallback until its older renderer is reviewed independently.
- Start the dedicated far-water GPU track earlier: document the water-owned pass, independent distance/fog policy, and future LOD boundary without adding an unsafe shared-translucency cull or placeholder setting.
- Make selecting any non-Vanilla preset turn the master switch on, while Vanilla still restores the disabled reference state.
- Show the effective water path in the main settings screen so Sodium ownership and unavailable geometry options are explicit.
- Add a fail-closed Sodium 0.9.x/Minecraft 26.2 bridge for the opt-in reduced-inward-face experiment without duplicating Sodium's fluid renderer, hidden-fluid culling, or translucent sorting.
- Keep unknown Sodium builds on the particle-only fallback until a matching renderer hook is reviewed.

## 0.0.3 — 2026-08-30

Small compatibility and settings-clarity release.

- Explain in Advanced settings when Sodium owns fluid rendering and why the overlapping vanilla fluid-geometry controls are unavailable.
- Show the Sodium notice only when Sodium is present and account for its wrapped height in the responsive layout.
- Preserve the existing client-only boundary, vanilla fallbacks, and Sodium renderer ownership behavior.

## 0.0.2 — 2026-08-30

Polish release focused on making the mod identifiable and easier to install.

- Packaged the supplied water-block logo as the Fabric mod icon so compatible mod lists can display it.
- Added the icon path to the generated Fabric metadata.
- Kept the public README logo, installation guidance, compatibility notes, and client-only boundaries aligned with the packaged artifact.

## 0.0.1 — 2026-08-30

The first official release of Water Optimisation for Minecraft 26.2.

- Published the client-only rendering and cosmetic-particle optimization package with Fabric-compatible metadata.
- Added clear Vanilla, Balanced, Performance, and Maximum FPS profiles with native Minecraft settings screens.
- Added the Cinq water-block logo to the public project presentation and refreshed the README for installation, configuration, compatibility, and testing.
- Kept the conservative hidden source-water fast path as the recommended performance option.
- Kept Maximum FPS opt-in because reduced inward water faces can change unusual inside-water or transparent-boundary views.
- Preserved Sodium renderer ownership and vanilla fallbacks for flowing, waterlogged, partial, transparent, overlay, and ambiguous cases.

## 0.1.0-preview.8

- Added a clear Maximum FPS profile that enables the existing reduced-face GPU path together with the hidden-water fast path.
- Kept the normal Performance profile conservative and vanilla-compatible.
- Reordered the hidden-water predicate to reject common open-surface water before checking all six neighbors, reducing avoidable section-compile work.
- Kept flowing, waterlogged, transparent, overlay, and ambiguous cases on vanilla behavior; Sodium remains the fluid-renderer owner when present.

## 0.1.0-preview.7

- Fixed the Advanced settings action row so the lower controls cannot be covered by the bottom buttons, including compact-height layouts.
- Removed the yellow warning copy and the redundant renderer-status line from the in-game screens.
- Reworded settings as plain-language questions, including “Skip hidden water blocks?” and “Reduce water geometry?”.
- Kept the existing conservative fallback behavior and the opt-in reduced-face GPU experiment unchanged.

## 0.1.0-preview.6

- Reorganized the native settings screens into Safe performance, Experimental GPU, and Diagnostics sections, with a compact two-column layout at normal widths and a narrow-screen fallback.
- Added clear Vanilla/Sodium renderer ownership status to the settings screens.
- Expanded the fully hidden source-water fast path to include faces hidden by full solid-rendering blocks while retaining vanilla fallback behavior for flowing, waterlogged, transparent, overlay, and ambiguous cases.
- Limited experimental reverse-face reduction to ordinary full source-water blocks so flowing and waterlogged water stay vanilla.
- Added a diagnostics counter for the reverse faces actually removed, making the GPU experiment measurable.
- Removed redundant thread-local cleanup from the reduced-face fluid path and kept safe/disabled paths free of thread-local work to reduce section-compilation overhead.
- Kept camera-relative water distance fading and global translucent-sort bypasses deferred because the shared translucent layer cannot safely make them water-only.

## 0.1.0-preview.5

- Added an explicit Experimental reduced-face mode for vanilla fluid rendering.
- Reduced-face mode keeps each outward fluid face but omits vanilla's optional reverse face, cutting translucent water geometry and overdraw at the cost of possible visual differences when viewing water from inside or through unusual transparent arrangements.
- Kept reduced-face mode off in all presets and disabled it automatically when Sodium owns fluid rendering.
- Added diagnostics and UI wording that make the visual trade-off visible before testing.
- Kept water-distance fading out of this preview because the shared translucent section layer cannot safely apply a camera-relative water-only fade without a renderer-specific implementation.

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

The preview entries below are retained as development history. The 0.0.4 package is the current published release for Minecraft 26.2 and 1.21.1; the 1.21.1 compatibility build and all optional performance modes should still be validated on the intended hardware and modpack.
