package ie.eim.puppyclicker.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

/** Common Fabric networking registration. */
public final class ModNetworking {
    private ModNetworking() {
    }

    public static void registerServer() {
        PayloadTypeRegistry.serverboundPlay().register(
                BindClickerPayload.TYPE, BindClickerPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(
                AutomationTriggerPayload.TYPE, AutomationTriggerPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
                BindClickerPayload.TYPE,
                (payload, context) -> payload.handle(context.player()));
    }

    public static void sendToPlayer(ServerPlayer player, AutomationTriggerPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
