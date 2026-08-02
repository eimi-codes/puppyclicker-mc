# PuppyClicker for Minecraft

An early client-side NeoForge mod for Minecraft 1.21.1. Press a configurable
key to send a self-click through the [PuppyClicker API](https://puppyclicker.app/docs/api/v2/)
without blocking Minecraft's render thread.

## Prototype usage

1. Launch the development client once:

   ```bash
   ./gradlew runClient
   ```

2. Open **Mods → PuppyClicker for Minecraft → Config**, select
   **PuppyClicker Client Settings**, and enter your API key. NeoForge saves it
   to `run/config/puppyclicker-client.toml`:

   ```toml
   apiKey = "pak_your_key_here"
   ```

3. Launch the client again and enter a world.
4. Press `P` to send a self-click. The binding can be changed under
   **Options → Controls → Key Binds → PuppyClicker**.

The API key is a client-only credential. The generated `run/` and `runs/`
directories are ignored by Git; do not move the key into source code,
`gradle.properties`, logs, or any server configuration.

## Development

The project targets Java 21, Minecraft 1.21.1, and NeoForge 21.1.243.

```bash
./gradlew build
./gradlew runClient
```

The first milestone intentionally contains no custom items, automatic gameplay
triggers, server networking, SSE connection, or multi-loader support.
