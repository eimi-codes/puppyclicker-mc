package ie.eim.puppyclicker;

import ie.eim.puppyclicker.component.ModDataComponents;
import ie.eim.puppyclicker.item.ModItems;
import ie.eim.puppyclicker.network.ModNetworking;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
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
        var modBusGroup = context.getModBusGroup();
        // These registries define game-visible state and therefore must exist on both sides.
        ModDataComponents.COMPONENTS.register(modBusGroup);
        ModItems.ITEMS.register(modBusGroup);
        ModNetworking.initialize();
        BuildCreativeModeTabContentsEvent.BUS.addListener(PuppyClickerMod::addCreativeTabContents);
        AdvancementEvent.AdvancementEarnEvent.BUS.addListener(
                AutomationGameplayEvents::onAdvancementEarned);
        LivingDamageEvent.BUS.addListener(AutomationGameplayEvents::onPlayerDamaged);
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
