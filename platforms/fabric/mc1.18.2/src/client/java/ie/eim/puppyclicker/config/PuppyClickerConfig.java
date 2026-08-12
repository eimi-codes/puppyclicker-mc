package ie.eim.puppyclicker.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

/** Client-only JSON config. The API key is never synchronized to a Minecraft server. */
public final class PuppyClickerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static String apiKey = "";
    private static boolean clickOnAdvancement;
    private static boolean shockOnDamage;
    private static int damageShockCooldownSeconds = 30;

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
                clickOnAdvancement = stored.clickOnAdvancement;
                shockOnDamage = stored.shockOnDamage;
                damageShockCooldownSeconds = clamp(stored.damageShockCooldownSeconds);
            }
        } catch (IOException | RuntimeException ignored) {
            // Keep safe defaults for a missing or malformed local config; never log credentials.
        }
    }

    public static synchronized String apiKey() {
        return apiKey;
    }

    public static synchronized void saveApiKey(String value) {
        apiKey = value;
        save();
    }

    public static synchronized boolean clickOnAdvancement() {
        return clickOnAdvancement;
    }

    public static synchronized boolean shockOnDamage() {
        return shockOnDamage;
    }

    public static synchronized int damageShockCooldownSeconds() {
        return damageShockCooldownSeconds;
    }

    public static synchronized void saveAutomationSettings(
            boolean advancementClicks,
            boolean damageShocks,
            int cooldownSeconds) {
        clickOnAdvancement = advancementClicks;
        shockOnDamage = damageShocks;
        damageShockCooldownSeconds = clamp(cooldownSeconds);
        save();
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
                            clickOnAdvancement,
                            shockOnDamage,
                            damageShockCooldownSeconds)),
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
            boolean clickOnAdvancement,
            boolean shockOnDamage,
            int damageShockCooldownSeconds) {
    }
}
