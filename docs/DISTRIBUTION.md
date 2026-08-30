# Distribution

## Current status

Water Optimisation is an experimental Minecraft 26.2 preview. No stable release claim is made until the exact artifact has passed visual, performance, backend, companion-mod, and multiplayer compatibility checks.

## Planned distribution

1. GitHub Releases as the canonical source for tagged artifacts and checksums.
2. Modrinth for discovery and launcher installation after the release candidate is validated.
3. Additional platforms only when they add clear value and their project permissions are explicitly approved.

## Release requirements

Before publishing a stable artifact:

- the exact Minecraft, Fabric Loader, Fabric API, Java, and companion-mod versions are documented;
- the JAR is built from a tagged commit;
- SHA-256 checksums are published;
- privacy and client-only audits pass;
- visual trade-offs and known limitations are documented;
- no personal information, credentials, private server data, or generated runtime files are included;
- the listing makes no universal FPS or server-approval claim;
- the exact artifact is tested in a clean client.

## Credential boundary

Publishing automation is not enabled. Never commit API keys or publishing tokens. Any future publishing workflow must be reviewed separately and use repository secrets.

## Listing facts

- Name: Water Optimisation
- Brand: Cinq
- Category: Client-side rendering optimisation
- Loader: Fabric
- Environment: Client
- Minecraft target: 26.2
- Java target: 25
- License: MIT
- Required dependency: matching Fabric API build

## Multiplayer wording

Describe the project as a client-side rendering and cosmetic-particle optimisation. Do not advertise it as an anti-cheat bypass, competitive advantage, or server-approved modification.
