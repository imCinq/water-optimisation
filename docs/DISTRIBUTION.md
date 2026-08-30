# Distribution

## Current status

Water Optimisation 0.1.0-preview.3 is an experimental Minecraft 26.2 preview. The repository is prepared for a public release candidate, but no stable artifact has been published. Build artifacts from GitHub Actions are temporary verification outputs.

## Current build profile

| Field | Value |
| --- | --- |
| Mod version | 0.1.0-preview.3 |
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3 or newer |
| Fabric API | 0.158.0+26.2 |
| Java | 25 |
| Mod Menu | Optional, 19.0.0-alpha.1 |
| Environment | Client |
| License | MIT |
| Creator attribution | Cinq |

## Preview artifacts

For temporary preview testing, use the latest successful [GitHub Actions build](https://github.com/imCinq/water-optimisation/actions/workflows/build.yml). Verify that the artifact is from the reviewed commit, extract the runtime JAR, and pair it with the matching Fabric API build.

Workflow artifacts expire and are not the canonical distribution format. Do not describe an Actions artifact as a stable release or rely on it as a permanent download.

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

## Publishing sequence

1. Complete and review the release checklist.
2. Create the version tag from the accepted commit.
3. Rebuild and test the exact tagged commit.
4. Publish a GitHub Release with the runtime JAR, optional sources JAR, SHA-256 checksum, changelog, compatibility notes, and known limitations.
5. Mark the artifact as a pre-release until the documented validation supports a stable release.
6. Publish to Modrinth only after the release package and project permissions are reviewed.

Publishing is intentionally manual. No publishing automation or update checker is enabled.

## Listing facts

- Name: Water Optimisation
- Creator attribution: Cinq
- Category: Client-side rendering optimisation
- Loader: Fabric
- Environment: Client
- Minecraft target: 26.2
- Java target: 25
- License: MIT
- Required dependency: matching Fabric API build

## Credential boundary

Never commit API keys or publishing tokens. Any future publishing workflow must be reviewed separately and use repository secrets.

## Multiplayer wording

Describe the project as a client-side rendering and cosmetic-particle optimisation. Do not advertise it as an anti-cheat bypass, competitive advantage, or server-approved modification.
