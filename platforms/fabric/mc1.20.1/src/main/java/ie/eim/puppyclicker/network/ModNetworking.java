package ie.eim.puppyclicker.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/** Server half of the classic Fabric packet channel. */
public final class ModNetworking {
    private ModNetworking() {
    }

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(
                BindClickerPayload.ID,
                (server, player, handler, buffer, responseSender) -> {
                    BindClickerPayload payload = BindClickerPayload.decode(buffer);
                    server.execute(() -> payload.handle(player));
                });
    }

    public static void sendToPlayer(ServerPlayer player, AutomationTriggerPayload payload) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        payload.encode(buffer);
        ServerPlayNetworking.send(player, AutomationTriggerPayload.ID, buffer);
    }
}
