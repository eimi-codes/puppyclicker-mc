# PuppyClicker for Minecraft

Connect Minecraft to your [PuppyClicker](https://puppyclicker.app/) account.
Send a self-click from a configurable keybind, craft clicker items for accepted
friends, or opt into carefully rate-limited actions for advancements and
damage.

[![A Puppy Clicker held in Minecraft with a friend-click confirmation above the hotbar](https://raw.githubusercontent.com/eimi-codes/puppyclicker-mc/main/img/SCR-PCMC-ClickConf.png)](https://github.com/eimi-codes/puppyclicker-mc/blob/main/img/SCR-PCMC-ClickConf.png)

## Features

- Press `P` by default to send a self-click.
- Configure and validate a masked PuppyClicker API key in-game.
- Craft multiple Puppy Clickers and bind each stack to a different accepted
  friend.
- Right-click a bound clicker to send its friend a click; sneak-right-click to
  rebind it.
- Optionally send a self-click after earning a visible advancement.
- Optionally send a self-targeted OSC shock after taking damage.
- Configure a 15–300 second damage-action cooldown; the default is 30 seconds.
- Receive clear hotbar feedback for success, Do Not Disturb suppression,
  credentials, rate limits, HTTP failures, and network failures.

Automated actions are **off by default** and each category must be enabled
separately. Damage actions target only the authenticated player's own
PuppyClicker devices. The mod does not contact OpenShock directly and does not
choose device intensity or duration.

## Supported versions

Choose the JAR whose filename contains both your loader and exact Minecraft
version.

| Minecraft | NeoForge | Forge | Fabric |
| --- | --- | --- | --- |
| 1.18.2 | — | Yes | Yes |
| 1.19.2 | — | Yes | Yes |
| 1.20.1 | — | Yes | Yes |
| 1.21.1 | Yes | Yes | Yes |
| 1.21.11 | Yes | Yes | Yes |
| 26.1.2 | Yes | Yes | Yes |

The 1.18.2 and 1.19.2 Forge builds are intended for the generation of popular
older packs that includes ATM7, ATM8, FTB StoneBlock 3, and FTB One. Fabric
installations also require Fabric API.

The JARs are deliberately loader- and version-specific because Minecraft and
the three loaders use incompatible binary APIs and startup metadata. The
shared PuppyClicker core is already included in every JAR; there is no separate
library mod to install.

## Installation and use

1. Install the matching JAR on both the Minecraft client and server.
2. On NeoForge or Forge, open **Mods → PuppyClicker for Minecraft → Config**.
   On Fabric, press `O`; the binding can be changed under Controls.
3. Enter the personal PuppyClicker API key and choose **Validate & Save**.
4. Press `P` for a self-click, or enable either automation category from
   **Automated Actions…**.
5. Craft a clicker from a stone button, iron nugget, and redstone in any
   arrangement.

| Clicker interaction | Result |
| --- | --- |
| Right-click an unbound clicker | Open the accepted-friend picker |
| Right-click a bound clicker | Send a click to its selected friend |
| Sneak-right-click a bound clicker | Choose a different friend |

[![PuppyClicker settings with a masked API key](https://raw.githubusercontent.com/eimi-codes/puppyclicker-mc/main/img/api-config.png)](https://github.com/eimi-codes/puppyclicker-mc/blob/main/img/api-config.png)

[![A bound Puppy Clicker in the Minecraft inventory with its friend and consent tooltip](https://raw.githubusercontent.com/eimi-codes/puppyclicker-mc/main/img/SCR-PCMC-InventoryView.png)](https://github.com/eimi-codes/puppyclicker-mc/blob/main/img/SCR-PCMC-InventoryView.png)

## Privacy and safety

The mod connects only to `puppyclicker-api.boundfire.com`. The API key remains
on the client and is never sent to the Minecraft server, stored in an item, or
intentionally logged. The local client config is plain text, so never share or
upload `puppyclicker-client.toml` or `puppyclicker-client.json`.

Advancement automation uses PuppyClicker's self-click endpoint. Damage
automation sends only `type: osc` to PuppyClicker's self-action endpoint—no
friend identifier, message, or integration override. PuppyClicker, Do Not
Disturb, and the player's device configuration remain responsible for delivery
and physical safety limits.

## Help and links

- [Setup help and issue forms](https://github.com/eimi-codes/puppyclicker-mc/issues/new/choose)
- [Full documentation and source](https://github.com/eimi-codes/puppyclicker-mc)
- [Release history](https://github.com/eimi-codes/puppyclicker-mc/blob/main/CHANGELOG.md)
- [PuppyClicker](https://puppyclicker.app/)
- [Author — Éimí Mhic an Ridire](https://eim.ie/)

Source code and documentation are available under the MIT License. PuppyClicker
branding and artwork have separate terms described in the repository's asset
notices.
