package ie.eim.puppyclicker.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * Public friend identity persisted on a clicker stack.
 *
 * <p>This data is intentionally network-synchronized and server-visible. It never contains the
 * PuppyClicker API key.</p>
 */
public record BoundFriend(String id, String name) {
    private static final String FRIEND_ID_TAG = "PuppyClickerFriendId";
    private static final String FRIEND_NAME_TAG = "PuppyClickerFriendName";

    public static BoundFriend fromStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null
                || !tag.contains(FRIEND_ID_TAG, Tag.TAG_STRING)
                || !tag.contains(FRIEND_NAME_TAG, Tag.TAG_STRING)) {
            return null;
        }
        return new BoundFriend(tag.getString(FRIEND_ID_TAG), tag.getString(FRIEND_NAME_TAG));
    }

    public void writeTo(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(FRIEND_ID_TAG, id);
        tag.putString(FRIEND_NAME_TAG, name);
    }
}
