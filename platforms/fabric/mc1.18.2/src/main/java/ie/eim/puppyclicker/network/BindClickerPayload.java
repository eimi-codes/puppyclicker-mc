package ie.eim.puppyclicker.network;

import java.util.UUID;

import ie.eim.puppyclicker.PuppyClickerMod;
import ie.eim.puppyclicker.component.BoundFriend;
import ie.eim.puppyclicker.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

/** Bounded client-to-server request to bind the clicker in the selected hand. */
public record BindClickerPayload(boolean mainHand, String friendId, String friendName) {
    public static final ResourceLocation ID =
            new ResourceLocation(PuppyClickerMod.MOD_ID, "bind_clicker");

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(mainHand);
        buffer.writeUtf(friendId, 64);
        buffer.writeUtf(friendName, 64);
    }

    public static BindClickerPayload decode(FriendlyByteBuf buffer) {
        return new BindClickerPayload(buffer.readBoolean(), buffer.readUtf(64), buffer.readUtf(64));
    }

    public void handle(ServerPlayer player) {
        InteractionHand hand = mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack stack = player.getItemInHand(hand);
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
        if (name.isEmpty() || name.length() > 64) {
            return;
        }

        new BoundFriend(id, name).writeTo(stack);
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        player.displayClientMessage(
                new TranslatableComponent("message.puppyclicker.clicker_bound", name), true);
    }
}
