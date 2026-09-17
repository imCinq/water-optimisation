# Distribution

## Current status

Water Optimisation 0.0.9 is the current public GitHub release. It adds standalone NeoForge 26.2 support alongside Fabric 26.2 and 1.21.1 artifacts, with packaged NeoForge client/server smoke and renderer-mixin checks. Earlier releases remain in the changelog and tags as historical records. Maximum FPS remains an opt-in visual trade-off inside an otherwise client-only release.

## Current build profile

| Field | Value |
| --- | --- |
| Mod version | 0.0.9 |
| Minecraft | 26.2 and 1.21.1, with target-specific artifacts |
| Fabric Loader | 0.19.3+ for 26.2; 0.16.13+ for 1.21.1 |
| Fabric API | 0.158.0+26.2; 0.116.12+1.21.1 |
| NeoForge | 26.2.0.77+ for Minecraft 26.2 |
| Java | 25 for 26.2; 21 for 1.21.1 |
| Mod Menu | Optional, 19.0.0-alpha.1 for 26.2; 11.0.4 for 1.21.1 |
| Environment | Client |
| License | MIT |
| Creator attribution | Cinq |

## Release artifacts

Use the published [GitHub Release](https://github.com/imCinq/water-optimisation/releases/tag/v0.0.9) for the current target-specific Fabric and NeoForge runtime JARs, matching sources JARs, and `SHA256SUMS-0.0.9.txt`.

GitHub Actions artifacts remain temporary build outputs. They expire and are not a substitute for the tagged release.

## Fabric 26.3 preview — unpublished

The separate `0.0.10-26.3-preview.2` build uses `-Ptarget_minecraft=26.3` and `version_263`, producing `build/26.3/libs/water-optimisation-0.0.10-26.3-preview.2-mc26.3-fabric.jar`. It requires Java 25, Fabric Loader `0.19.5`, and Fabric API `0.160.6+26.3`; the API metadata floor is `>=0.160.6+26.3` for 26.3 only. Mod Menu `21.0.0-beta.1` is optional. The expected Fabric 26.3 suite is now 25 tests: 16 config-model tests, 4 context helper tests, and 5 mocked predicate tests. The newer historical local result passed all 25 via `test` only. Earlier evidence separately records a forced `test build verifyArtifact --rerun-tasks` pass with 20 tests, not a 25-test full build. A separate NeoForge 26.2 build passed 20 tests before the new mocked predicate fixture was added. Current-change CI is pending; transformed-Mixin cancellation/exception validation remains an open nonvisual gate, and startup success alone does not prove optional local-capture hook execution. No in-game tests were run for this hardening, and in-world visual and FPS validation remain outstanding. The [PREVIEW release notes](FABRIC_26_3_RELEASE_NOTES.md) describe the fixes and remaining limitations.

This preview is not published and does not extend the public 0.0.9 support claims or listing facts. Any publication must be a separate, explicitly labeled pre-release with its own artifacts, checksum, and validation caveats—not a replacement for 0.0.9. Follow the [26.3 preview build/test/release guide](FABRIC_26_3.md), including OIT on/off, hook observation and skips, Sodium particle-only behavior, both backends on supported hardware, and mod-off/on comparisons.

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

## Listing facts

- Name: Water Optimisation
- Creator attribution: Cinq
- Category: Client-side rendering optimisation
- Loader: Fabric for 26.2 and 1.21.1; NeoForge for 26.2
- Environment: Client
- Minecraft target: 26.2 and 1.21.1, with target-specific artifacts
- Java target: 25 for 26.2; 21 for 1.21.1
- License: MIT
- Required dependency: matching Fabric API build on Fabric; NeoForge 26.2.0.77+ on NeoForge

## Credential boundary

Never commit API keys or publishing tokens. Any future publishing workflow must be reviewed separately and use repository secrets.

## Multiplayer wording

Describe the project as a client-side rendering and cosmetic-particle optimisation. Do not advertise it as an anti-cheat bypass, competitive advantage, or server-approved modification.
