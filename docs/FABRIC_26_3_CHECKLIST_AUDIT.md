# Fabric 26.3 checklist audit — no in-game tests

Date: 16 September 2026. Implementation audited: `a3ab4c5`, following `7fae4fb`. Scope: Water Optimisation only; Fabric 26.3. No Minecraft client was launched for this audit. Prior startup evidence is not counted as scene/mixin verification.

## Verdict

**The 26.3 source port and vanilla hook locations check out. The complete checklist does not pass yet.** There are non-play-test hardening gaps around optional hook detection and invocation cleanup, plus limitations in diagnostic interpretation. Visual, performance, hardware and mod-combination assertions cannot be signed off from source inspection.

Legend:
- **VERIFIED**: source, official bytecode, build or isolated Java evidence supports the stated claim.
- **ATTENTION**: a specific limitation or robustness issue remains.
- **RUNTIME ONLY**: intentionally not tested; a code review does not establish the requested outcome.
- **OUT OF SCOPE**: not part of the requested separate Fabric 26.3 target.

## 1. Build / dependencies

- VERIFIED: explicit 26.3 target, Java 25, Loader 0.19.5, Fabric API 0.160.6+26.3, optional Mod Menu 21.0.0-beta.1. Minecraft 26.3 is unobfuscated: the non-remapping Loom plugin is appropriate; no legacy mapping layer is missing.
- VERIFIED: existing Loom 1.17.20 and Gradle 9.7.1 build this target. Updating these again merely to increase the version is not necessary. Gradle deprecation warnings remain; there was no metadata-template parse warning in this audit's build.
- VERIFIED: runtime metadata declares exact Minecraft `26.3`, client environment, Java >=25 and Loader >=0.19.5. Runtime and sources JARs have separate 26.3 filenames and outputs under `build/26.3`.
- ATTENTION: metadata still accepts `fabric-api: "*"`, although the build pins 0.160.6+26.3. The metadata does not itself enforce the documented API minimum.
- VERIFIED, qualified isolation: 26.2 retains its original pins and KEYSYM adapter; 26.3 selects KEYBOARD. Modern rendering code is deliberately shared, not independently forked. Future edits to that shared code require both builds. Also, a default-target `clean` removes the parent `build` directory, including `build/26.3`; don't treat build outputs as permanent release storage.
- VERIFIED: existing build/production-smoke CI matrices include 26.3 and the correct artifact upload path. GitHub CI execution is not established by local checks.
- OUT OF SCOPE: NeoForge 26.3. Maintaining the existing NeoForge 26.2 release does not imply introducing a new loader/version combination in this Fabric-only port. No claim is made about a second mod.

Audit command `./gradlew -Ptarget_minecraft=26.3 build verifyArtifact` passed. Compilation/test tasks were up-to-date from the previously passing clean build; `verifyArtifact` ran. Existing suite contains 16 tests, primarily configuration/policy plus two diagnostics regressions—not 16 water-scene tests. Privacy/client-only audits and diff whitespace checks also passed.

## 2. FluidRendererMixin — exact bytecode checks

VERIFIED against official 26.3 client SHA-1 `e877b6a07acd633fb3bb475002175cec036e7b87`:

- `SectionCompiler.compile` constructs `FluidRenderer` and invokes `tesselate` for nonempty fluid states. Water still goes through this path in the vanilla section compiler.
- `tesselate(BlockAndTintGetter, BlockPos, FluidRenderer.Output, BlockState, FluidState)` exists with the expected argument types. `Output.getBuilder(ChunkSectionLayer)` still returns a `VertexConsumer`.
- `addFace` still takes a VertexConsumer, 20 floats, two ints, then **one boolean**. That boolean is named `addBackFace` in local slot **24**. `argsOnly=true, ordinal=0` therefore selects the intended boolean, not an unrelated flag.
- The first four vertex calls are unconditional. Offset **74** loads slot 24; offset **76** branches around the four reverse-order vertex calls. Changing the argument to false preserves the outward quad.
- `tesselate` calls `shouldRenderFace` five times. Invocation ordinal 0 is at bytecode offset **158**, for **DOWN**, after `renderUp` has already been calculated. Later calls at 193/207/221/235 are north/south/west/east.
- Neighbor lookups remain **down, up, north, south, west, east**. Every capture was checked: slots 6/7 down block/fluid, 8/9 up, 10/11 north, 12/13 south, 14/15 west, 16/17 east; slot 18 is boolean `renderUp`. All are live at the first face-check invocation.
- `javap -c -p -l` output for the complete vanilla FluidRenderer matches 26.2 exactly, including its local-variable table. This does not mean every renderer class is unchanged.

