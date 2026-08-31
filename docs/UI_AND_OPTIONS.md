# User Experience and Mod Menu

The settings are implemented with target-specific native Minecraft screen APIs. The 26.2 profile uses extraction-based GUI APIs; the 1.21.1 profile uses that version’s older `GuiGraphics` API. Mod Menu is an optional adapter only.

## Mod Menu integration

Mod Menu is compile-only and suggested: version 19.0.0-alpha.1 for 26.2 and 11.0.4 for 1.21.1. The core client entrypoint does not import Mod Menu. If Mod Menu is absent, the mod keeps its keybind and native settings screen and continues to load.

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
- Water rendering: the fluid-geometry mode, the optional reduced-face setting, the 26.2-only flat-surface experiment, and the guarded far-water pass;
- Diagnostics: performance statistics and fallback logging.

Reset preset is kept with the bottom action buttons because it changes the whole
working copy rather than enabling a diagnostic.

The layout uses two columns at normal widths and falls back to one column on narrow screens. This keeps the performance controls together while separating the visual-risk experiment and diagnostic switches.

The labels are phrased as short questions so their effect is understandable without renderer knowledge: “Skip hidden water blocks?”, “Limit water particles per tick?”, and “Combine flat water surfaces?”. Sodium ownership displays a short notice and disables the overlapping vanilla geometry controls unless the reviewed Sodium 0.9.x/Minecraft 26.2 face bridge has matched; on 1.21.1, the flat-surface experiment remains unavailable while the particle and diagnostic controls stay available. The effective-path summary is based on the unsaved working copy, so changing a preset immediately explains what Apply will do.

The far-water toggle is labelled `Limit distant water to 64 blocks?` so its effect is explicit. It is disabled by default and unavailable with Sodium or on 1.21.1. It only activates after a section-level preflight proves that the section contains ordinary still water without mixed non-solid model geometry; other sections keep the normal renderer. This is a hard distance cutoff, not a fade, and it must be visually and quantitatively reviewed before being promoted into a preset.

## Recovery

Configuration loading catches invalid or partial JSON and restores safe defaults. Invalid particle distances are clamped. An interrupted write leaves the previous complete configuration in place whenever the filesystem honors atomic replacement.

## Local validation still required

The remote build proves that the screen and optional adapter compile against both the 26.2 and 1.21.1 toolchains. A local run must still check common GUI scales, readable text, Mod Menu present/absent, keyboard navigation, persistence, and Escape/Cancel behavior on each target.
