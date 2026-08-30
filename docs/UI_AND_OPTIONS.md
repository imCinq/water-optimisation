# User Experience and Mod Menu

The settings are implemented with Minecraft 26.2's native screen and extraction-based GUI APIs. Mod Menu is an optional adapter only.

## Mod Menu integration

Mod Menu 19.0.0-alpha.1 is a compile-only, suggested dependency for the 26.2 target. The core client entrypoint does not import Mod Menu. If Mod Menu is absent, the mod keeps its keybind and native settings screen and continues to load.

## Main screen

The main screen contains:

1. an enable switch;
2. a Vanilla, Balanced, or Performance profile selector;
3. a short client-only explanation;
4. a visible visual-trade-off warning;
5. an Advanced settings button;
6. Done and Cancel buttons.

Done saves the working copy. Cancel and Escape return to the parent screen without saving. The screen never changes a server or world setting.

## Advanced screen

Advanced controls are separate from the main screen:

- fluid culling mode;
- flat source-water fast path;
- water particles;
- particle distance;
- conservative fog/distance tightening;
- diagnostics HUD;
- fallback logging;
- Reset to profile.

The labels communicate the choice without requiring knowledge of renderer internals. The fast path and experimental culling choice remain explicit.

## Recovery

Configuration loading catches invalid or partial JSON and restores safe defaults. Invalid particle distances are clamped. An interrupted write leaves the previous complete configuration in place whenever the filesystem honors atomic replacement.

## Local validation still required

The remote build proves that the screen and optional adapter compile against the 26.2 toolchain. A local run must still check common GUI scales, readable text, Mod Menu present/absent, keyboard navigation, persistence, and Escape/Cancel behavior.
