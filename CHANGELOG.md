# Changelog

All notable changes to PuppyClicker for Minecraft are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and the project uses [Semantic Versioning](https://semver.org/).

## [2.0.0] - 2026-08-12

### Added

- Optional self-clicks after earning visible, toast-worthy Minecraft
  advancements.
- Optional self-targeted OSC shocks after the player takes damage.
- A configurable 15–300 second damage-action cooldown, defaulting to 30
  seconds, with a hard 15-second safety minimum.
- Native NeoForge builds for Minecraft 1.21.1, 1.21.11, and 26.1.2.
- Native Forge and Fabric builds for Minecraft 1.18.2, 1.19.2, 1.20.1,
  1.21.1, 1.21.11, and 26.1.2.
- Fabric configuration and self-click keybindings, using `O` and `P` by
  default.
- A full loader/version CI matrix and tag-driven release packaging for all 15
  supported JARs and their SHA-256 checksums.
- Friendly bug, setup-help, and feature-request forms, plus expanded support,
  security, contribution, and version-porting guidance.

### Changed

- Reorganised the project around a loader-neutral shared core with dedicated
  NeoForge, Forge, and Fabric adapters for each supported Minecraft version.
- Release files now include both the loader and exact Minecraft version in
  their names so launchers and modpack users can select the correct artifact.
- Advancement automation ignores recipe unlocks and other non-toast background
  advancements.
- Damage automation now uses PuppyClicker's self-action API. It sends only the
  OSC action type and never targets a friend or supplies a message or
  integration override.
- Updated the Gradle wrapper, build plugins, metadata, recipes, item storage,
  networking, and event hooks for the supported Minecraft generations.

### Security

- Every automated-action category is disabled by default and must be enabled
  separately in the client configuration.
- PuppyClicker API keys remain client-only, masked in the settings screen,
  omitted from narration, and excluded from server packets and item data.
- Automated requests use cooldown and in-flight guards to prevent burst queues;
  rate-limit responses continue to honour `Retry-After`.

## [1.0.0] - 2026-08-02

### Added

- Initial public release for Minecraft 1.21.1 on NeoForge.
- Configurable self-click keybinding and masked PuppyClicker API-key settings.
- Craftable clicker items that can be bound to accepted PuppyClicker friends.
- Asynchronous API requests, controlled error feedback, rate-limit handling,
  and dedicated-server-safe client separation.

[2.0.0]: https://github.com/eimi-codes/puppyclicker-mc/compare/v1.0.0...v2.0.0
[1.0.0]: https://github.com/eimi-codes/puppyclicker-mc/releases/tag/v1.0.0
