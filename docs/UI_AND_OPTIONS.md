# User Experience and Mod Menu

The settings are implemented with Minecraft 26.2's native screen and extraction-based GUI APIs. Mod Menu is an optional adapter only.

## Mod Menu integration

Mod Menu 19.0.0-alpha.1 is a compile-only, suggested dependency for the 26.2 target. The core client entrypoint does not import Mod Menu. If Mod Menu is absent, the mod keeps its keybind and native settings screen and continues to load.

## Main screen

The main screen contains:

1. an enable switch;
2. a Vanilla, Balanced, or Performance preset selector;
3. a short client-only explanation;
4. a visible visual-trade-off warning;
5. a More settings button;
6. Done and Cancel buttons.

Done saves the working copy. Cancel and Escape return to the parent screen without saving. The screen never changes a server or world setting.

## Advanced screen

Advanced controls are separate from the main screen:

- water culling mode;
- enclosed-water fast path;
- water particles;
- particle distance;
- distant-effect culling;
- performance stats;
- fallback logging;
- Reset preset.

The labels communicate the choice without requiring knowledge of renderer internals. The enclosed-water fast path and Experimental reduced-face choice remain explicit; the warning explains that the latter can look different underwater. Sodium ownership keeps the choice inactive because Sodium controls its own fluid renderer.

## Recovery

Configuration loading catches invalid or partial JSON and restores safe defaults. Invalid particle distances are clamped. An interrupted write leaves the previous complete configuration in place whenever the filesystem honors atomic replacement.

## Local validation still required

The remote build proves that the screen and optional adapter compile against the 26.2 toolchain. A local run must still check common GUI scales, readable text, Mod Menu present/absent, keyboard navigation, persistence, and Escape/Cancel behavior.
