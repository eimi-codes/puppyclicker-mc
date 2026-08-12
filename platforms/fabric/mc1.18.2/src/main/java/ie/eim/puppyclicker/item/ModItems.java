package ie.eim.puppyclicker.item;

import java.util.function.Supplier;

import ie.eim.puppyclicker.PuppyClickerMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

/** Fabric item registry for the 1.18 creative-tab API. */
public final class ModItems {
    private static final ClickerItem CLICKER_ITEM = new ClickerItem(
            new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_TOOLS));
    public static final Supplier<ClickerItem> CLICKER = () -> CLICKER_ITEM;

    private ModItems() {
    }

    public static void register() {
        Registry.register(
                Registry.ITEM,
                new ResourceLocation(PuppyClickerMod.MOD_ID, "clicker"),
                CLICKER_ITEM);
    }
}
