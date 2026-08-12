package ie.eim.puppyclicker.item;

import ie.eim.puppyclicker.PuppyClickerMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Common item registry. */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, PuppyClickerMod.MOD_ID);

    public static final RegistryObject<ClickerItem> CLICKER = ITEMS.register(
            "clicker",
            () -> new ClickerItem(
                    // One stack represents one friend binding; stacking would make ownership ambiguous.
                    new Item.Properties().stacksTo(1)));

    private ModItems() {
    }
}
