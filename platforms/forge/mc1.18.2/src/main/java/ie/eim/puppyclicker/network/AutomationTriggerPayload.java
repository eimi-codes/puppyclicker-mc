package ie.eim.puppyclicker.network;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/** Server-to-client notification that a supported gameplay automation event occurred. */
public record AutomationTriggerPayload(AutomationTrigger trigger) {

    private static volatile Consumer<AutomationTrigger> clientHandler = trigger -> {
    };

    public static void setClientHandler(Consumer<AutomationTrigger> handler) {
        clientHandler = handler;
    }

    public static void encode(AutomationTriggerPayload payload, FriendlyByteBuf buffer) {
        buffer.writeEnum(payload.trigger);
    }

    public static AutomationTriggerPayload decode(FriendlyByteBuf buffer) {
        return new AutomationTriggerPayload(buffer.readEnum(AutomationTrigger.class));
    }

    public static void handle(
            AutomationTriggerPayload payload,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> clientHandler.accept(payload.trigger));
        context.setPacketHandled(true);
    }

    public enum AutomationTrigger {
        ADVANCEMENT,
        DAMAGE
    }
}
