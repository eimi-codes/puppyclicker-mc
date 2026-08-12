package ie.eim.puppyclicker.network;

import ie.eim.puppyclicker.PuppyClickerMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** Owns the small Forge payload channel used for binding and automation triggers. */
public final class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(PuppyClickerMod.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    private ModNetworking() {
    }

    public static void initialize() {
        int messageId = 0;
        CHANNEL.messageBuilder(BindClickerPayload.class, messageId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(BindClickerPayload::encode)
                .decoder(BindClickerPayload::decode)
                .consumerMainThread(BindClickerPayload::handle)
                .add();
        CHANNEL.messageBuilder(AutomationTriggerPayload.class, messageId, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AutomationTriggerPayload::encode)
                .decoder(AutomationTriggerPayload::decode)
                .consumerMainThread(AutomationTriggerPayload::handle)
                .add();
    }

    public static void sendToPlayer(ServerPlayer player, AutomationTriggerPayload payload) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }

    public static void sendToServer(BindClickerPayload payload) {
        CHANNEL.sendToServer(payload);
    }
}
