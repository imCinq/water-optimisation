# Minecraft 26.2 Water Research

## Executive conclusion

The practical client-side FPS targets are translucent water geometry, chunk-rebuild work, translucent overdraw and sorting, and water-related particles. Fluid simulation is server-authoritative and should remain untouched.

## Vanilla rendering path

During chunk-section compilation, Minecraft processes non-empty fluid states through FluidRenderer.tesselate. The renderer:

- checks neighboring block and fluid states;
- decides whether the top, bottom, and horizontal faces are visible;
- samples fluid heights and calculates four corner heights;
- calculates flow direction for flowing texture orientation;
- applies light and biome tint;
- emits still, flowing, and optional overlay geometry.

Water uses still and flowing materials plus a water overlay and is placed in the translucent terrain path. Translucent geometry can create overdraw and requires sorting work. Geometry is generally reused until the section is rebuilt, so steady-state rendering and rebuild spikes should be measured separately.

## Fluid simulation

FlowingFluid handles level propagation, scheduled updates, spreading, slope checks, and source conversion. Water state arrives from the server in multiplayer. A client-only renderer must not replace fluid states with air, change collision, or alter spread behavior.

## 26.2 rendering constraints

Minecraft 26.2 supports an optional Vulkan backend in addition to OpenGL. The mod must use Blaze3D and Minecraft's rendering abstractions so it remains backend-independent. Raw OpenGL assumptions are not acceptable.

## Useful upstream references

- Minecraft 26.2 release notes: https://www.minecraft.net/en-us/article/minecraft-java-edition-26-2
- Fabric 26.2 rendering guidance: https://docs.fabricmc.net/develop/rendering/basic-concepts
- Fabric fluid-rendering API: https://maven.fabricmc.net/docs/fabric-api-0.154.2%2B26.2/net/fabricmc/fabric/api/client/render/fluid/v1/FluidRendering.html
- Minecraft 26.2 FluidRenderer reference: https://aldak.netlify.app/javadoc/26.2.x/net/minecraft/client/renderer/block/fluidrenderer
- Sodium fluid renderer reference: https://raw.githubusercontent.com/CaffeineMC/sodium/dev/common/src/main/java/net/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/DefaultFluidRenderer.java
- Sodium particle fog-culling work: https://github.com/CaffeineMC/sodium/pull/2766

## Decisions

- Do not make water tint caching the first feature; the major historical average-water-color bottleneck was addressed before 26.2.
- Do not globally disable translucent sorting.
- Do not aggressively cull through waterlogged blocks, leaves, overlays, or unusual transparency without shape-aware tests.
- Add instrumentation before claiming an FPS improvement.
