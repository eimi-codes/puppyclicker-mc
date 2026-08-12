# Security Policy

## Supported releases

Security fixes are made for the latest stable release of PuppyClicker for
Minecraft. Older stable releases and prereleases may be used to understand a
report, but users will normally be asked to update to the newest fixed release.

The supported Minecraft line is documented in the [README](README.md#requirements).
A newer Minecraft version is not considered supported merely because the game
allows the JAR to be selected.

## Report a vulnerability privately

Do not open a public GitHub issue for a suspected vulnerability or credential
exposure. Email [minecraftclicker@eim.ie](mailto:minecraftclicker@eim.ie) with
the subject **PuppyClicker Minecraft security report**.

Include as much of the following as is safe:

- A concise description of the problem and its potential impact.
- The mod, Minecraft, loader, Java, launcher, and operating-system versions.
- Reproduction steps or a small proof of concept.
- Whether the issue affects the client, dedicated server, or both.
- Any suggested mitigation.

Do not send a live PuppyClicker API key, Minecraft access token, or another
person's private information. Redact secrets from screenshots, logs, requests,
and configuration files.

If an API key has already been exposed, replace or revoke it through
PuppyClicker immediately. Do not wait for a response to the report before
protecting the account.

The maintainer will acknowledge reports as soon as practical, investigate them
privately, and coordinate disclosure when a fix or mitigation is ready. No
response-time or resolution-time guarantee is offered.

## Appropriate security reports

Examples include:

- Accidental API-key exposure or logging.
- A way for the Minecraft server to obtain a client's API key.
- Unintended remote requests or actions without deliberate player input.
- Unsafe handling of the client-to-server clicker-binding payload.
- A vulnerability introduced by the mod's code or packaged dependencies.

Ordinary bugs, setup questions, and feature requests belong in the
[guided issue forms](https://github.com/eimi-codes/puppyclicker-mc/issues/new/choose).
Problems with the PuppyClicker service itself should be directed to
[PuppyClicker](https://puppyclicker.app/).
