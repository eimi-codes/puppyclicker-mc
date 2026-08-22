package ie.eim.puppyclicker.network;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import ie.eim.puppyclicker.component.BoundFriend;
import ie.eim.puppyclicker.item.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

/** Server-to-client notification that a supported gameplay automation event occurred. */
public record AutomationTriggerPayload(AutomationTrigger trigger, String attackerBoundFriendIds) {
    private static final int MAX_BOUND_IDS_LENGTH = 2048;

    private static volatile Consumer<AutomationTriggerPayload> clientHandler = payload -> {
    };

    public AutomationTriggerPayload(AutomationTrigger trigger) {
        this(trigger, "");
    }

    public static AutomationTriggerPayload forDamage(DamageSource source) {
        return new AutomationTriggerPayload(
                AutomationTrigger.DAMAGE,
                collectAttackerBoundFriendIds(source));
    }

    public static void setClientHandler(Consumer<AutomationTriggerPayload> handler) {
        clientHandler = handler;
    }

    public static void encode(AutomationTriggerPayload payload, FriendlyByteBuf buffer) {
        buffer.writeEnum(payload.trigger);
        buffer.writeUtf(payload.attackerBoundFriendIds, MAX_BOUND_IDS_LENGTH);
    }

    public static AutomationTriggerPayload decode(FriendlyByteBuf buffer) {
        return new AutomationTriggerPayload(
                buffer.readEnum(AutomationTrigger.class),
                buffer.readUtf(MAX_BOUND_IDS_LENGTH));
    }

    public static void handle(
            AutomationTriggerPayload payload,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> clientHandler.accept(payload));
        context.setPacketHandled(true);
    }

    private static String collectAttackerBoundFriendIds(DamageSource source) {
        if (!(source.getEntity() instanceof ServerPlayer attacker)) {
            return "";
        }
        Set<String> ids = new LinkedHashSet<>();
        for (int slot = 0; slot < attacker.getInventory().getContainerSize(); slot++) {
            ItemStack stack = attacker.getInventory().getItem(slot);
            if (!stack.is(ModItems.CLICKER.get())) {
                continue;
            }
            BoundFriend friend = BoundFriend.fromStack(stack);
            if (friend != null) {
                try {
                    ids.add(UUID.fromString(friend.id()).toString());
                } catch (IllegalArgumentException ignored) {
                    // Ignore forged or obsolete item data rather than forwarding it to a client.
                }
            }
        }
        return String.join(",", ids);
    }

    public enum AutomationTrigger {
        ADVANCEMENT,
        DAMAGE
    }
}
