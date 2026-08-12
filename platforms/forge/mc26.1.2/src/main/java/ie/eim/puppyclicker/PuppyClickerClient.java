package ie.eim.puppyclicker;

import ie.eim.puppyclicker.client.ClientAutomationService;
import ie.eim.puppyclicker.client.ClientClickerActions;
import ie.eim.puppyclicker.client.ClientEvents;
import ie.eim.puppyclicker.client.PuppyClickerConfigScreen;
import ie.eim.puppyclicker.config.PuppyClickerConfig;
import ie.eim.puppyclicker.item.ClickerItem;
import ie.eim.puppyclicker.network.AutomationTriggerPayload;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
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
        context.registerConfig(
                ModConfig.Type.CLIENT,
                PuppyClickerConfig.SPEC,
                "puppyclicker-client.toml");
        context.registerExtensionPoint(
                ConfigScreenFactory.class,
                () -> new ConfigScreenFactory(PuppyClickerConfigScreen::new));

        // Inject a client callback without importing client classes from the common item class.
        ClickerItem.setClientUseHandler(ClientClickerActions::useClicker);
        AutomationTriggerPayload.setClientHandler(ClientAutomationService::handleTrigger);
        RegisterKeyMappingsEvent.BUS.addListener(ClientEvents::registerKeyMappings);
        TickEvent.ClientTickEvent.Post.BUS.addListener(ClientEvents::onClientTick);
    }
}
