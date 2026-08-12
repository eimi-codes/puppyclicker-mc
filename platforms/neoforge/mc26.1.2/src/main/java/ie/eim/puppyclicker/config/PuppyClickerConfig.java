package ie.eim.puppyclicker.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Client-only credential and automation configuration. Never register this as COMMON/SERVER. */
public final class PuppyClickerConfig {
    private static final ModConfigSpec.ConfigValue<String> API_KEY;
    private static final ModConfigSpec.BooleanValue CLICK_ON_ADVANCEMENT;
    private static final ModConfigSpec.BooleanValue SHOCK_ON_DAMAGE;
    private static final ModConfigSpec.IntValue DAMAGE_SHOCK_COOLDOWN_SECONDS;
    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        API_KEY = builder
                .comment(
                        "PuppyClicker public API key.",
                        "This credential stays in the client config and is never sent to a Minecraft server.")
                .translation("puppyclicker.configuration.apiKey")
                .define("apiKey", "");

        builder.push("automations");
        CLICK_ON_ADVANCEMENT = builder
                .comment(
                        "Send a self-click after earning a visible Minecraft advancement.",
                        "Disabled by default; recipe and other background advancements are ignored.")
                .translation("puppyclicker.configuration.clickOnAdvancement")
                .define("clickOnAdvancement", false);
        SHOCK_ON_DAMAGE = builder
                .comment(
                        "Send a self-targeted OSC shock through PuppyClicker after taking damage.",
                        "Disabled by default; your PuppyClicker device setup determines the physical response.")
                .translation("puppyclicker.configuration.shockOnDamage")
                .define("shockOnDamage", false);
        DAMAGE_SHOCK_COOLDOWN_SECONDS = builder
                .comment(
                        "Minimum seconds between damage-triggered shock attempts.",
                        "The 15-second minimum protects against rapid damage bursts and API-budget exhaustion.")
                .translation("puppyclicker.configuration.damageShockCooldown")
                .defineInRange("damageShockCooldownSeconds", 30, 15, 300);
        builder.pop();
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

    public static boolean clickOnAdvancement() {
        return CLICK_ON_ADVANCEMENT.get();
    }

    public static boolean shockOnDamage() {
        return SHOCK_ON_DAMAGE.get();
    }

    public static int damageShockCooldownSeconds() {
        return DAMAGE_SHOCK_COOLDOWN_SECONDS.get();
    }

    public static void saveAutomationSettings(
            boolean clickOnAdvancement,
            boolean shockOnDamage,
            int damageShockCooldownSeconds) {
        CLICK_ON_ADVANCEMENT.set(clickOnAdvancement);
        SHOCK_ON_DAMAGE.set(shockOnDamage);
        DAMAGE_SHOCK_COOLDOWN_SECONDS.set(damageShockCooldownSeconds);
        SPEC.save();
    }
}
