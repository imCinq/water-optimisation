# User Experience and Mod Menu

The settings are implemented with Minecraft 26.2's native screen and extraction-based GUI APIs. Mod Menu is an optional adapter only.

## Mod Menu integration

Mod Menu 19.0.0-alpha.1 is a compile-only, suggested dependency for the 26.2 target. The core client entrypoint does not import Mod Menu. If Mod Menu is absent, the mod keeps its keybind and native settings screen and continues to load.

## Main screen

The main screen contains:

1. an enable switch;
2. a Vanilla, Balanced, Performance, or Maximum FPS preset selector;
3. a short client-only explanation;
4. an Advanced settings button;
5. Done and Cancel buttons.

Done saves the working copy. Cancel and Escape return to the parent screen without saving. The screen never changes a server or world setting.

## Advanced screen

Advanced controls are separate from the main screen and are grouped into three sections:

- Performance: skipping hidden water blocks, water particles, particle distance, and particle fog culling;
- Water rendering: the fluid-geometry mode, including the optional reduced-face setting;
- Diagnostics: performance statistics and fallback logging.

Reset preset is kept with the bottom action buttons because it changes the whole
working copy rather than enabling a diagnostic.

The layout uses two columns at normal widths and falls back to one column on narrow screens. This keeps the performance controls together while separating the visual-risk experiment and diagnostic switches.

The labels are phrased as short questions so their effect is understandable without renderer knowledge. Sodium ownership disables the two vanilla fluid controls because Sodium controls its own fluid renderer; the remaining local particle and diagnostic controls stay available.

## Recovery

Configuration loading catches invalid or partial JSON and restores safe defaults. Invalid particle distances are clamped. An interrupted write leaves the previous complete configuration in place whenever the filesystem honors atomic replacement.

## Local validation still required

The remote build proves that the screen and optional adapter compile against the 26.2 toolchain. A local run must still check common GUI scales, readable text, Mod Menu present/absent, keyboard navigation, persistence, and Escape/Cancel behavior.
