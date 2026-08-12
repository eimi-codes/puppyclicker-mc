package ie.eim.puppyclicker.item;

import java.util.function.Supplier;

import ie.eim.puppyclicker.PuppyClickerMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

/** Fabric item registry. */
public final class ModItems {
    private static final Identifier CLICKER_ID =
            Identifier.fromNamespaceAndPath(PuppyClickerMod.MOD_ID, "clicker");
    private static final ClickerItem CLICKER_ITEM = new ClickerItem(
            new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, CLICKER_ID))
                    .stacksTo(1));
    public static final Supplier<ClickerItem> CLICKER = () -> CLICKER_ITEM;

    private ModItems() {
    }

    public static void register() {
        Registry.register(
                BuiltInRegistries.ITEM,
                CLICKER_ID,
                CLICKER_ITEM);
    }
}
