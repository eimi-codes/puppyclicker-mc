# PuppyClicker for Minecraft

PuppyClicker for Minecraft is a NeoForge, Forge, and Fabric mod that connects
Minecraft to a
[PuppyClicker](https://puppyclicker.app/) account. Send a self-click with
a configurable keybind, or craft clicker items and bind each one to a different
accepted PuppyClicker friend or "pet". This makes it possible to carry several
individually named clickers and choose who receives each click directly from
the hotbar.

Manual actions require deliberate player input. Automated action categories are
disabled by default and must be enabled individually in the client config.

![A Puppy Clicker held in Minecraft with friend-click confirmation above the hotbar](img/SCR-PCMC-ClickConf.png)

## Contents

- [Screenshots and quick tour](#at-a-glance)
- [Features](#features)
- [Requirements](#requirements)
- [Setup and use](#setup-and-use)
- [Privacy and informed consent](#privacy-and-informed-consent)
- [Current scope and planned work](#current-scope-and-planned-work)
- [Help and feedback](#help-and-feedback)
- [Contributing](#contributing)
- [Author and project](#author-and-project)
- [License](#license)
- [Links](#links)

Repository documents:

- [Support guide](SUPPORT.md)
- [Contributor guide](CONTRIBUTING.md)
- [Release history](CHANGELOG.md)
- [Modrinth description](MODRINTH.md)
- [CurseForge description](CURSEFORGE.md)
- [Security policy](SECURITY.md)
- [Code of Conduct](CODE_OF_CONDUCT.md)
- [Asset and third-party notices](ASSET_LICENSES.md)

## At a glance

### Configure and bind

| Masked API-key configuration | Accepted-friend picker |
| --- | --- |
| ![PuppyClicker settings with a masked stored API key and validation controls](img/api-config.png) | ![The corrected friend picker with clear title, status, binding notice, and friend buttons](img/SCR-PCMC-FriendSelect.png) |

### Send and confirm

| Bound clicker details | PuppyClicker activity confirmation |
| --- | --- |
| ![A bound Puppy Clicker in the Minecraft inventory with its friend, server-sync, and consent tooltip](img/SCR-PCMC-InventoryView.png) | ![The resulting click from Minecraft in PuppyClicker's activity log](img/SCR-receivedlog.png) |

PuppyClicker can also display its own notification after receiving the outgoing
Minecraft click:

![A PuppyClicker notification for a click sent from Minecraft](img/SCR-receivednotification.png)

## Features

- Send a self-click using a configurable keybind, set to `P` by default.
- Configure and validate a PuppyClicker API key through
  **Mods → PuppyClicker for Minecraft → Config**.
- Retrieve the account's accepted PuppyClicker friends.
- Craft multiple Puppy Clicker items and bind each stack to a different friend.
- Send clicks without blocking Minecraft's render thread.
- Optionally send a self-click to the player's own devices after earning a
  visible advancement.
- Optionally send an OSC shock action to the player's own configured
  PuppyClicker devices after taking damage, with a 15–300 second repeat
  cooldown.
- Display concise success, rate-limit, credential, HTTP, and network feedback
  above the hotbar.
- Prevent overlapping requests, apply a one-second local cooldown, and honour
  the API's `Retry-After` response after a `429`.
- Report when PuppyClicker's Do Not Disturb setting recorded an action but
  suppressed delivery to the player's devices.

## Requirements

Choose the JAR whose filename contains both your loader and exact Minecraft
version. Fabric installations also need Fabric API.

| Minecraft | Java | NeoForge | Forge | Fabric Loader / Fabric API |
| --- | --- | --- | --- | --- |
| 1.18.2 | 17 | — | 40.3.0–40.x | 0.19.3+ / 0.77.0+1.18.2 |
| 1.19.2 | 17 | — | 43.5.0–43.x | 0.19.3+ / 0.77.0+1.19.2 |
| 1.20.1 | 17 | — | 47.4.10–47.x | 0.19.3+ / 0.92.11+1.20.1 |
| 1.21.1 | 21 | 21.1.243–21.1.x | 52.1.0–52.x | 0.19.3+ / 0.116.15+1.21.1 |
| 1.21.11 | 21 | 21.11.45–21.11.x | 61.1.13–61.x | 0.19.3+ / 0.141.6+1.21.11 |
| 26.1.2 | 25 | 26.1.2.95–before 26.2 | 64.0.11–64.x | 0.19.3+ / 0.155.2+26.1.2 |

The 1.18.2 and 1.19.2 builds specifically cover the Java 17 generation used by
popular older packs such as ATM7, ATM8, FTB StoneBlock 3, and FTB One. Packs
must still use one of the loaders in the table; a Forge JAR cannot load on
Fabric or NeoForge.

All versions also require:

- A PuppyClicker account and personal API key beginning with `pak_`
- The same matching mod JAR installed on both the Minecraft client and server

The JARs are deliberately loader- and version-specific. For example, the Forge
1.20.1 JAR will not load on Fabric 1.20.1 or Forge 1.21.1, and changing a JAR's
metadata does not make it portable.

Minecraft 1.16.5 and 1.12.2 are not supported yet. Those pack generations use
Java 8, while the current loader-neutral HTTP client intentionally requires
Java 17. Supporting them honestly requires a separate Java 8 core and HTTP
transport rather than a metadata-only backport.

The server installation is required for the custom clicker item and its
synchronised per-stack binding data. The Minecraft server does not receive the
PuppyClicker API key and does not connect to the PuppyClicker API.

## Setup and use

1. Place the mod JAR in the `mods` directory for both the client and server.
2. On NeoForge or Forge, open **Mods**, select **PuppyClicker for Minecraft**,
   and choose **Config**. On Fabric, press `O` to open PuppyClicker settings;
   this binding can be changed under Controls.
3. Enter the API key in the masked field and choose **Validate & Save**. The mod
   verifies the key through `GET /api/v2/me` before saving it.
4. To opt into gameplay-triggered actions, choose **Automated Actions…** and
   enable **Clicks on advancements**, **Shocks on damage**, or both. Each
   category is independent and disabled by default.
5. Press `P` to send a self-click. Change this binding under
   **Options → Controls → Key Binds → PuppyClicker** if needed.
6. Craft a Puppy Clicker with a stone button, an iron nugget, and redstone in
   any arrangement.

| Clicker interaction | Result |
| --- | --- |
| Right-click an unbound clicker | Open the accepted-friend picker |
| Right-click a bound clicker | Send a click to its selected friend |
| Sneak and right-click a bound clicker | Choose a different friend |

NeoForge and Forge store configuration in
`config/puppyclicker-client.toml`; Fabric uses
`config/puppyclicker-client.json`. The same screen controls a separate 15–300
second cooldown for damage-triggered actions; 30 seconds is the default.

## Privacy and informed consent

The mod makes HTTPS requests to `puppyclicker-api.boundfire.com` to validate
the API key, retrieve accepted friends, and send self-clicks or direct friend
clicks. The API key is sent only to the PuppyClicker API. It remains on the
Minecraft client and is not sent to the Minecraft server, stored in clicker
items, embedded in the mod, or intentionally written to logs.

The key is masked in Minecraft and omitted from screen-reader narration, but
the local configuration file is plain text on the player's computer. It
must be treated as a credential and should never be shared, uploaded, or added
to source control. The generated `run/` and `runs/` directories are ignored by
Git.

A bound clicker stores the selected friend's public PuppyClicker identifier and
display name. Minecraft synchronises item data, so that binding information is
visible to the Minecraft server.

Advancement automation sends a self-click through
`POST /api/v2/clicks/self`. Damage automation sends only an OSC action type to
`POST /api/v2/puppies/self/actions`; it does not include a friend identifier,
message, or integration override. Each request is made only when its separate
client-side setting is enabled. The mod does not contact OpenShock directly or
choose shock intensity/duration. PuppyClicker and the player's own device
configuration remain responsible for delivery, Do Not Disturb, and physical
safety settings. A hard 15-second minimum damage cooldown prevents rapid repeat
requests.

## Current scope and planned work

The 2.0.0 feature set adds opt-in outgoing automation and separate NeoForge,
Forge, and Fabric artifacts while keeping every automation category off by
default. It does not yet include incoming-click streaming, recent-click
history, or a finished custom clicker texture.

- Connect to `GET /stream` with bearer-header SSE authentication.
- Add accessible incoming-click HUD notifications, sounds, and optional paw
  particles.
- Add a recent-click history screen.
- Replace the temporary tripwire-hook item model with dedicated clicker art.
- Add `en_gb` and `ga_ie` translations.

## Help and feedback

You do not need to be a developer to report a problem or suggest an idea. The
[guided issue forms](https://github.com/eimi-codes/puppyclicker-mc/issues/new/choose)
explain what information is useful and include a form specifically for setup
help. Please read the [support guide](SUPPORT.md) before sharing logs: never
post an API key, access token, or PuppyClicker client configuration file.

Security concerns and other matters that should not be public can be sent to
[minecraftclicker@eim.ie](mailto:minecraftclicker@eim.ie). See the
[security policy](SECURITY.md) for reporting guidance.

## Contributing

Development setup, testing, credential-safety requirements, and the release
process are documented in [CONTRIBUTING.md](CONTRIBUTING.md). Participation is
covered by the project [Code of Conduct](CODE_OF_CONDUCT.md).

## Author and project

PuppyClicker for Minecraft is created and maintained by
[Éimí Mhic an Ridire](https://eim.ie/). It is an independent Minecraft client
for [PuppyClicker](https://puppyclicker.app/); PuppyClicker supplies the service,
API, name, and branded icon used by the mod.

## License

The mod's source code and documentation are available under the
[MIT License](LICENSE). PuppyClicker artwork and branding are excluded from
that software licence; see [the asset and third-party notices](ASSET_LICENSES.md)
for details. The original NeoForge MDK template notice is retained in
[`TEMPLATE_LICENSE.txt`](TEMPLATE_LICENSE.txt). The adapted
[Code of Conduct](CODE_OF_CONDUCT.md#attribution) carries its stated
CC BY-SA 4.0 terms.

## Links

- [Author — Éimí Mhic an Ridire](https://eim.ie/)
- [PuppyClicker](https://puppyclicker.app/)
- [PuppyClicker API documentation](https://puppyclicker.app/docs/api/v2/)
- [Modrinth project](https://modrinth.com/project/puppyclicker/)
- [GitHub repository](https://github.com/eimi-codes/puppyclicker-mc)
- [GitHub releases](https://github.com/eimi-codes/puppyclicker-mc/releases/)
