package ie.eim.puppyclicker.network;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import ie.eim.puppyclicker.PuppyClickerMod;
import ie.eim.puppyclicker.component.BoundFriend;
import ie.eim.puppyclicker.component.ModDataComponents;
import ie.eim.puppyclicker.item.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server-to-client notification that a supported gameplay automation event occurred. */
public record AutomationTriggerPayload(AutomationTrigger trigger, String attackerBoundFriendIds)
        implements CustomPacketPayload {
    private static final int MAX_BOUND_IDS_LENGTH = 2048;
    public static final Type<AutomationTriggerPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(PuppyClickerMod.MOD_ID, "automation_trigger"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AutomationTriggerPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeEnum(payload.trigger);
                        buffer.writeUtf(payload.attackerBoundFriendIds, MAX_BOUND_IDS_LENGTH);
                    },
                    buffer -> new AutomationTriggerPayload(
                            buffer.readEnum(AutomationTrigger.class),
                            buffer.readUtf(MAX_BOUND_IDS_LENGTH)));

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

    public static void handle(AutomationTriggerPayload payload, IPayloadContext context) {
        // The registrar dispatches on the main thread; the indirection avoids loading client
        // classes from this common payload on a dedicated server.
        clientHandler.accept(payload);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
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
            BoundFriend friend = stack.get(ModDataComponents.BOUND_FRIEND.get());
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
