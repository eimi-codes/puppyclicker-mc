package ie.eim.puppyclicker.item;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import ie.eim.puppyclicker.component.BoundFriend;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * One clicker item type whose {@link BoundFriend} NBT identifies the target per stack.
 *
 * <p>The common item contains no Minecraft client imports. A no-op callback is replaced by the
 * physical-client entry point, keeping dedicated-server class loading safe.</p>
 *
 * <p>TODO: Replace the temporary vanilla tripwire-hook item model with dedicated clicker artwork.</p>
 */
public final class ClickerItem extends Item {
    private static Consumer<ClientUse> clientUseHandler = use -> {
    };

    public ClickerItem(Properties properties) {
        super(properties);
    }

    public static void setClientUseHandler(Consumer<ClientUse> handler) {
        clientUseHandler = Objects.requireNonNull(handler);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // The server observes a normal item use but never initiates a PuppyClicker API action.
        if (level.isClientSide()) {
            clientUseHandler.accept(new ClientUse(hand, stack, player.isShiftKeyDown()));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public Component getName(ItemStack stack) {
        BoundFriend friend = BoundFriend.fromStack(stack);
        return friend == null
                ? super.getName(stack)
                : new TranslatableComponent("item.puppyclicker.clicker.bound", friend.name());
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Level level,
            List<Component> tooltip,
            TooltipFlag flag) {
        BoundFriend friend = BoundFriend.fromStack(stack);
        if (friend == null) {
            tooltip.add(new TranslatableComponent("tooltip.puppyclicker.clicker.unbound")
                    .withStyle(ChatFormatting.YELLOW));
            tooltip.add(new TranslatableComponent("tooltip.puppyclicker.clicker.bind")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(new TranslatableComponent("tooltip.puppyclicker.clicker.bound", friend.name())
                    .withStyle(ChatFormatting.AQUA));
            tooltip.add(new TranslatableComponent("tooltip.puppyclicker.clicker.use")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("tooltip.puppyclicker.clicker.rebind")
                    .withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(new TranslatableComponent("tooltip.puppyclicker.clicker.binding_data")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(new TranslatableComponent("tooltip.puppyclicker.clicker.consent")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    public record ClientUse(InteractionHand hand, ItemStack stack, boolean rebind) {
    }
}
