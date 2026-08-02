# PuppyClicker for Minecraft

An early NeoForge mod for Minecraft 1.21.1. Press a configurable key to send a
self-click, or bind craftable clicker items to accepted PuppyClicker friends.
All PuppyClicker API requests are asynchronous and run only on the holder's
client.

## Prototype usage

1. Launch the development client once:

   ```bash
   ./gradlew runClient
   ```

2. Open **Mods → PuppyClicker for Minecraft → Config**, enter your API key in
   the masked field, then choose **Validate & Save**. The mod validates it with
   `GET /api/v2/me` before saving it to
   `run/config/puppyclicker-client.toml`:

   ```toml
   apiKey = "pak_your_key_here"
   ```

3. Launch the client again and enter a world on a server that also has the mod.
4. Press `P` to send a self-click. The binding can be changed under
   **Options → Controls → Key Binds → PuppyClicker**.
5. Craft a Puppy Clicker from a stone button, iron nugget, and redstone.
6. Right-click an unbound clicker to choose an accepted friend. Right-click a
   bound clicker to send that friend a click; sneak-right-click to rebind it.

The API key is a client-only credential. The generated `run/` and `runs/`
directories are ignored by Git; do not move the key into source code,
`gradle.properties`, logs, or any server configuration. A bound friend's name
and PuppyClicker ID are stored on the clicker's item data and are therefore
visible to the Minecraft server.

## Development

The project targets Java 21, Minecraft 1.21.1, and NeoForge 21.1.243.
The packaged mod-list icon is the PuppyClicker artwork supplied from
[puppyclicker.app](https://puppyclicker.app/).

```bash
./gradlew build
./gradlew runClient
```

The mod must be installed on both the client and server because the clicker is
a registered item with per-stack binding data. The server never receives the
PuppyClicker API key and never contacts PuppyClicker. No automatic gameplay
event sends a click; every click requires an explicit keypress or item use.

## TODO

- Connect to `GET /stream` with bearer-header SSE authentication.
- Add accessible incoming-click HUD notifications, sounds, and optional paw
  particles.
- Add a recent-click history screen.
- Replace the temporary tripwire-hook item model with dedicated clicker art.
- Add `en_gb` and `ga_ie` translations.
- Consider Fabric or multi-loader support only after the NeoForge release is
  stable.
