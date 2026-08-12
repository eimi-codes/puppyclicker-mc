package ie.eim.puppyclicker.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/** Registers the manual click and settings key mappings. */
public final class ClientEvents {
    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath("puppyclicker", "main"));
    private static final KeyMapping SEND_SELF_CLICK = new KeyMapping(
            "key.puppyclicker.send_self_click",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            CATEGORY);
    private static final KeyMapping OPEN_SETTINGS = new KeyMapping(
            "key.puppyclicker.open_settings",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            CATEGORY);

    private ClientEvents() {
    }

    public static void register() {
        KeyMappingHelper.registerKeyMapping(SEND_SELF_CLICK);
        KeyMappingHelper.registerKeyMapping(OPEN_SETTINGS);
        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::onClientTick);
    }

    private static void onClientTick(Minecraft client) {
        while (SEND_SELF_CLICK.consumeClick()) {
            ClientClickService.sendSelfClick();
        }
        while (OPEN_SETTINGS.consumeClick()) {
            client.setScreen(new PuppyClickerConfigScreen(client.screen));
        }
    }
}
