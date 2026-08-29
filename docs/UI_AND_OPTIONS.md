# User Experience and Mod Menu

## Goals

The settings should be simple enough for a normal player while still allowing controlled renderer experiments.

The user should be able to install the mod, open Configure from Mod Menu, select a profile, and stop there. No technical knowledge of fluid meshes, translucent sorting, or mixins should be required.

## Mod Menu integration

Mod Menu is optional:

- declare the Mod Menu version as a suggested dependency for the Minecraft target;
- expose a Mod Menu entrypoint that returns the Water Optimisation configuration screen;
- keep the core client entrypoint independent from Mod Menu;
- avoid a hard dependency or crash when Mod Menu is absent;
- keep the screen implemented with Minecraft's native UI APIs unless a future dependency is justified;
- verify the exact Mod Menu version against the 26.2 toolchain before adding it to the build profile.

The integration should follow the same small adapter pattern used by FPS Tune: a separate integration class, a separate screen class, and no renderer logic inside the Mod Menu adapter.

## Main screen

The main screen contains:

1. an enable switch;
2. a performance-profile selector;
3. a short summary of what the selected profile changes;
4. a visible warning that stronger performance settings can reduce visual density or alter edge cases;
5. an Advanced settings button;
6. Done and Cancel behavior.

Recommended profiles:

- Vanilla — feature disabled and useful for comparison;
- Balanced — recommended starting point with conservative behavior;
- Performance — stronger local reductions after they have been validated.

The screen should show whether the selected profile is active and should not imply a guaranteed FPS increase.

## Advanced screen

Advanced settings may expose:

- fluid culling mode;
- flat source-water fast path;
- water-particle behavior;
- particle distance;
- fog culling;
- diagnostics HUD;
- fallback logging.

Experimental settings must be visibly marked, default off, and accompanied by a Reset to profile action.

## Apply and recovery behavior

- Changes are edited in memory until Done.
- Cancel and Escape restore the previous values.
- Invalid or partially written configuration returns to safe defaults.
- If another renderer owns fluid compilation, the relevant optimisation disables itself and reports a local compatibility status rather than crashing.
- The configuration screen remains usable when the renderer feature is unavailable.

## Accessibility and clarity

- Use readable labels rather than internal field names.
- Keep descriptions short enough to fit at common window sizes.
- Do not rely on color alone to communicate warnings or state.
- Tooltips should explain both the expected performance effect and the possible visual trade-off.
- Avoid exposing diagnostic counters on the main screen.

## Non-goals

- no required Mod Menu installation;
- no required Cloth Config dependency in the first version;
- no automatic profile switching until measurements demonstrate that it is useful;
- no server-specific controls or features;
- no settings that alter collision, movement, or world simulation.
