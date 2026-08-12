package ie.eim.puppyclicker.client;

import ie.eim.puppyclicker.component.BoundFriend;
import ie.eim.puppyclicker.item.ClickerItem;
import net.minecraft.client.Minecraft;

/** Resolves a clicker use into either binding/rebinding UI or an explicit friend click. */
public final class ClientClickerActions {
    private ClientClickerActions() {
    }

    public static void useClicker(ClickerItem.ClientUse use) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        BoundFriend friend = BoundFriend.fromStack(use.stack());
        // Sneak-use is the deliberate rebinding gesture for an already-bound stack.
        if (friend == null || use.rebind()) {
            client.setScreen(new FriendPickerScreen(use.hand(), friend));
            return;
        }

        ClientClickService.sendFriendClick(friend);
    }
}
