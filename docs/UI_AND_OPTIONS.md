# User Experience and Mod Menu

The settings are implemented with target-specific native Minecraft screen APIs. The 26.3 profile uses extraction-based GUI APIs; the 1.21.11 profile uses that version’s older `GuiGraphics` API. Mod Menu is an optional adapter only.

## Mod Menu integration

Mod Menu is compile-only and suggested: version 21.0.0-beta.1 for 26.3 and 17.0.1-beta.1 for 1.21.11. The core client entrypoint does not import Mod Menu. If Mod Menu is absent, the mod keeps its keybind and native settings screen and continues to load.

## Main screen

The main screen contains:

1. an enable switch;
2. a Vanilla, Balanced, Performance, or Maximum FPS preset selector;
3. a short client-only explanation;
4. a read-only effective-path summary;
5. an Advanced settings button;
6. Done and Cancel buttons.

Done saves the working copy. Cancel and Escape return to the parent screen without saving. The screen never changes a server or world setting.

## Advanced screen

Advanced controls are separate from the main screen and are grouped into three sections:

- Performance: skipping hidden water blocks, water particles, particle distance, particle fog culling, and the optional per-tick particle budget;
- Water rendering: the fluid-geometry mode and the optional reduced-face setting;
- Diagnostics: performance statistics and fast-path hook status.

Reset preset is kept with the bottom action buttons because it changes the whole
working copy rather than enabling a diagnostic.

The layout uses two columns at normal widths and falls back to one column on narrow screens. This keeps the performance controls together while separating the visual-risk experiment and diagnostic switches.

The labels are phrased as short questions so their effect is understandable without renderer knowledge: “Skip hidden water blocks?” and “Limit water particles per tick?”. The rendering-mode control identifies the selected mode; the `Experimental reduced inward faces` choice is shown in red, while unavailable Sodium or target states use neutral warning styling. Ordinary Vanilla and Conservative modes do not use warning styling, and hovering the Experimental choice explains that it may cause visual and graphical issues. The hidden-water tooltip states that flowing water, waterlogged blocks, transparent boundaries, partial shapes, overlays, and ambiguous cases stay on vanilla tessellation. Sodium ownership displays a short notice and disables the overlapping vanilla geometry controls because Sodium renders water itself; on 1.21.11, the reduced-face choice is unavailable while the particle and diagnostic controls stay available. The active-path summary is based on the unsaved working copy, so changing a preset immediately explains what Apply will do. The Advanced screen now clips its content, keeps Reset/Done fixed, and scrolls the settings area with the mouse wheel when the window is too short; it still adapts between two columns and one column. Diagnostics separately report what is configured, what the target can activate, whether the hook was observed during this diagnostics session, and how many fluid blocks were actually skipped.

The target implementations intentionally use different evidence: Minecraft 26.3 reuses the renderer's already-captured neighbor locals at its fail-soft first-face hook, while Minecraft 1.21.11 uses an explicit reusable-position probe after center and upward early rejection. Neither path widens its eligibility based on this UI/diagnostics work.

## Recovery

Configuration loading catches invalid or partial JSON and restores safe defaults. Invalid particle distances are clamped. An interrupted write leaves the previous complete configuration in place whenever the filesystem honors atomic replacement.

## Local validation still required

Source-level checks cover the target-isolated screens and renderer adapters; a build still needs to run against both the 26.3 and 1.21.11 toolchains. A later local run should check common GUI scales, readable text, Mod Menu present/absent, keyboard navigation, persistence, and Escape/Cancel behavior on each target.
