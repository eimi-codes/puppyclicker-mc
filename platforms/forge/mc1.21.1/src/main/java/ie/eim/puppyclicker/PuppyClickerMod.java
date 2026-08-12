package ie.eim.puppyclicker;

import ie.eim.puppyclicker.component.ModDataComponents;
import ie.eim.puppyclicker.item.ModItems;
import ie.eim.puppyclicker.network.ModNetworking;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Common mod entry point, loaded on both physical clients and dedicated servers.
 *
 * <p>Client-only configuration, HTTP, and UI registration deliberately lives in
 * {@link PuppyClickerClient} so a dedicated server never loads Minecraft client classes.</p>
 */
@Mod(PuppyClickerMod.MOD_ID)
public final class PuppyClickerMod {
    public static final String MOD_ID = "puppyclicker";

    public PuppyClickerMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        // These registries define game-visible state and therefore must exist on both sides.
        ModDataComponents.COMPONENTS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModNetworking.initialize();
        modEventBus.addListener(PuppyClickerMod::addCreativeTabContents);
        MinecraftForge.EVENT_BUS.addListener(AutomationGameplayEvents::onAdvancementEarned);
        MinecraftForge.EVENT_BUS.addListener(AutomationGameplayEvents::onPlayerDamaged);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            PuppyClickerClient.initialize(context);
        }
    }

    private static void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.CLICKER);
        }
    }
}
