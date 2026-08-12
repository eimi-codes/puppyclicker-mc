package ie.eim.puppyclicker;

import ie.eim.puppyclicker.component.ModDataComponents;
import ie.eim.puppyclicker.item.ModItems;
import ie.eim.puppyclicker.network.ModNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTabs;

/** Fabric common entry point. */
public final class PuppyClickerMod implements ModInitializer {
    public static final String MOD_ID = "puppyclicker";

    @Override
    public void onInitialize() {
        ModDataComponents.register();
        ModItems.register();
        ModNetworking.registerServer();
        AutomationGameplayEvents.register();
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(entries -> entries.accept(ModItems.CLICKER.get()));
    }
}
