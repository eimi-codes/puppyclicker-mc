package ie.eim.puppyclicker;

import ie.eim.puppyclicker.component.ModDataComponents;
import ie.eim.puppyclicker.item.ModItems;
import ie.eim.puppyclicker.network.ModNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;

/** Fabric common entry point. Dedicated servers never load the client adapter. */
public final class PuppyClickerMod implements ModInitializer {
    public static final String MOD_ID = "puppyclicker";

    @Override
    public void onInitialize() {
        ModDataComponents.register();
        ModItems.register();
        ModNetworking.registerServer();
        AutomationGameplayEvents.register();
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(entries -> entries.accept(ModItems.CLICKER.get()));
    }
}
