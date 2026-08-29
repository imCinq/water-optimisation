# Distribution plan

## Current status

The repository is private and intended for development, profiling, and code review only. No public release or platform project should be created yet.

## Possible future order

1. GitHub Releases as the canonical source and checksum archive.
2. Modrinth for Minecraft discovery and launcher installation.
3. CurseForge only if there is a clear need for additional distribution.

The exact order may change after the mod has a verified implementation.

## Release requirements

Before distribution:

- the exact Minecraft target and Fabric versions are tested;
- the JAR is built from a tagged commit;
- checksums are published;
- privacy and client-only audits pass;
- visual trade-offs are documented;
- no personal information or private server data is included;
- the project description does not promise universal FPS gains;
- the listing does not claim DonutSMP approval without current official confirmation.

## Account and credential boundary

Do not automate platform publishing until the required account, project, and permissions are explicitly approved. Never commit API keys, publishing tokens, or credentials. Use repository secrets only in a future reviewed workflow.

## Listing facts

- Name: Water Optimisation
- Category: Optimization
- Loader: Fabric
- Environment: Client
- Initial Minecraft target: 26.2
- Java target: 25
- License: MIT
- Required dependency: the matching Fabric API build
- Source: this repository, if and when it becomes public

## Multiplayer wording

The mod should be described accurately as a client-side rendering and cosmetic-particle optimisation. It must not be advertised as an anti-cheat bypass, competitive advantage, or server-approved modification.
