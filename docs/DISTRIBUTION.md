# Distribution

## Current status

Water Optimisation 0.0.10 is the current public GitHub release. It adds supported Fabric 26.3 alongside standalone NeoForge 26.2 support, Fabric 26.2, and 1.21.1 artifacts, with packaged NeoForge client/server smoke and renderer-mixin checks. Earlier releases remain in the changelog and tags as historical records. Maximum FPS remains an opt-in visual trade-off inside an otherwise client-only release.

## v1.0.0 release line

The v1.0.0 release line includes Fabric 26.3 and Fabric 1.21.11. NeoForge 26.3 is planned as a later support update after its development beta and build tooling stabilise. Fabric 26.2, Fabric 1.21.1, and NeoForge 26.2 remain reproducible only as historical 0.0.10 targets and are not part of the v1.0.0 feature line.

| Target | Status | Current direction |
| --- | --- | --- |
| Fabric 26.3 | Included | Shared modern settings UI and renderer path. |
| Fabric 1.21.11 | Included | Isolated compatibility source set with the same settings semantics and conservative renderer path. |
| NeoForge 26.3 | Planned | Port the shared UI and policy after stable target tooling is available. |

## Historical 0.0.10 build profile

| Field | Value |
| --- | --- |
| Mod version | 0.0.10 |
| Minecraft | 26.3, 26.2 and 1.21.1, with target-specific artifacts |
| Fabric Loader | 0.19.5+ for 26.3; 0.19.3+ for 26.2; 0.16.13+ for 1.21.1 |
| Fabric API | 0.160.6+26.3; 0.158.0+26.2; 0.116.12+1.21.1 |
| NeoForge | 26.2.0.77+ for Minecraft 26.2 |
| Java | 25 for 26.3 and 26.2; 21 for 1.21.1 |
| Mod Menu | Optional, 21.0.0-beta.1 for 26.3; 19.0.0-alpha.1 for 26.2; 11.0.4 for 1.21.1 |
| Environment | Client |
| License | MIT |
| Creator attribution | Cinq |

## Release artifacts

Use the published [GitHub Release](https://github.com/imCinq/water-optimisation/releases/tag/v0.0.10) for the current target-specific Fabric and NeoForge runtime JARs, matching sources JARs, and `SHA256SUMS-0.0.10.txt`.

GitHub Actions artifacts remain temporary build outputs. They expire and are not a substitute for the tagged release.

## Historical Fabric 26.3 support

The Fabric 26.3 build uses `-Ptarget_minecraft=26.3` and `version_263`, producing `build/26.3/libs/water-optimisation-0.0.10-mc26.3-fabric.jar`. It requires Java 25, Fabric Loader `0.19.5`, and Fabric API `0.160.6+26.3`; the API metadata floor is `>=0.160.6+26.3` for 26.3 only. Mod Menu `21.0.0-beta.1` is optional. The expected Fabric 26.3 suite is now 25 tests: 16 config-model tests, 4 context helper tests, and 5 mocked predicate tests. The newer historical local result passed all 25 via `test` only. Earlier evidence separately records a forced `test build verifyArtifact --rerun-tasks` pass with 20 tests, not a 25-test full build. A separate NeoForge 26.2 build passed 20 tests before the new mocked predicate fixture was added. Current-SHA CI passed for builds and startup smoke per runs 35237266215 and 35237266249; transformed-Mixin cancellation/exception validation remains an open nonvisual gate, and startup success alone does not prove optional local-capture hook execution. No project-run in-game tests were run for this hardening, and in-world visual and FPS validation remain outstanding. The [26.3 release notes](FABRIC_26_3_RELEASE_NOTES.md) describe the fixes and remaining limitations. A user-reported pass dated 2026-09-17 covered Apple M2 hardware, macOS 27, OpenGL, Sodium absent, and Mod Menu present on CI runtime `water-optimisation-0.0.10-26.3-preview.2-mc26.3-fabric.jar` (SHA-256 `d50d3074a14a0bd70daa7474e91f4f5d54a8751d6ceb5f4a8f84be6ab0db7d24`); the full visual matrix and FPS remain unvalidated. A follow-up pass covered Mod Menu present and most settings with no failures observed. A further pass covered Sodium present and the Vulkan backend with no failures observed, with Sodium version and scenes unspecified.

Fabric 26.3 support is included in the public 0.0.10 release and its listing facts. Follow the [26.3 build/test/release guide](FABRIC_26_3.md), including OIT on/off, hook observation and skips, Sodium particle-only behavior, both backends on supported hardware, and mod-off/on comparisons. The validation gaps recorded above remain open until the full visual matrix, hook observation, and FPS checks are completed.

## Stable release requirements

Before publishing a stable artifact:

- the exact Minecraft, loader, loader API, Java, optional integration, and companion-mod versions are documented;
- the version in `gradle.properties`, generated loader metadata, changelog, and release notes agrees;
- the JAR is built from a reviewed tagged commit in a clean environment;
- the runtime JAR contents and SHA-256 checksum are inspected and recorded;
- privacy and client-only audits, tests, and the build pass;
- disabled-mode comparisons and the complete visual matrix pass on the target client;
- backend, resource-pack, shader, companion-mod, and multiplayer checks are recorded;
- current server rules are checked before any multiplayer claim;
- no personal information, credentials, server data, or generated runtime files are included;
- the listing makes no universal FPS or server-approval claim.

Use the [public release checklist](RELEASE_CHECKLIST.md) as the acceptance record.

## Publishing sequence for future releases

1. Complete and review the release checklist.
2. Create the version tag from the accepted commit.
3. Rebuild and test the exact tagged commit.
4. Publish a GitHub Release with the runtime JAR, optional sources JAR, SHA-256 checksum, changelog, compatibility notes, and known limitations.
5. Mark the artifact as a pre-release when the documented validation is not yet sufficient for a stable release.
6. Publish to Modrinth only after the release package and project permissions are reviewed.

Publishing is intentionally manual. No publishing automation or update checker is enabled.

## Historical 0.0.10 listing facts

- Name: Water Optimisation
- Creator attribution: Cinq
- Category: Client-side rendering optimisation
- Loader: Fabric for 26.3, 26.2 and 1.21.1; NeoForge for 26.2
- Environment: Client
- Minecraft target: 26.3, 26.2 and 1.21.1, with target-specific artifacts
- Java target: 25 for 26.3 and 26.2; 21 for 1.21.1
- License: MIT
- Required dependency: matching Fabric API build on Fabric; NeoForge 26.2.0.77+ on NeoForge

## v1.0.0 planned listing

- Loader: Fabric for 26.3 and 1.21.11; NeoForge 26.3 after stable tooling
- Environment: Client
- Minecraft target: 26.3 and 1.21.11 initially
- Java target: 25 for 26.3; 21 for 1.21.11
- Required dependency: matching Fabric API build on Fabric; matching stable NeoForge 26.3 line when enabled

## Credential boundary

Never commit API keys or publishing tokens. Any future publishing workflow must be reviewed separately and use repository secrets.

## Multiplayer wording

Describe the project as a client-side rendering and cosmetic-particle optimisation. Do not advertise it as an anti-cheat bypass, competitive advantage, or server-approved modification.
