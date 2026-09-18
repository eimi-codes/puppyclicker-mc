# Translating PuppyClicker

PuppyClicker's player-facing text lives in one loader-neutral language
directory:

`common/src/main/resources/assets/puppyclicker/lang/`

Minecraft automatically falls back to US English when a locale is not present.
The canonical `en_us.json` file therefore defines the complete set of keys for
Fabric and NeoForge. Do not add or edit copies under an individual loader.

## Add a locale

1. Copy `en_us.json` in the same directory and name the copy with Minecraft's
   locale identifier, such as `ga_ie.json`.
2. Translate every value, but leave the JSON keys unchanged.
3. Run `./gradlew :common:test` from the repository root.
4. Test the locale in Minecraft, including the configuration screen, friend
   picker, item tooltips, hotbar messages, and narrator.
5. In the pull request, name the translator and say whether the translation was
   reviewed by a fluent speaker.

The tests reject missing, extra, or blank entries and format arguments that no
longer match US English. This keeps every shipped locale complete as the mod
grows.

## Translation rules

- Keep the product name `PuppyClicker`, protocol name `OSC`, HTTP status codes,
  and the API-key prefix `pak_` unchanged.
- Preserve placeholders such as `%s`. A language may use numbered placeholders
  such as `%1$s` when it needs to reorder values.
- Translate meaning rather than English word order. Keep buttons short enough
  for Minecraft's existing layouts.
- Treat friend names and other text inserted through placeholders as user data;
  do not add grammar that assumes a name's gender.
- Keep privacy, consent, safety, and error messages precise. Ask for review if a
  translation could change what data is sent or who can see it.
- Read narrator-specific strings aloud during review. Clarity matters more than
  matching the visual wording exactly.
- Do not submit unreviewed machine translation as if it were fluent work. It is
  fine to open a clearly labelled draft for a speaker to improve.

## Language priorities

The first review targets are British English and Irish, followed by languages
requested by players. Minecraft's novelty language choices are welcome too;
Pirate Speak, LOLCAT, Upside Down, Anglish, and Shakespearean English should be
kept in separate locale files and reviewed for a consistent voice.

Novelty locales still need accurate safety, privacy, error, and narrator text.
The joke should never obscure what an action will do.