ATTENTION: the hidden-water hook is explicitly `require=0` with `CAPTURE_FAILSOFT` (`FluidRendererMixin.java:73–103`). A mismatch can leave vanilla running without the optimization. Mixin may warn and the enabled HUD can show “not observed”, but there is no unconditional startup capability verification or automatic policy downgrade based on injection success. With diagnostics off, users may believe the selected fast path is effective when it is not. The vanilla-bytecode comparison is not an audit of every Fabric/mod-transformed class.

Recommendation: add an exact-artifact transformed-hook check to CI and distinguish hook unavailable from selected/active. Do not simply change all injections to fail-hard: that can turn a safe fallback into a compatibility crash. No cleaner, equally cheap stable hook was demonstrated in this audit. A new HEAD hook that repeats six lookups is not automatically an improvement over verified local capture.

## 3. Flat source-water fast path

VERIFIED as predicate logic (`FluidOptimizationPolicy.java:109–153`):

- The subject must be `Blocks.WATER`, `Fluids.WATER`, and a source. Flowing water, waterlogged blocks and bubble-column blocks cannot be optimized subjects.
- All six neighbors must be ordinary source-water blocks or `isSolidRender()` blocks. Up is rejected first for the common surface case.
- Water touching air cannot be skipped. Ordinary glass, ice, leaves and non-solid translucent/partial boundaries remain conservative when their block states do not report solid rendering. This is a vanilla block-state predicate, not a universal material whitelist for arbitrary mods.
- Geometry decisions are made from the current compilation's supplied neighbors. The mod does not cache a permanent “interior” classification.
- Applying changed fluid-rendering settings requests `levelExtractor.allChanged()` (`ConfigManager.java:163–167`). Ordinary world updates/chunk invalidation remain vanilla's responsibility.

RUNTIME ONLY, not signed off: interior ocean, surface, floor, river, small lake, one-block hole, deep water, every transparent contact, waterlogging, bubble columns, flow/waterfalls, water next to lava, chunk and section/Y borders, unloaded neighbors, fast chunk loading, block-change rebuilds and no holes when water becomes exposed. Source inspection supports the intended predicates but is not visual coverage of these scenes.

## 4. Backface reduction / ThreadLocal

VERIFIED: reduction checks the policy and the invocation's ordinary-source-water marker before suppressing an optional backface (`FluidRendererMixin.java:29–47`). It does not intentionally remove outward faces. Successful fast-path cancellation explicitly removes the marker at line 135; ordinary returns remove it when the policy remains enabled at lines 148–149.

ATTENTION: cleanup is not guaranteed for every exit. The RETURN hook re-reads mutable `reducedWaterBackfacesActive()`. If it was enabled at entry but disabled while a worker tessellates, the RETURN cleanup can be skipped. A vanilla exception or another mixin's cancellation can also bypass the expected cleanup. This retains a Boolean marker, not a large object graph, and the next enabled tessellation ordinarily overwrites it; **visual corruption from that stale marker is not established**. Nevertheless, “no ThreadLocal leakage between jobs” cannot be checked off. Use invocation-scoped try/finally/context handling if hardening this; preserve the disabled hot path and any nested-call semantics.

