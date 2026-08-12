package ie.eim.puppyclicker.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

/** Registers and consumes the configurable self-click key mapping. */
public final class ClientEvents {
    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath("puppyclicker", "main"));
    private static final KeyMapping SEND_SELF_CLICK = new KeyMapping(
            "key.puppyclicker.send_self_click",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            CATEGORY);

    private ClientEvents() {
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(SEND_SELF_CLICK);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        while (SEND_SELF_CLICK.consumeClick()) {
            ClientClickService.sendSelfClick();
        }
    }
}
