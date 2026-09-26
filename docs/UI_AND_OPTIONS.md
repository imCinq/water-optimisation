# User Experience and Mod Menu

The settings are implemented with target-specific native Minecraft screen APIs. The 26.3 profile uses extraction-based GUI APIs; the 1.21.11 profile uses that version’s older `GuiGraphics` API. Mod Menu is an optional adapter only.

## Mod Menu integration

Mod Menu is compile-only and suggested: version 21.0.0-beta.1 for 26.3 and 17.0.1-beta.1 for 1.21.11. The core client entrypoint does not import Mod Menu. If Mod Menu is absent, the mod keeps its keybind and native settings screen and continues to load.

## Settings screen

One tabbed screen holds every option. It opens from Mod Menu on Fabric, the Mods screen on NeoForge and Forge, or the `K` key on every loader. The key was `O` before v1.1.0; Minecraft 26.3 binds `O` to its Friends key.

- **General:** a short client-only explanation, the current effective path ("Right now: …"), the main switch, the preset, Reset to preset, and the performance overlay.
- **Water:** Skip hidden water, and Draw less inside water (experimental).
- **Particles:** Water particles, Particle distance, Hide particles in fog, New particles per tick, and Limit "always show" particles.

Each option is one row: an icon on the left, the option button (`Name: Value`), and a plain-language explanation underneath. A yellow note explains when an option is unavailable or currently has no effect: Sodium owns water geometry, the target lacks the reduced-face hook, water particles are off, the main switch is off, or the Vanilla preset is selected. Unavailable options are disabled rather than hidden.

The rows scroll with the mouse wheel when the window is short; the tabs and the Done/Cancel footer stay fixed. The screen remembers the last tab for the session. Done saves the working copy; Cancel and Escape return without saving. The effective-path line reflects the unsaved working copy.

The target implementations intentionally use different evidence: Minecraft 26.3 reuses the renderer's already-captured neighbor locals at its fail-soft first-face hook, while Minecraft 1.21.11 uses an explicit reusable-position probe after center and upward early rejection. Neither path widens its eligibility based on this UI/diagnostics work.

## Recovery

Configuration loading catches invalid or partial JSON and restores safe defaults. Invalid particle distances are clamped. An interrupted write leaves the previous complete configuration in place whenever the filesystem honors atomic replacement.

## Local validation still required

On 26 September 2026 the v1.1.0 screen was checked in game on Fabric 26.3, Fabric 1.21.11, Forge 26.3, and NeoForge 1.21.11 with no failures reported. A fuller run should still check common GUI scales, readable text, Mod Menu present/absent, keyboard navigation, persistence, and Escape/Cancel behavior on each target.
