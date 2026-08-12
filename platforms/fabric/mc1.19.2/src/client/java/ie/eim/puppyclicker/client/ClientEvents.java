package ie.eim.puppyclicker.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

/** Registers the manual click and settings key mappings. */
public final class ClientEvents {
    private static final KeyMapping SEND_SELF_CLICK = new KeyMapping(
            "key.puppyclicker.send_self_click",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.puppyclicker");
    private static final KeyMapping OPEN_SETTINGS = new KeyMapping(
            "key.puppyclicker.open_settings",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "key.categories.puppyclicker");

    private ClientEvents() {
    }

    public static void register() {
        KeyBindingHelper.registerKeyBinding(SEND_SELF_CLICK);
        KeyBindingHelper.registerKeyBinding(OPEN_SETTINGS);
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
