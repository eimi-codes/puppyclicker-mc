package ie.eim.puppyclicker.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ie.eim.puppyclicker.api.PuppyClickerApi.OscActionCapabilities;
import ie.eim.puppyclicker.shared.DamageTriggerMode;
import net.fabricmc.loader.api.FabricLoader;

/** Client-only JSON config. The API key is never synchronized to a Minecraft server. */
public final class PuppyClickerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static String apiKey = "";
    private static String accountId = "";
    private static boolean clickOnAdvancement;
    private static boolean shockOnDamage;
    private static DamageTriggerMode damageTriggerMode = DamageTriggerMode.ALL_DAMAGE;
    private static int damageShockCooldownSeconds = 30;
    private static String damageActionType = OscActionCapabilities.DEFAULT_SUBTYPE;
    private static int damageActionIntensity = OscActionCapabilities.DEFAULT_INTENSITY;
    private static int damageActionDurationMillis = OscActionCapabilities.DEFAULT_DURATION_MILLIS;
    private static String oscCapabilitiesConfig = "";

    private PuppyClickerConfig() {
    }

    public static synchronized void load() {
        Path path = path();
        if (!Files.isRegularFile(path)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            StoredConfig stored = GSON.fromJson(reader, StoredConfig.class);
            if (stored != null) {
                apiKey = stored.apiKey == null ? "" : stored.apiKey;
                accountId = stored.accountId == null ? "" : stored.accountId;
                clickOnAdvancement = stored.clickOnAdvancement;
                shockOnDamage = stored.shockOnDamage;
                damageTriggerMode = DamageTriggerMode.fromConfigValue(stored.damageTriggerMode);
                damageShockCooldownSeconds = clamp(stored.damageShockCooldownSeconds);
                damageActionType = stored.damageActionType == null
                        ? OscActionCapabilities.DEFAULT_SUBTYPE
                        : stored.damageActionType;
                damageActionIntensity = stored.damageActionIntensity == null
                        ? OscActionCapabilities.DEFAULT_INTENSITY
                        : Math.max(0, stored.damageActionIntensity);
                damageActionDurationMillis = stored.damageActionDurationMillis == null
                        ? OscActionCapabilities.DEFAULT_DURATION_MILLIS
                        : Math.max(0, stored.damageActionDurationMillis);
                oscCapabilitiesConfig = stored.oscCapabilities == null
                        ? ""
                        : stored.oscCapabilities;
                normalizeDamageAction();
            }
        } catch (IOException | RuntimeException ignored) {
            // Keep safe defaults for a missing or malformed local config; never log credentials.
        }
    }

    public static synchronized String apiKey() {
        return apiKey;
    }

    public static synchronized void saveValidatedApiKey(
            String value,
            String validatedAccountId,
            OscActionCapabilities capabilities) {
        apiKey = value;
        accountId = validatedAccountId;
        oscCapabilitiesConfig = capabilities.toConfigString();
        if (!capabilities.available()) {
            shockOnDamage = false;
        }
        normalizeDamageAction();
        save();
    }

    public static synchronized void clearApiKey() {
        apiKey = "";
        accountId = "";
        oscCapabilitiesConfig = "";
        shockOnDamage = false;
        save();
    }

    public static synchronized boolean clickOnAdvancement() {
        return clickOnAdvancement;
    }

    public static synchronized boolean shockOnDamage() {
        return shockOnDamage && oscCapabilities().available();
    }

    public static synchronized String accountId() {
        return accountId;
    }

    public static synchronized DamageTriggerMode damageTriggerMode() {
        return damageTriggerMode;
    }

    public static synchronized int damageShockCooldownSeconds() {
        return damageShockCooldownSeconds;
    }

    public static synchronized String damageActionType() {
        return oscCapabilities().normalizeSubtype(damageActionType);
    }

    public static synchronized int damageActionIntensity() {
        return oscCapabilities().clampIntensity(damageActionIntensity);
    }

    public static synchronized int damageActionDurationMillis() {
        return oscCapabilities().clampDurationMillis(damageActionDurationMillis);
    }

    public static synchronized OscActionCapabilities oscCapabilities() {
        return OscActionCapabilities.fromConfigString(oscCapabilitiesConfig);
    }

    public static synchronized void saveAutomationSettings(
            boolean advancementClicks,
            boolean damageShocks,
            DamageTriggerMode triggerMode,
            int cooldownSeconds,
            String actionType,
            int actionIntensity,
            int actionDurationMillis) {
        OscActionCapabilities capabilities = oscCapabilities();
        clickOnAdvancement = advancementClicks;
        shockOnDamage = damageShocks && capabilities.available();
        damageTriggerMode = triggerMode == null ? DamageTriggerMode.ALL_DAMAGE : triggerMode;
        damageShockCooldownSeconds = clamp(cooldownSeconds);
        damageActionType = capabilities.normalizeSubtype(actionType);
        damageActionIntensity = capabilities.clampIntensity(actionIntensity);
        damageActionDurationMillis = capabilities.clampDurationMillis(actionDurationMillis);
        save();
    }

    private static void normalizeDamageAction() {
        OscActionCapabilities capabilities = oscCapabilities();
        damageActionType = capabilities.normalizeSubtype(damageActionType);
        damageActionIntensity = capabilities.clampIntensity(damageActionIntensity);
        damageActionDurationMillis = capabilities.clampDurationMillis(damageActionDurationMillis);
        if (!capabilities.available()) {
            shockOnDamage = false;
        }
    }

    private static int clamp(int value) {
        return Math.max(15, Math.min(300, value == 0 ? 30 : value));
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("puppyclicker-client.json");
    }

    private static void save() {
        Path path = path();
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(
                    temporary,
                    GSON.toJson(new StoredConfig(
                            apiKey,
                            accountId,
                            clickOnAdvancement,
                            shockOnDamage,
                            damageTriggerMode.name(),
                            damageShockCooldownSeconds,
                            damageActionType,
                            damageActionIntensity,
                            damageActionDurationMillis,
                            oscCapabilitiesConfig)),
                    StandardCharsets.UTF_8);
            try {
                Files.move(
                        temporary,
                        path,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException unsupportedAtomicMove) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ignored) {
            // The screen remains usable for this session; avoid exposing the secret in logs.
        }
    }

    private record StoredConfig(
            String apiKey,
            String accountId,
            boolean clickOnAdvancement,
            boolean shockOnDamage,
            String damageTriggerMode,
            int damageShockCooldownSeconds,
            String damageActionType,
            Integer damageActionIntensity,
            Integer damageActionDurationMillis,
            String oscCapabilities) {
    }
}