RUNTIME ONLY: above/below water, submerged/crossing camera, third-person clipping, spectator, boats, swimming, glass/stained glass/translucent entities, and unusual inward-face visibility. Maximum FPS is explicitly a visual trade-off, not an unconditional promise of vanilla-equivalent visuals.

## 5. OIT / ResortTransparencyTaskMixin

VERIFIED from `GameRenderer` and `LevelRenderer` bytecode:

- Effective Improved Transparency requires the option enabled and terrain wireframe disabled.
- In 26.3 `LevelRenderer.scheduleResort`, offset 74 calls `useImprovedTransparency`; offset 77 skips the `resortTransparency` call at 81 when enabled. The 26.2 version lacks that gate.
- The scheduler still traverses candidate sections under OIT. Previously queued resort tasks can still execute: neither `resortTransparency()` nor the task's `doTask()` independently checks OIT.
- Initial section compilation still sorts translucent quads even under OIT: `SectionCompiler.compile`, offset 463, calls `MeshData.sortQuads`; this builds real sorted indices via `VertexSorting.sort`. OIT does **not** remove all CPU sorting in this version.
- The resort class exists, and `doTask(SectionBufferBuilderPack)` returns synchronous `SectionTaskResult`. The inspected task disassembly is identical between 26.2 and 26.3.
- The mod's mixin only times HEAD/RETURN. It never cancels the task, sorts extra geometry, or suppresses sorting. There is no “sorting optimization” to remove globally or make OIT-aware.

ATTENTION: `translucentResortCalls` counts normal task completions including early CANCELLED/no-op exits, not successful sorts. Time includes buffer-placement retry work, not merely index sorting. Initial sorting is charged to section compile timing instead. The HUD's average of 0 with no samples is not evidence of zero sorting cost. Prefer a no-samples indicator and task counts; do not blindly label OIT as “sorting disabled”.

RUNTIME ONLY: task-number/time comparison, actual OIT ON/OFF behavior and all ordering combinations (glass, particles, slimes, translucent items/entities, clouds, beacon, text displays, overlapping layers, chunk boundaries), flicker/popping/alpha/depth outcomes.

## 6. SectionCompiler / MultiDrawIndirect / shaders

VERIFIED:

- The explicit four-argument `compile` descriptor matches. CompileTask calls it with camera-relative `VertexSorting` and section builder buffers.
- Nonempty fluid state reaches `FluidRenderer.tesselate` at compile offset 243. The model chooses its layer; the output callback gets/begins that layer using a QUADS buffer builder.
- Section diagnostics time the **whole section compile**, not water alone: block/fluid processing, mesh building, initial translucent sorting and visibility work. Later uploads/draw submission are outside that timer.
- Indirect terrain rendering requires device support and no known-issue workaround. The render path combines that availability with its render-state selection flag, creates indexed indirect commands and submits through `RenderPass.drawIndexedIndirect`.
- This alters draw preparation/submission, not the demonstrated fluid-compilation entry point. The mod's diagnostics do not count draw calls or time GPU work.
- No custom terrain shaders are packaged in the JAR; reviewed Java code contains no raw OpenGL/Vulkan draw calls or ShaderC/terrain-shader assumptions. No ShaderC-specific Java port was demonstrated as necessary.

RUNTIME ONLY: threaded Minecraft compilation, rapid rebuilds, F3+T, render-distance changes, enabled MultiDrawIndirect benchmarks, GPU benefit/bottleneck shifts, draw-call counts, backend and shader compilation outcomes.

## 7. Diagnostics — non-game tests and limitations

An isolated Java probe against the built main classes passed without launching Minecraft:

- Face calls, requested reverse faces, removed reverse faces and skip counters increment as expected.
- A second end call does not double-count; face calls after closing an invocation do not attach to it.
- Counter-generation isolation survives resets.
- Disabled diagnostic recording does not increment counters.
- Four worker threads performing 1,000 invocations each produced exactly 4,000 fluid visits/faces, section completions and resort completions.

This supplements, but does not replace, the 16 existing tests. The probe was exploratory and saved outside the repository; it is not a new CI regression test.

ATTENTION:

