# Distribution

## Current status

Water Optimisation 0.0.4 is the current public GitHub release with target-specific artifacts for Minecraft 26.2 and 1.21.1. The 1.21.1 artifact is a compatibility build pending live visual and FPS validation; earlier releases remain in the changelog and tags as historical records. Maximum FPS is still an opt-in visual trade-off inside an otherwise client-only release.

## Current build profile

| Field | Value |
| --- | --- |
| Mod version | 0.0.4 |
| Minecraft | 26.2 and 1.21.1, with target-specific 0.0.4 artifacts |
| Fabric Loader | 0.19.3+ for 26.2; 0.16.13+ for 1.21.1 |
| Fabric API | 0.158.0+26.2; 0.116.12+1.21.1 |
| Java | 25 for 26.2; 21 for 1.21.1 |
| Mod Menu | Optional, 19.0.0-alpha.1 for 26.2; 11.0.4 for 1.21.1 |
| Environment | Client |
| License | MIT |
| Creator attribution | Cinq |

## Release artifacts

Use the published [GitHub Release](https://github.com/imCinq/water-optimisation/releases/tag/v0.0.4) for the target-specific runtime JARs, matching sources JARs, and `SHA256SUMS-0.0.4.txt`. Choose the asset matching the Minecraft target, then verify the tag, accepted commit, and checksum before distributing the artifact. The former `v0.0.3` release remains available as historical 26.2-only distribution.

GitHub Actions artifacts remain temporary build outputs. They expire and are not a substitute for the tagged release.

## Stable release requirements

Before publishing a stable artifact:

- the exact Minecraft, Fabric Loader, Fabric API, Java, Mod Menu, and companion-mod versions are documented;
- the version in `gradle.properties`, generated `fabric.mod.json`, changelog, and release notes agrees;
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
- Loader: Fabric
- Environment: Client
- Minecraft target: 26.2 and 1.21.1, with target-specific artifacts
- Java target: 25 for 26.2; 21 for 1.21.1
- License: MIT
- Required dependency: matching Fabric API build

## Credential boundary

Never commit API keys or publishing tokens. Any future publishing workflow must be reviewed separately and use repository secrets.

## Multiplayer wording

Describe the project as a client-side rendering and cosmetic-particle optimisation. Do not advertise it as an anti-cheat bypass, competitive advantage, or server-approved modification.
