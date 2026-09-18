package ie.eim.puppyclicker.client;

import ie.eim.puppyclicker.config.PuppyClickerConfig;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

/** Registers and consumes the self-click and settings key mappings. */
public final class ClientEvents {
    private static final KeyMapping SEND_SELF_CLICK = new KeyMapping(
            "key.puppyclicker.send_self_click",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.puppyclicker");
    private static final KeyMapping OPEN_SETTINGS = new KeyMapping(
            "key.puppyclicker.open_settings",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.categories.puppyclicker");
    private static boolean setupHintShown;

    private ClientEvents() {
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(SEND_SELF_CLICK);
        event.register(OPEN_SETTINGS);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (!setupHintShown && client.player != null && PuppyClickerConfig.apiKey().isBlank()) {
            setupHintShown = true;
            client.player.displayClientMessage(Component.translatable(
                    "message.puppyclicker.setup_hint",
                    OPEN_SETTINGS.getTranslatedKeyMessage()), false);
        }
        while (SEND_SELF_CLICK.consumeClick()) {
            ClientClickService.sendSelfClick();
        }
        while (OPEN_SETTINGS.consumeClick()) {
            client.setScreen(new PuppyClickerConfigScreen(client.screen));
        }
    }
}
