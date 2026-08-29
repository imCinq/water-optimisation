# Remote-first Codex Workflow

## Purpose

This plan evaluates whether Codex can implement and verify most of Water Optimisation without using Mac storage or launching separate Java and Gradle processes on the Mac.

## What remains remote

Codex can work from a temporary remote workspace and use the private GitHub repository as the source of truth. The remote workflow may include:

- creating feature branches;
- writing and reviewing Java, Gradle, resource, and documentation files;
- running Java and Gradle builds;
- running unit tests and repository audits;
- inspecting mappings, dependencies, and build output;
- creating commits and updating the project plan;
- producing a temporary JAR for download.

The temporary workspace may be discarded after work is committed. The repository should contain only reproducible project inputs and human-reviewed documentation.

## What stays off the Mac

The Mac does not need:

- a cloned development repository;
- a separate Java installation for Codex;
- a Gradle installation;
- Gradle caches;
- build folders;
- test worlds or runtime logs;
- generated JARs committed to Git.

The Mac will still run Java when Minecraft itself is launched. The goal is to avoid additional local development processes and persistent development storage.

## What must happen locally

Codex cannot reliably replace the user's exact hardware test. The Mac is required for:

- M2 GPU behavior;
- macOS graphics-driver and OpenGL/Vulkan comparison;
- actual FPS, 1% lows, and frame-time measurements;
- visual correctness in the user's Minecraft instance;
- interaction with the installed modpack, Sodium version, resource packs, and settings;
- DonutSMP connection and final server-rule smoke testing.

## Proposed cycle

1. Codex creates a branch for one focused phase.
2. Codex implements the smallest change remotely.
3. Codex runs build, tests, audits, and static checks remotely.
4. Codex commits only source, tests, docs, and configuration.
5. Codex provides a short result summary and a temporary JAR only when local testing is useful.
6. The user runs Minecraft locally and reports FPS, visuals, and compatibility.
7. Codex adjusts the branch based on the measured result.
8. The branch is merged only after both remote and local acceptance criteria pass.

## Storage policy

Keep generated output outside Git:

- Gradle caches;
- build and out directories;
- Minecraft run directories;
- logs and crash reports;
- screenshots and test-world files;
- temporary benchmark captures;
- generated JARs unless a release artifact is intentionally published.

The final downloadable JAR is expected to be small. GitHub Actions artifacts or temporary Codex files should have limited retention and should not become part of the source history.

## Evaluation criteria

The remote-first approach succeeds if it can:

- produce a reproducible build;
- pass automated tests and repository audits;
- preserve the client-only boundary;
- keep generated output out of GitHub;
- create reviewable commits;
- produce a usable test JAR;
- require local work only for Minecraft and hardware-specific validation.

It should be reconsidered if remote builds cannot reproduce the toolchain, if graphics debugging requires local iteration, or if remote execution time outweighs the storage and setup savings.
