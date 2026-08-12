# PuppyClicker for Minecraft

PuppyClicker for Minecraft connects the game to a
[PuppyClicker](https://puppyclicker.app/) account. Send a self-click with a
keybind, carry individually bound clickers for accepted friends, and optionally
connect visible advancements or player damage to carefully guarded automated
actions.

[![A Puppy Clicker held in Minecraft with confirmation above the hotbar](https://raw.githubusercontent.com/eimi-codes/puppyclicker-mc/main/img/SCR-PCMC-ClickConf.png)](https://github.com/eimi-codes/puppyclicker-mc/blob/main/img/SCR-PCMC-ClickConf.png)

## What it adds

- A configurable self-click keybind (`P` by default).
- A masked in-game API-key screen with validation.
- Craftable clicker items that can each be bound to a different accepted
  PuppyClicker friend.
- Optional self-clicks for visible advancements.
- Optional **self-targeted** OSC shocks after the player takes damage.
- A separate 15–300 second damage cooldown, defaulting to 30 seconds.
- Friendly hotbar feedback for delivery, Do Not Disturb, rate limits, and
  errors.

Both automated categories are disabled by default and must be enabled
individually. Damage never targets a friend. The mod sends the self OSC action
to PuppyClicker, which applies the player's existing integration, Do Not
Disturb, device, intensity, duration, and safety settings.

## Compatibility

| Minecraft | NeoForge | Forge | Fabric |
| --- | --- | --- | --- |
| 1.18.2 | — | Supported | Supported |
| 1.19.2 | — | Supported | Supported |
| 1.20.1 | — | Supported | Supported |
| 1.21.1 | Supported | Supported | Supported |
| 1.21.11 | Supported | Supported | Supported |
| 26.1.2 | Supported | Supported | Supported |

Use the JAR labelled with your exact Minecraft version and loader. Fabric also
requires Fabric API. The mod must be installed on both client and server.

The 1.18.2 and 1.19.2 Forge builds cover the era used by packs such as ATM7,
ATM8, FTB StoneBlock 3, and FTB One. Minecraft 1.16.5 and 1.12.2 are not yet
supported because they require a separate Java 8-compatible core.

There is no extra common-library download. Shared code is already packaged
inside each loader- and version-specific JAR.

## Setup

1. Put the matching JAR in the client and server `mods` folders.
2. Open the mod's Config button on NeoForge/Forge, or press `O` on Fabric.
3. Enter the personal PuppyClicker API key and select **Validate & Save**.
4. Use `P` for a self-click or open **Automated Actions…** to opt into either
   automation category.
5. Craft a clicker with a stone button, iron nugget, and redstone in any
   arrangement.

Right-click an unbound clicker to select a friend, right-click a bound one to
send its click, or sneak-right-click to change its binding.

[![PuppyClicker settings with a masked stored API key](https://raw.githubusercontent.com/eimi-codes/puppyclicker-mc/main/img/api-config.png)](https://github.com/eimi-codes/puppyclicker-mc/blob/main/img/api-config.png)

[![The accepted-friend picker in Minecraft](https://raw.githubusercontent.com/eimi-codes/puppyclicker-mc/main/img/SCR-PCMC-FriendSelect.png)](https://github.com/eimi-codes/puppyclicker-mc/blob/main/img/SCR-PCMC-FriendSelect.png)

## Privacy

PuppyClicker for Minecraft makes HTTPS requests to
`puppyclicker-api.boundfire.com`. The API key stays on the Minecraft client; it
is not sent to the server, placed in item data, or intentionally logged. Never
share the client config file because it stores the key locally in plain text.

The mod does not contact OpenShock directly or choose shock intensity or
duration. Damage requests contain only the OSC action type and no friend
identifier, message, or integration override.

## Support and project links

- [Setup help, bug reports, and feature ideas](https://github.com/eimi-codes/puppyclicker-mc/issues/new/choose)
- [Source and full documentation](https://github.com/eimi-codes/puppyclicker-mc)
- [Changelog](https://github.com/eimi-codes/puppyclicker-mc/blob/main/CHANGELOG.md)
- [PuppyClicker](https://puppyclicker.app/)
- [Éimí Mhic an Ridire](https://eim.ie/)

The source and documentation use the MIT License. PuppyClicker artwork and
branding are covered separately in the repository's asset notices.
