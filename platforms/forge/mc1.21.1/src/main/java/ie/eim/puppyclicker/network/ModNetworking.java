package ie.eim.puppyclicker.network;

import ie.eim.puppyclicker.PuppyClickerMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.payload.PayloadProtocol;

/** Owns the small Forge payload channel used for binding and automation triggers. */
public final class ModNetworking {
    private static final Channel<CustomPacketPayload> CHANNEL = createChannel();

    private ModNetworking() {
    }

    public static void initialize() {
        // Loading this class creates and registers the channel exactly once.
    }

    public static void sendToPlayer(ServerPlayer player, AutomationTriggerPayload payload) {
        CHANNEL.send(payload, PacketDistributor.PLAYER.with(player));
    }

    public static void sendToServer(BindClickerPayload payload) {
        CHANNEL.send(payload, PacketDistributor.SERVER.noArg());
    }

    private static Channel<CustomPacketPayload> createChannel() {
        PayloadProtocol<RegistryFriendlyByteBuf, CustomPacketPayload> protocol = ChannelBuilder
                .named(PuppyClickerMod.MOD_ID + ":main")
                .networkProtocolVersion(1)
                .payloadChannel()
                .play();
        protocol.serverbound().addMain(
                BindClickerPayload.TYPE,
                BindClickerPayload.STREAM_CODEC,
                BindClickerPayload::handle);
        return protocol.clientbound().addMain(
                AutomationTriggerPayload.TYPE,
                AutomationTriggerPayload.STREAM_CODEC,
                AutomationTriggerPayload::handle).build();
    }
}
