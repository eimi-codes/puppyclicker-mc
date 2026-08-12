# Version and loader porting

PuppyClicker publishes a separate artifact for each supported Minecraft and
loader combination. A wider metadata range is not a substitute for compiling
and testing against that version's APIs.

The split is a runtime requirement, not duplicated product logic. Each loader
reads different metadata and entry points before PuppyClicker code can choose
an adapter, while Minecraft changes class and method signatures between
versions. A universal JAR therefore cannot safely defer that choice. The
loader-neutral code is shared at build time and packaged into every small,
self-contained target JAR.

## Supported matrix

| Minecraft | NeoForge | Forge | Fabric | Java |
| --- | --- | --- | --- | --- |
| 1.18.2 | — | yes | yes | 17 |
| 1.19.2 | — | yes | yes | 17 |
| 1.20.1 | — | yes | yes | 17 |
| 1.21.1 | yes | yes | yes | 21 |
| 1.21.11 | yes | yes | yes | 21 |
| 26.1.2 | yes | yes | yes | 25 |

The 1.18.2 and 1.19.2 anchors cover the older Java 17 modpack generation.
Minecraft 1.16.5 and 1.12.2 remain a separate future tier: their Java 8
runtime cannot use the current `java.net.http` loader-neutral client, so they
need a Java 8 core and transport rather than metadata changes.

## Repository layout

| Path | Responsibility |
| --- | --- |
| `common/` | Loader-neutral PuppyClicker HTTP client and safety logic |
| `platforms/<loader>/common/` | Resources and metadata shared by one loader |
| `platforms/<loader>/<version>/` | Minecraft-version and loader adapter |
| `gradle/neoforge-platform.gradle` | Shared NeoForge build and packaging convention |
| `gradle/forge-legacy-platform.gradle` | Forge 1.18.2–1.20.1 build convention |
| `gradle/forge-modern-platform.gradle` | Forge 1.21.1 and newer build convention |
| `gradle/fabric-platform.gradle` | Shared Fabric build and packaging convention |

Every user-facing JAR packages the compiled `common` classes. Players never
need a separate common-library mod.

Fabric splits client-only classes into `src/client/java`; Forge and NeoForge
use loader-specific physical-client entry points. Keep credentials, config
screens, HTTP-triggering services, and client packet handlers out of dedicated
server class loading in every adapter.

## Adding a Minecraft version

1. Choose an ecosystem version worth maintaining rather than every short-lived
   Minecraft release.
2. Add a version module and pin its exact Minecraft, loader, API, and Java
   versions.
3. Start from the nearest adapter, then migrate identifiers, networking,
   screens, item interaction, tooltips, data components or NBT, and gameplay
   events as required by that Minecraft version.
4. Keep genuinely portable assets under the loader's `common/` resources and
   put version-specific recipes, item definitions, or mixins in the module.
5. Add the module to `settings.gradle`, the root build, build workflow, release
   assets, and public compatibility table.
6. Run the full build, inspect the produced JAR, start a dedicated server, and
   manually verify the client configuration, keybind, clicker item, and enabled
   automation categories.

## Compatibility rules

- Artifact names include loader and exact Minecraft version.
- Metadata declares an exact Minecraft version and a bounded loader line.
- Fabric artifacts declare Fabric API as a dependency.
- The API key remains client-only in every adapter.
- Dedicated servers must load without client classes or PuppyClicker API calls.
- Advancements and damage are relayed from authoritative gameplay events, but
  client configuration decides whether any PuppyClicker request is made.
- Damage actions target the player's own devices and retain the minimum repeat
  cooldown.
- Modern item data components and older NBT storage must preserve the same
  public friend ID/name boundary and must never contain credentials.
