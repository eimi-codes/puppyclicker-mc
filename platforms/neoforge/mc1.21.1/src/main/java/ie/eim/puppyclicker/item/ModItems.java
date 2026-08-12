package ie.eim.puppyclicker.item;

import ie.eim.puppyclicker.PuppyClickerMod;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Common item registry. */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PuppyClickerMod.MOD_ID);

    public static final DeferredItem<ClickerItem> CLICKER = ITEMS.registerItem(
            "clicker",
            ClickerItem::new,
            // One stack represents one friend binding; stacking would make ownership ambiguous.
            new Item.Properties().stacksTo(1));

    private ModItems() {
    }
}
