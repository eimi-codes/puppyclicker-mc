package ie.eim.puppyclicker.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Client-only credential configuration. This spec must never be registered as COMMON/SERVER. */
public final class PuppyClickerConfig {
    private static final ModConfigSpec.ConfigValue<String> API_KEY;
    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        API_KEY = builder
                .comment(
                        "PuppyClicker public API key.",
                        "This credential stays in the client config and is never sent to a Minecraft server.")
                .translation("puppyclicker.configuration.apiKey")
                .define("apiKey", "");
        SPEC = builder.build();
    }

    private PuppyClickerConfig() {
    }

    public static String apiKey() {
        return API_KEY.get();
    }

    public static void saveApiKey(String apiKey) {
        // The custom screen calls this only after /me validates the candidate (or when clearing it).
        API_KEY.set(apiKey);
        SPEC.save();
    }
}
