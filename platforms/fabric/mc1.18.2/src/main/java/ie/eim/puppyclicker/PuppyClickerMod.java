package ie.eim.puppyclicker;

import ie.eim.puppyclicker.item.ModItems;
import ie.eim.puppyclicker.network.ModNetworking;
import net.fabricmc.api.ModInitializer;

/** Fabric common entry point. Dedicated servers never load the client adapter. */
public final class PuppyClickerMod implements ModInitializer {
    public static final String MOD_ID = "puppyclicker";

    @Override
    public void onInitialize() {
        ModItems.register();
        ModNetworking.registerServer();
    }
}
