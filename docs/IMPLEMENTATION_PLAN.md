# Implementation Plan

Remote-first Codex development is evaluated throughout this plan. It is a delivery method, not a replacement for local Minecraft validation.

## Cross-cutting workstream — Remote-first Codex evaluation

The goal is to determine how much of the project Codex can complete without using storage or Java/Gradle processes on the Mac.

### Remote tasks

Codex can:

- create and modify the Fabric project in a temporary remote workspace;
- create branches and commits directly against the private GitHub repository;
- run Java, Gradle, unit tests, repository audits, and build tasks remotely;
- inspect build output and dependency changes;
- produce a temporary test JAR without committing generated artifacts;
- update documentation and issue plans.

### Local-only tasks

The user still needs to run the final client locally for:

- M2 GPU and macOS graphics-backend behavior;
- real frame-time and FPS measurements;
- visual comparison in the exact Minecraft instance;
- Sodium/resource-pack/modpack interaction;
- DonutSMP connection and server-rule smoke testing.

### Evaluation checklist

- [x] Create a Codex feature branch without cloning to the Mac.
- [x] Add the minimal Fabric 26.2 scaffold remotely.
- [x] Run the remote build and unit tests successfully for the current implementation.
- [x] Run repository privacy and client-only audits remotely.
- [x] Review the diff and commit only source, tests, docs, and configuration.
- [x] Produce a temporary JAR without adding build output to Git.
- [x] Confirm the Mac needs no Java or Gradle installation for the remote workflow.
- [ ] Download only the current implementation JAR required for local Minecraft testing.
- [ ] Record local M2/backend/visual/DonutSMP results.
- [ ] Decide whether remote-first remains the default after the first full implementation cycle.

### Known limitations

Remote builds use different operating-system, Java, GPU, driver, and dependency-cache conditions. They prove compilation and automated behavior but cannot prove M2 rendering performance or server acceptance. Remote work also uses Codex execution time and requires regular commits because a temporary workspace may be discarded.

## Phase 0 — Baseline and instrumentation

The code includes opt-in local fluid, particle, section-compilation, and translucent-resort counters/timing. The report template defines repeatable scenes and the required environment fields. Frame-time distributions and tail latency must still be recorded with Minecraft's profiler/Tracy output during the local run.

Status: remote instrumentation and reporting template complete; live counter sanity checks and baseline measurements pending.

## Phase 1 — Scaffold, configuration, and Mod Menu

Status: implemented and remotely build-verified. The native screen, atomic local JSON, profile reset, optional Mod Menu adapter, keybind, invalid-config recovery, and disabled no-op behavior are present. Local GUI and Mod Menu checks remain pending.

## Phase 2 — Particle filtering

Status: implemented behind the opt-in master switch. Water-specific client particle admission is limited by distance, with an explicitly conservative fog/distance tightening option. Fluid state, particle physics, and non-water particles are untouched. Local visual and stress-scene measurements remain pending.

## Phase 3 — Conservative face culling

Status: implemented for exact full source-water neighbors. The policy does not classify partial shapes, waterlogged blocks, overlays, transparent neighbors, or flowing boundaries. Local visual comparison remains required.

## Phase 4 — Flat source-water fast path

Status: implemented as an explicit opt-in cancellation only for fully interior ordinary source-water blocks. Irregular levels and boundaries fall back to the vanilla tessellator. Local compile-time and visual comparison remain required.

## Phase 5 — Sodium compatibility

Status: renderer-ownership detection is implemented. When Sodium is loaded, the vanilla FluidRenderer optimizations are disabled and no replacement renderer is installed. The Sodium-present/absent and backend matrix remains pending.

## Phase 6 — Release review

Status: privacy, client-only, version, distribution, and server-boundary documentation is present. DonutSMP rule review, public distribution, and release decisions remain intentionally pending.
