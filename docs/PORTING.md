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
| `common/` | Loader-neutral code, translations, item models, and textures |
| `platforms/<loader>/common/` | Metadata, mixins, and resources specific to one loader |
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
4. Keep genuinely portable assets under `common/src/main/resources` and put
   loader-specific metadata under the loader's `common/` resources. Put
   version-specific recipes, item definitions, or mixins in the version module.
5. Add the module to `settings.gradle`, the root build, build workflow, release
   assets, and public compatibility table.
6. Run the full build, inspect the produced JAR, start a dedicated server, and
   manually verify the client configuration, keybind, clicker item, and enabled
   automation categories.

## Minecraft 26.3 development gate

Minecraft 26.3 development targets Fabric and NeoForge only. Work-in-progress
adapters may be built and tested before both ecosystems are ready, but 26.3
must not be added to the publishing matrix or advertised as supported until
both Fabric and NeoForge have stable, non-beta releases.

The [26.3 release notes](https://feedback.minecraft.net/hc/en-us/articles/48913133328013-Minecraft-Java-Edition-26-3)
set Data Pack version 121.0 and Resource Pack version 97.1. The port must check
the clicker recipe, modern item definition, model, texture, language files, and
stored binding data against those formats.

Minecraft 26.3 also replaces GLFW with SDL and changes key bindings to physical
keys. The 26.3 adapters must not copy the current direct `GLFW_KEY_*` imports.
Use the version's Minecraft input APIs, then manually verify the default click
and settings bindings on more than one keyboard layout. The masked API-key box
also needs focused testing for text input, paste, IME/accent input, focus loss,
and narrator output under the new backend. Fabric's
[26.3 migration notes](https://fabricmc.net/2026/09/15/263.html) provide the
loader-specific SDL and text-input guidance.

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
