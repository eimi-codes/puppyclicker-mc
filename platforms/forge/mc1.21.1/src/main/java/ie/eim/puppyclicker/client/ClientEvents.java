package ie.eim.puppyclicker.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent.ClientTickEvent;

/** Registers and consumes the configurable self-click key mapping. */
public final class ClientEvents {
    private static final KeyMapping SEND_SELF_CLICK = new KeyMapping(
            "key.puppyclicker.send_self_click",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.puppyclicker");

    private ClientEvents() {
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(SEND_SELF_CLICK);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        while (SEND_SELF_CLICK.consumeClick()) {
            ClientClickService.sendSelfClick();
        }
    }
}
