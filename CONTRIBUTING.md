# Contributing to PuppyClicker for Minecraft

Thank you for helping improve PuppyClicker for Minecraft. The project targets
Minecraft 1.21.1, NeoForge 21.1.243 or newer, and Java 21.

## Project principles

- Outgoing PuppyClicker actions must require deliberate player input.
- Do not connect damage, death, combat, or other automatic gameplay events to
  PuppyClicker actions.
- OpenShock, physical-device actions, and unrelated third-party integrations
  are outside the current project scope.
- Keep the PuppyClicker API key on the client. Never place it in server data,
  item components, source code, `gradle.properties`, tests, screenshots, or
  logs.
- Network work must remain asynchronous. Return to the Minecraft client thread
  before changing screens, HUD messages, or other client state.
- Preserve accessible status feedback and avoid narrating masked credentials.

## Development setup

Clone the repository and ensure a Java 21 JDK is available. On macOS or Linux,
make the Gradle wrapper executable before the first run:

```bash
chmod +x gradlew
```

Launch the NeoForge development client with:

```bash
./gradlew runClient
```

The development client stores its local API key in
`run/config/puppyclicker-client.toml`. The `run/` and `runs/` directories are
ignored by Git, but contributors must still check changes for credentials
before committing.

## Building and verification

Run the complete Gradle build before submitting a change:

```bash
./gradlew build
```

The release JAR is written to `build/libs/`. Verify behaviour in proportion to
the change. Network, configuration, item, or UI changes should also be checked
in the development client. Relevant manual checks include:

- The mod appears in the NeoForge mod list and its Config button opens.
- API-key input is masked and a valid key can be validated and saved.
- The self-click keybind appears under Controls and remains configurable.
- Click requests do not freeze or noticeably stall the client.
- Missing credentials, HTTP failures, rate limits, and network failures produce
  controlled feedback without exposing the API key.
- An unbound clicker opens the friend picker, a bound clicker sends to the
  selected friend, and sneak-right-click permits rebinding.
- The mod loads on a dedicated server without attempting a PuppyClicker API
  request.

Before committing, inspect the working tree and check for whitespace errors:

```bash
git status --short
git diff --check
```

## Code and documentation

Keep comments focused on security boundaries, threading requirements, unusual
NeoForge behaviour, or reasoning that is not obvious from the code itself. Add
specific `TODO` comments for intentionally deferred work, and avoid leaving
credentials or real friend identifiers in examples.

If behaviour changes, update the README, translations, and relevant metadata
in the same contribution. New player-facing text must use translation keys
rather than hard-coded strings.

## Publishing a GitHub release

This section is for project maintainers. Releases are built from tags by
`.github/workflows/release.yml`.

1. Set `mod_version` in `gradle.properties` to the version being released.
2. Run `./gradlew build` and complete the relevant manual checks.
3. Commit and push the version and release-readiness changes.
4. Create and push an annotated semantic-version tag:

   ```bash
   git tag -a v1.0.0 -m "PuppyClicker for Minecraft v1.0.0"
   git push origin v1.0.0
   ```

The workflow verifies that the tag and `mod_version` match, builds with Java 21,
and publishes the JAR plus a SHA-256 checksum with generated release notes.
Tags with a suffix such as `v1.1.0-beta.1` are published as prereleases and are
not marked as the latest stable release. Validation and the build must succeed
before the workflow attempts to create a release.

## Licence and assets

Contributions to the mod's source code and documentation are accepted under the
[MIT License](LICENSE). Do not contribute branding or other assets unless their
origin and reuse permission can be documented in
[ASSET_LICENSES.md](ASSET_LICENSES.md).