- `recordFluidFace` means outward face-emission calls for **all fluids passing this renderer**, including lava, not water-only mesh totals. Requested reverse faces are recorded before optional suppression. On a stable complete invocation, estimated emitted quads = face calls + reverse requests - removed backfaces; four vertices per quad. These are estimates from hooks, not a measured GPU mesh inventory.
- With diagnostics stable, cancellation/end is idempotent and counter generations are isolated. But outer mixins gate RETURN calls on the current enabled flag: toggling diagnostics off or taking an exceptional exit can skip closure. The next begin resets/overwrites timing state; strict begin/end balancing for all execution paths is not guaranteed.
- Disabled instrumentation still pays method/volatile-gate overhead. No clock/counter work on guarded paths is a source-level observation, not proof of “zero measurable overhead”. Backface state tracking is separate from diagnostics and can remain active.
- No allocation, CPU, GPU, frame-time or benchmark-distortion measurements were performed.

## 8. Compatibility / final release matrix

VERIFIED in source and existing pure policy tests: detected Sodium disables Water Optimisation geometry changes and retains particles. Settings show Sodium ownership. Input uses vanilla constants rather than hardcoded SDL/GLFW numbers. No server entrypoint or gameplay/network mutation was introduced.

ATTENTION: capability detection recognizes Sodium, not arbitrary replacement renderers or every transparency/chunk mod. `supportsReducedWaterBackfaces()` returns true for the modern client; it is not a live transformed-hook compatibility test. Policy gating happens inside applied vanilla hooks; it does not universally prevent mixin conflicts during transformation. Iris/shader and unknown-renderer safety cannot be inferred from the Sodium gate alone.

RUNTIME ONLY — all remain explicitly excluded from this audit:

- Actual Sodium 26.3 / Mod Menu / Iris / shader / transparency / chunk-mod combinations.
- Every visual torture scene: huge oceans above/below, fast flight, rotation, aquariums/entities, mixed flow, waterlogged builds, bubble farms, monuments, rain, time of day, render distance, FOV and camera modes.
- Every performance comparison: vanilla-vs-mod 26.3 with matched OIT/settings, cold load/static/moving scenes; average/1%/0.1% FPS, frame-time graphs, CPU/GPU utilization, memory allocation, compile/vertex/face/draw metrics and actual benefit.
- macOS Apple Silicon, Windows and Linux with supported Vulkan/OpenGL combinations; OIT, fullscreen/windowed, VSync, fresh/existing worlds, multiplayer, long sessions, no crashes/corruption or mixin warnings across those configurations.

README, changelog and distribution docs correctly describe an unpublished preview and validation gaps. This audit found no reason for a renderer rewrite, but it is **not** a stable-release clearance. No gameplay source was modified or JAR rebuilt to introduce behavioral changes during the audit.

## Evidence references

Project files: `build.gradle`; `src/main/resources/fabric.mod.json`; `src/client/java/io/github/imcinq/wateroptimisation/{FluidOptimizationPolicy,ConfigManager,WaterOptimisationClient}.java`; client mixins; `src/main/java/io/github/imcinq/wateroptimisation/{Diagnostics,EffectiveWaterPolicy}.java`.

Official JARs: 26.3 SHA-1 above, 26.2 SHA-1 `2dc72797acbc1b63fc16a11c4ac393605f453754`. `javap` evidence includes FluidRenderer with locals, SectionCompiler, CompileTask, ResortTransparencyTask, RenderSection, LevelRenderer, GameRenderer, MeshData and SortState. Offsets quoted are method bytecode offsets, not Java source line numbers.

Research background: [Fabric 26.3 guide](https://fabricmc.net/2026/09/15/263.html), [OIT changes](https://www.minecraft.net/en-us/article/minecraft-26-3-snapshot-2), [indirect terrain changes](https://www.minecraft.net/en-us/article/minecraft-26-3-snapshot-6). The conclusions about remaining initial sorting are from final-release bytecode, not assumptions from those announcements.
