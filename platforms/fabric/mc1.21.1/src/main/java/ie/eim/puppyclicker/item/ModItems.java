package ie.eim.puppyclicker.item;

import java.util.function.Supplier;

import ie.eim.puppyclicker.PuppyClickerMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/** Fabric item registry with a holder-shaped surface shared by the version adapters. */
public final class ModItems {
    private static final ClickerItem CLICKER_ITEM = new ClickerItem(
            new Item.Properties().stacksTo(1));
    public static final Supplier<ClickerItem> CLICKER = () -> CLICKER_ITEM;

    private ModItems() {
    }

    public static void register() {
        Registry.register(
                BuiltInRegistries.ITEM,
                ResourceLocation.fromNamespaceAndPath(PuppyClickerMod.MOD_ID, "clicker"),
                CLICKER_ITEM);
    }
}
