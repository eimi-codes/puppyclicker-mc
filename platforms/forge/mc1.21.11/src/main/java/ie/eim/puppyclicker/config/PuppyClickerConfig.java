package ie.eim.puppyclicker.config;

import ie.eim.puppyclicker.api.PuppyClickerApi.OscActionCapabilities;
import ie.eim.puppyclicker.shared.DamageTriggerMode;
import net.minecraftforge.common.ForgeConfigSpec;

/** Client-only credential and automation configuration. Never register this as COMMON/SERVER. */
public final class PuppyClickerConfig {
    private static final ForgeConfigSpec.ConfigValue<String> API_KEY;
    private static final ForgeConfigSpec.ConfigValue<String> ACCOUNT_ID;
    private static final ForgeConfigSpec.BooleanValue CLICK_ON_ADVANCEMENT;
    private static final ForgeConfigSpec.BooleanValue SHOCK_ON_DAMAGE;
    private static final ForgeConfigSpec.ConfigValue<String> DAMAGE_TRIGGER_MODE;
    private static final ForgeConfigSpec.IntValue DAMAGE_SHOCK_COOLDOWN_SECONDS;
    private static final ForgeConfigSpec.ConfigValue<String> DAMAGE_ACTION_TYPE;
    private static final ForgeConfigSpec.IntValue DAMAGE_ACTION_INTENSITY;
    private static final ForgeConfigSpec.IntValue DAMAGE_ACTION_DURATION_MILLIS;
    private static final ForgeConfigSpec.ConfigValue<String> OSC_CAPABILITIES;
    public static final ForgeConfigSpec SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        API_KEY = builder
                .comment(
                        "PuppyClicker public API key.",
                        "This credential stays in the client config and is never sent to a Minecraft server.")
                .translation("puppyclicker.configuration.apiKey")
                .define("apiKey", "");
        ACCOUNT_ID = builder
                .comment("Public PuppyClicker account ID cached during API-key validation.")
                .define("accountId", "");

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
        DAMAGE_TRIGGER_MODE = builder
                .comment("Which successful damage events may trigger the configured OSC action.")
                .translation("puppyclicker.configuration.damageTriggerMode")
                .define("damageTriggerMode", DamageTriggerMode.ALL_DAMAGE.name());
        DAMAGE_SHOCK_COOLDOWN_SECONDS = builder
                .comment(
                        "Minimum seconds between damage-triggered shock attempts.",
                        "The 15-second minimum protects against rapid damage bursts and API-budget exhaustion.")
                .translation("puppyclicker.configuration.damageShockCooldown")
                .defineInRange("damageShockCooldownSeconds", 30, 15, 300);
        DAMAGE_ACTION_TYPE = builder
                .comment("OSC subtype used for damage automation (for example Shock or Vibrate).")
                .translation("puppyclicker.configuration.damageActionType")
                .define("damageActionType", OscActionCapabilities.DEFAULT_SUBTYPE);
        DAMAGE_ACTION_INTENSITY = builder
                .comment("OSC intensity, clamped to the limits found during API-key validation.")
                .translation("puppyclicker.configuration.damageActionIntensity")
                .defineInRange(
                        "damageActionIntensity",
                        OscActionCapabilities.DEFAULT_INTENSITY,
                        0,
                        Integer.MAX_VALUE);
        DAMAGE_ACTION_DURATION_MILLIS = builder
                .comment("OSC duration in milliseconds, clamped to the validated device limits.")
                .translation("puppyclicker.configuration.damageActionDuration")
                .defineInRange(
                        "damageActionDurationMillis",
                        OscActionCapabilities.DEFAULT_DURATION_MILLIS,
                        0,
                        Integer.MAX_VALUE);
        OSC_CAPABILITIES = builder
                .comment("Sanitized OSC limits cached by Validate & Save; contains no API key.")
                .define("oscCapabilities", "");
        builder.pop();
        SPEC = builder.build();
    }

    private PuppyClickerConfig() {
    }

    public static String apiKey() {
        return API_KEY.get();
    }

    public static void saveValidatedApiKey(
            String apiKey,
            String accountId,
            OscActionCapabilities capabilities) {
        API_KEY.set(apiKey);
        ACCOUNT_ID.set(accountId);
        OSC_CAPABILITIES.set(capabilities.toConfigString());
        normalizeDamageAction(capabilities);
        if (!capabilities.available()) {
            SHOCK_ON_DAMAGE.set(false);
        }
        SPEC.save();
    }

    public static void clearApiKey() {
        API_KEY.set("");
        ACCOUNT_ID.set("");
        OSC_CAPABILITIES.set("");
        SHOCK_ON_DAMAGE.set(false);
        SPEC.save();
    }

    public static boolean clickOnAdvancement() {
        return CLICK_ON_ADVANCEMENT.get();
    }

    public static boolean shockOnDamage() {
        return SHOCK_ON_DAMAGE.get() && oscCapabilities().available();
    }

    public static String accountId() {
        return ACCOUNT_ID.get();
    }

    public static DamageTriggerMode damageTriggerMode() {
        return DamageTriggerMode.fromConfigValue(DAMAGE_TRIGGER_MODE.get());
    }

    public static int damageShockCooldownSeconds() {
        return DAMAGE_SHOCK_COOLDOWN_SECONDS.get();
    }

    public static String damageActionType() {
        return oscCapabilities().normalizeSubtype(DAMAGE_ACTION_TYPE.get());
    }

    public static int damageActionIntensity() {
        return oscCapabilities().clampIntensity(DAMAGE_ACTION_INTENSITY.get());
    }

    public static int damageActionDurationMillis() {
        return oscCapabilities().clampDurationMillis(DAMAGE_ACTION_DURATION_MILLIS.get());
    }

    public static OscActionCapabilities oscCapabilities() {
        return OscActionCapabilities.fromConfigString(OSC_CAPABILITIES.get());
    }

    public static void saveAutomationSettings(
            boolean clickOnAdvancement,
            boolean shockOnDamage,
            DamageTriggerMode damageTriggerMode,
            int damageShockCooldownSeconds,
            String actionType,
            int actionIntensity,
            int actionDurationMillis) {
        OscActionCapabilities capabilities = oscCapabilities();
        CLICK_ON_ADVANCEMENT.set(clickOnAdvancement);
        SHOCK_ON_DAMAGE.set(shockOnDamage && capabilities.available());
        DAMAGE_TRIGGER_MODE.set((damageTriggerMode == null
                ? DamageTriggerMode.ALL_DAMAGE
                : damageTriggerMode).name());
        DAMAGE_SHOCK_COOLDOWN_SECONDS.set(damageShockCooldownSeconds);
        DAMAGE_ACTION_TYPE.set(capabilities.normalizeSubtype(actionType));
        DAMAGE_ACTION_INTENSITY.set(capabilities.clampIntensity(actionIntensity));
        DAMAGE_ACTION_DURATION_MILLIS.set(capabilities.clampDurationMillis(actionDurationMillis));
        SPEC.save();
    }

    private static void normalizeDamageAction(OscActionCapabilities capabilities) {
        DAMAGE_ACTION_TYPE.set(capabilities.normalizeSubtype(DAMAGE_ACTION_TYPE.get()));
        DAMAGE_ACTION_INTENSITY.set(capabilities.clampIntensity(DAMAGE_ACTION_INTENSITY.get()));
        DAMAGE_ACTION_DURATION_MILLIS.set(
                capabilities.clampDurationMillis(DAMAGE_ACTION_DURATION_MILLIS.get()));
    }
}
