# Minecraft Water Rendering Research

## Executive conclusion

The practical client-side FPS targets are translucent water geometry, chunk-rebuild work, translucent overdraw and sorting, and water-related particles. Fluid simulation is server-authoritative and should remain untouched.

The earlier dedicated far-water pass prototype was removed after visual correctness failures. A future GPU/fill-rate path must first prove water ownership, camera following, translucent ordering, and measurable frame-time benefit before it returns to the public build.

The project also has a target-isolated Minecraft 1.21.1 compatibility profile. It uses the older liquid-renderer and client GUI/HUD APIs, keeps the geometry proof narrower than 26.2, and leaves Sodium-owned fluid geometry untouched. No Sodium geometry bridge is planned unless project scope is formally reconsidered.

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

## Upstream rendering findings

The Minecraft 26.2 rendering guidance requires the Blaze3D abstraction because the release has an optional Vulkan backend; this project therefore does not use raw OpenGL: https://docs.fabricmc.net/develop/rendering/basic-concepts

Sodium's current `DefaultFluidRenderer` performs shape-aware face visibility, same-fluid culling, cached occlusion comparisons, fluid-height sampling, and an optional flooded-cave heuristic. Its `TranslucentGeometryCollector` gathers translucent quads and chooses sorting data for each section. Those optimizations depend on Sodium's renderer, level slice, and shape-cache ownership, so this mod does not copy or duplicate them. Sodium remains the owner of fluid rendering when detected, and this mod does not install a Sodium geometry mixin:

- https://raw.githubusercontent.com/CaffeineMC/sodium/dev/common/src/main/java/net/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/DefaultFluidRenderer.java
- https://raw.githubusercontent.com/CaffeineMC/sodium/dev/common/src/main/java/net/caffeinemc/mods/sodium/client/render/chunk/translucent_sorting/TranslucentGeometryCollector.java

Sodium's particle fog-occlusion work reports large but highly situational gains for long-distance particle effects, while noting that water fog reduced particle counts less in its testing. That supports bounded water-particle admission, but not a universal FPS claim: https://github.com/CaffeineMC/sodium/pull/2766

The safe presets still have no renderer-independent GPU shortcut: vanilla already removes faces between equal fluids, and the interior fast path removes CPU tessellation work for fully hidden source-water blocks but does not reduce visible surface geometry. Preview.7 keeps the explicitly optional vanilla-only reduced-face path limited to ordinary source-water blocks and counts the reverse faces it removes. It can reduce translucent vertex work and overdraw, but may change views from inside water; it is disabled when Sodium owns the renderer. A camera-relative water-distance fade remains out of scope because water shares the translucent section layer with other geometry and section meshes are compiled asynchronously. Global translucent-sort bypasses remain unsafe because they can change ordering around waterlogged blocks, glass, leaves, overlays, and other translucent quads.

The practical additions from this review are split by confidence:

- ready now: target-isolated 1.21.1 build/API support, conservative fallbacks, and clearer compatibility documentation;
- deferred experiment: a water-owned GPU/fill-rate path with a new correctness proof and controlled measurements;
- out of scope: a Sodium-specific reverse-face reduction; Sodium remains the geometry owner unless project scope is formally reconsidered;
- later prototype: a fade, independent fog policy, reduced vertex density, or half-resolution far water;
- not approved: a shared-translucent distance cull, global sort bypass, raw OpenGL path, or returning the failed far-water prototype without new evidence.

MoreCulling was reviewed as a comparable culling project, but its source is GPL-3.0 and its implementation is not suitable for copying into this MIT project without permission: https://github.com/FxMorin/MoreCulling
