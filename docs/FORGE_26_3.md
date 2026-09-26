# Forge 26.3

Forge 26.3 ships once, in v1.1.0; no further Forge updates are planned. The
build is pinned to Forge `66.0.2` and Java 25; the artifact version is `1.1.0`.

The Forge module lives in `forge-26.3/`. It reuses the shared settings, policy,
configuration, and screens while providing Forge-native mod registration,
key/tick events, and the Mods-list configuration screen. Forge-specific Mixin
classes are kept in that module because its official-mapped runtime needs
different remapping declarations from Fabric and NeoForge. The diagnostics HUD
is attached after Forge's vanilla HUD extraction.

Build and verify locally with:

```sh
JAVA_HOME=/path/to/jdk-25 ./gradlew -p forge-26.3 clean test build verifyArtifact --no-daemon --console=plain
```

The current local build passed unit tests, packaging, and artifact metadata
verification. The generated JAR is
`forge-26.3/build/libs/water-optimisation-1.1.0-mc26.3-forge.jar`.

The first Prism client launch (Minecraft 26.3, Forge 66.0.2, Java 25, Mixin
0.8.7) stopped before Minecraft startup because Mixin 0.8.7 does not recognize
`JAVA_25` as a compatibility level. The Forge and NeoForge 26.3 Mixin configs
now request `JAVA_21`; the Forge JAR has been rebuilt and its packaged config
checked. A follow-up Prism log confirms Mixin accepted `JAVA_21` and Water
Optimisation reached its initialization message. A subsequent screenshot showed
the settings screen displaying untranslated keys. The Forge JAR had the English
language file but lacked root `pack.mcmeta`, so Forge did not discover the mod's
resource pack. Added 26.3 resource-pack metadata (`min_format` and `max_format`
`[97, 1]`) to both the Forge and NeoForge 26.3 modules, and added packaged
metadata assertions to both artifact verifiers. The corrected Forge artifact
has passed tests, build, and `verifyArtifact`; the NeoForge preview has not been
built. On 22 September 2026, the user confirmed the corrected Forge 26.3 JAR
was all good on their client after the earlier untranslated-settings issue.
The supplied runtime log identifies Forge 66.0.2, Java 25.0.1, macOS 27.0,
Apple M2, and OpenGL; individual visual scenes and performance measurements
were not reported.

The Forge client smoke has now been user-confirmed. Fine-grained visual-scene
coverage and performance measurements remain untested, so no FPS improvement
is claimed. The candidate is still unreleased until publication is separately
authorized and completed.
