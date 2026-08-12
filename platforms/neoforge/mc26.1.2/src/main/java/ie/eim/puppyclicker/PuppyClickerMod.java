package ie.eim.puppyclicker;

import ie.eim.puppyclicker.component.ModDataComponents;
import ie.eim.puppyclicker.item.ModItems;
import ie.eim.puppyclicker.network.ModNetworking;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * Common mod entry point, loaded on both physical clients and dedicated servers.
 *
 * <p>Client-only configuration, HTTP, and UI registration deliberately lives in
 * {@link PuppyClickerClient} so a dedicated server never loads Minecraft client classes.</p>
 */
@Mod(PuppyClickerMod.MOD_ID)
public final class PuppyClickerMod {
    public static final String MOD_ID = "puppyclicker";

    public PuppyClickerMod(IEventBus modEventBus) {
        // These registries define game-visible state and therefore must exist on both sides.
        ModDataComponents.COMPONENTS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        modEventBus.addListener(ModNetworking::register);
        modEventBus.addListener(PuppyClickerMod::addCreativeTabContents);
        NeoForge.EVENT_BUS.addListener(AutomationGameplayEvents::onAdvancementEarned);
        NeoForge.EVENT_BUS.addListener(AutomationGameplayEvents::onPlayerDamaged);
    }

    private static void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.CLICKER);
        }
    }
}
