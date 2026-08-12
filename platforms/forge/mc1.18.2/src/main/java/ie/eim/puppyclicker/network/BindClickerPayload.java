package ie.eim.puppyclicker.network;

import java.util.UUID;

import ie.eim.puppyclicker.component.BoundFriend;
import ie.eim.puppyclicker.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

/**
 * Client-to-server request to persist a selected friend on the held clicker.
 *
 * <p>The payload deliberately contains no API credential. All fields are bounded by the stream
 * codec and then validated again on the server before any inventory state is changed.</p>
 */
public record BindClickerPayload(boolean mainHand, String friendId, String friendName) {
    public static void encode(BindClickerPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBoolean(payload.mainHand);
        buffer.writeUtf(payload.friendId, 64);
        buffer.writeUtf(payload.friendName, 64);
    }

    public static BindClickerPayload decode(FriendlyByteBuf buffer) {
        return new BindClickerPayload(
                buffer.readBoolean(),
                buffer.readUtf(64),
                buffer.readUtf(64));
    }

    public static void handle(BindClickerPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        // Network handlers may run away from the game loop; enqueue all inventory mutation.
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            InteractionHand hand = payload.mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            ItemStack stack = player.getItemInHand(hand);
            // Do not trust the hand/item state captured earlier by the client.
            if (!stack.is(ModItems.CLICKER.get())) {
                return;
            }

            final String id;
            try {
                id = UUID.fromString(payload.friendId).toString();
            } catch (IllegalArgumentException exception) {
                return;
            }

            String strippedName = ChatFormatting.stripFormatting(payload.friendName);
            String name = strippedName == null ? "" : strippedName.trim();
            // Formatting is stripped before the API username becomes server-visible item text.
            if (name.isEmpty() || name.length() > 64) {
                return;
            }

            new BoundFriend(id, name).writeTo(stack);
            player.getInventory().setChanged();
            player.inventoryMenu.broadcastChanges();
            player.displayClientMessage(
                    new TranslatableComponent("message.puppyclicker.clicker_bound", name),
                    true);
        });
        context.setPacketHandled(true);
    }
}
