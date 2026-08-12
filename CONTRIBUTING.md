# Contributing to PuppyClicker for Minecraft

Thank you for helping improve PuppyClicker for Minecraft. The project builds
separate NeoForge, Forge, and Fabric artifacts from Minecraft 1.18.2 through
26.1.2. The supported matrix uses Java 17, 21, and 25.

Technical experience is not required to help. Bug reports, setup feedback,
accessibility observations, documentation fixes, and focused feature ideas are
all useful contributions.

## Contents

- [Project principles](#project-principles)
- [Before contributing](#before-contributing)
- [Development setup](#development-setup)
- [Building and verification](#building-and-verification)
- [Code and documentation](#code-and-documentation)
- [Publishing a GitHub release](#publishing-a-github-release)
- [Licence and assets](#licence-and-assets)

## Project principles

- Manual PuppyClicker actions must require deliberate player input.
- Automated action categories must remain separately opt-in, default off, and
  use an appropriate cooldown or equivalent repeat guard.
- Damage automation targets only the player's own PuppyClicker devices. The mod
  does not contact OpenShock directly or select device intensity or duration.
- Keep the PuppyClicker API key on the client. Never place it in server data,
  item components, source code, `gradle.properties`, tests, screenshots, or
  logs.
- Network work must remain asynchronous. Return to the Minecraft client thread
  before changing screens, HUD messages, or other client state.
- Preserve accessible status feedback and avoid narrating masked credentials.

## Before contributing

- Use the [guided issue forms](https://github.com/eimi-codes/puppyclicker-mc/issues/new/choose)
  for bugs, setup help, and feature ideas.
- Read [SUPPORT.md](SUPPORT.md) before posting logs or screenshots.
- Report security concerns privately as described in [SECURITY.md](SECURITY.md).
- Follow the project [Code of Conduct](CODE_OF_CONDUCT.md).

Never include a PuppyClicker API key, access token, credential-bearing config
file, or another person's private information in an issue, commit, or pull
request. If a key has been exposed, replace it through PuppyClicker immediately.

## Development setup

Clone the repository and ensure Java 17, Java 21, and Java 25 JDKs are
available. Gradle's toolchain resolver can obtain a missing JDK. On macOS or Linux, make the Gradle
wrapper executable before the first run:

```bash
chmod +x gradlew
```

Launch a specific development client by selecting its loader and version, for
example:

```bash
./gradlew :platforms:neoforge:mc1.21.1:runClient
./gradlew :platforms:forge:mc1.20.1:runClient
./gradlew :platforms:fabric:mc1.19.2:runClient
```

The development client stores its local API key under the selected module's
`run/config/` directory. NeoForge and Forge use
`puppyclicker-client.toml`; Fabric uses `puppyclicker-client.json`. Module
`run/` and `runs/` directories are ignored by Git, but contributors must still
check changes for credentials before committing.

The repository separates loader-neutral HTTP and safety logic in `common/`
from version adapters under `platforms/<loader>/`. Shared loader resources and
build conventions live under each loader's `common/` directory and `gradle/`.
See [the porting guide](docs/PORTING.md) before adding a Minecraft version or
loader.

## Building and verification

Run the complete Gradle build before submitting a change:

```bash
./gradlew build
```

Each release JAR is written to its version module's `build/libs/` directory and
includes the Minecraft version in its filename. Verify behaviour in proportion
to the change. Network, configuration, item, or UI changes should also be
checked in each affected development client. Relevant manual checks include:

- The mod appears in the loader's mod list. Its Config button opens on
  NeoForge/Forge, and the settings keybind opens the screen on Fabric.
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
loader behaviour, or reasoning that is not obvious from the code itself. Add
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
   git tag -a v2.0.0 -m "PuppyClicker for Minecraft v2.0.0"
   git push origin v2.0.0
   ```

The workflow verifies that the tag and `mod_version` match, makes all 15
loader/version JARs with the Java 17, 21, and 25 toolchains, and publishes each
JAR with its own SHA-256 checksum and generated release notes. Tags with a suffix such as
`v2.1.0-beta.1` are published as prereleases and are not marked as the latest
stable release. Validation and every build must succeed before the workflow
attempts to create a release.

## Licence and assets

Contributions to the mod's source code and documentation are accepted under the
[MIT License](LICENSE). Do not contribute branding or other assets unless their
origin and reuse permission can be documented in
[ASSET_LICENSES.md](ASSET_LICENSES.md). Contributions to the adapted
[Code of Conduct](CODE_OF_CONDUCT.md#attribution) are covered by its stated
CC BY-SA 4.0 terms.
