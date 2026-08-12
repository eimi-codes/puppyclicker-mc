package ie.eim.puppyclicker.client;

import ie.eim.puppyclicker.network.AutomationTriggerPayload;
import ie.eim.puppyclicker.network.BindClickerPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/** Client half of the Fabric packet channel. */
public final class FabricClientNetworking {
    private FabricClientNetworking() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(
                AutomationTriggerPayload.TYPE,
                (payload, context) -> AutomationTriggerPayload.handle(payload));
    }

    public static void sendToServer(BindClickerPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
