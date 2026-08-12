package ie.eim.puppyclicker.client;

import ie.eim.puppyclicker.network.AutomationTriggerPayload;
import ie.eim.puppyclicker.network.BindClickerPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

/** Client half of the classic Fabric packet channel. */
public final class FabricClientNetworking {
    private FabricClientNetworking() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(
                AutomationTriggerPayload.ID,
                (client, handler, buffer, responseSender) -> {
                    AutomationTriggerPayload payload = AutomationTriggerPayload.decode(buffer);
                    client.execute(payload::handle);
                });
    }

    public static void sendToServer(BindClickerPayload payload) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        payload.encode(buffer);
        ClientPlayNetworking.send(BindClickerPayload.ID, buffer);
    }
}
