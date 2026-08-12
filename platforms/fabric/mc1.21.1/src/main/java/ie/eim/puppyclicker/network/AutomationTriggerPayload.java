package ie.eim.puppyclicker.network;

import java.util.function.Consumer;

import ie.eim.puppyclicker.PuppyClickerMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server-to-client notification that a supported gameplay automation event occurred. */
public record AutomationTriggerPayload(AutomationTrigger trigger) implements CustomPacketPayload {
    public static final Type<AutomationTriggerPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(PuppyClickerMod.MOD_ID, "automation_trigger"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AutomationTriggerPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> buffer.writeEnum(payload.trigger),
                    buffer -> new AutomationTriggerPayload(
                            buffer.readEnum(AutomationTrigger.class)));

    private static volatile Consumer<AutomationTrigger> clientHandler = trigger -> {
    };

    public static void setClientHandler(Consumer<AutomationTrigger> handler) {
        clientHandler = handler;
    }

    public static void handle(AutomationTriggerPayload payload) {
        clientHandler.accept(payload.trigger);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum AutomationTrigger {
        ADVANCEMENT,
        DAMAGE
    }
}
