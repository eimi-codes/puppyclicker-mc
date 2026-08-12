package ie.eim.puppyclicker;

import ie.eim.puppyclicker.client.ClientAutomationService;
import ie.eim.puppyclicker.client.ClientClickerActions;
import ie.eim.puppyclicker.client.ClientEvents;
import ie.eim.puppyclicker.client.PuppyClickerConfigScreen;
import ie.eim.puppyclicker.config.PuppyClickerConfig;
import ie.eim.puppyclicker.item.ClickerItem;
import ie.eim.puppyclicker.network.AutomationTriggerPayload;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.ConfigGuiHandler.ConfigGuiFactory;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Physical-client entry point for credentials, screens, key mappings, and API actions.
 * Nothing registered here is loaded by a dedicated server.
 */
public final class PuppyClickerClient {
    private PuppyClickerClient() {
    }

    public static void initialize(FMLJavaModLoadingContext context) {
        // CLIENT config keeps the bearer credential out of server config synchronization.
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.CLIENT,
                PuppyClickerConfig.SPEC,
                "puppyclicker-client.toml");
        ModLoadingContext.get().registerExtensionPoint(
                ConfigGuiFactory.class,
                () -> new ConfigGuiFactory(PuppyClickerConfigScreen::new));

        // Inject a client callback without importing client classes from the common item class.
        ClickerItem.setClientUseHandler(ClientClickerActions::useClicker);
        AutomationTriggerPayload.setClientHandler(ClientAutomationService::handleTrigger);
        ClientRegistry.registerKeyBinding(ClientEvents.selfClickMapping());
        MinecraftForge.EVENT_BUS.addListener(ClientEvents::onClientTick);
    }
}
