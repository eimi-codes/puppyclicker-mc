# Summary

Describe what this pull request changes and why it is useful.

## Testing

List the checks you performed. Include relevant Minecraft, NeoForge, and Java
versions for manual tests.

- [ ] `./gradlew build` succeeds.
- [ ] `git diff --check` reports no whitespace errors.
- [ ] I completed the relevant manual Minecraft checks, or explained why they
      were not needed.

## User-facing changes

Describe any new screens, messages, controls, configuration, network behaviour,
or item behaviour. Add screenshots when a visual change is easier to review
that way.

## Privacy, consent, and safety

- [ ] This change contains no API key, access token, private identifier, or
      credential-bearing config file.
- [ ] PuppyClicker actions still require deliberate player input.
- [ ] PuppyClicker API work remains client-side and asynchronous.
- [ ] The Minecraft server never receives the PuppyClicker API key.
- [ ] UI changes made after asynchronous work return to the Minecraft client
      thread.
- [ ] I considered accessible text, narration, colour-independent feedback,
      and keyboard use where relevant.

## Documentation and compatibility

- [ ] Player-facing text uses translation keys.
- [ ] I updated documentation, metadata, screenshots, or translations where
      behaviour changed.
- [ ] I did not broaden the advertised Minecraft or NeoForge compatibility
      without a dedicated build and runtime test.

## Additional notes

Add anything reviewers should know, including deliberate follow-up work or
specific `TODO` items.
