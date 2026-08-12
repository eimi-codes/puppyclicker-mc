package ie.eim.puppyclicker.network;

import java.util.UUID;

import ie.eim.puppyclicker.PuppyClickerMod;
import ie.eim.puppyclicker.component.BoundFriend;
import ie.eim.puppyclicker.component.ModDataComponents;
import ie.eim.puppyclicker.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/**
 * Client-to-server request to persist a selected friend on the held clicker.
 *
 * <p>The payload deliberately contains no API credential. All fields are bounded by the stream
 * codec and then validated again on the server before any inventory state is changed.</p>
 */
public record BindClickerPayload(boolean mainHand, String friendId, String friendName)
        implements CustomPacketPayload {
    public static final Type<BindClickerPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(PuppyClickerMod.MOD_ID, "bind_clicker"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BindClickerPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, BindClickerPayload::mainHand,
                    ByteBufCodecs.stringUtf8(64), BindClickerPayload::friendId,
                    ByteBufCodecs.stringUtf8(64), BindClickerPayload::friendName,
                    BindClickerPayload::new);

    public void handle(ServerPlayer player) {
            InteractionHand hand = mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            ItemStack stack = player.getItemInHand(hand);
            // Do not trust the hand/item state captured earlier by the client.
            if (!stack.is(ModItems.CLICKER.get())) {
                return;
            }

            final String id;
            try {
                id = UUID.fromString(friendId).toString();
            } catch (IllegalArgumentException exception) {
                return;
            }

            String strippedName = ChatFormatting.stripFormatting(friendName);
            String name = strippedName == null ? "" : strippedName.trim();
            // Formatting is stripped before the API username becomes server-visible item text.
            if (name.isEmpty() || name.length() > 64) {
                return;
            }

            stack.set(ModDataComponents.BOUND_FRIEND.get(), new BoundFriend(id, name));
            player.getInventory().setChanged();
            player.inventoryMenu.broadcastChanges();
            player.sendOverlayMessage(
                    Component.translatable("message.puppyclicker.clicker_bound", name));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
