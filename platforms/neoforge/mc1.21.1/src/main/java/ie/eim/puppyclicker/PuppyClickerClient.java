package ie.eim.puppyclicker;

import ie.eim.puppyclicker.client.ClientAutomationService;
import ie.eim.puppyclicker.client.ClientClickerActions;
import ie.eim.puppyclicker.client.ClientEvents;
import ie.eim.puppyclicker.client.PuppyClickerConfigScreen;
import ie.eim.puppyclicker.config.PuppyClickerConfig;
import ie.eim.puppyclicker.item.ClickerItem;
import ie.eim.puppyclicker.network.AutomationTriggerPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Physical-client entry point for credentials, screens, key mappings, and API actions.
 * Nothing registered here is loaded by a dedicated server.
 */
@Mod(value = PuppyClickerMod.MOD_ID, dist = Dist.CLIENT)
public final class PuppyClickerClient {
    public PuppyClickerClient(IEventBus modEventBus, ModContainer modContainer) {
        // CLIENT config keeps the bearer credential out of server config synchronization.
        modContainer.registerConfig(
                ModConfig.Type.CLIENT,
                PuppyClickerConfig.SPEC,
                "puppyclicker-client.toml");
        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                (container, parent) -> new PuppyClickerConfigScreen(parent));

        // Inject a client callback without importing client classes from the common item class.
        ClickerItem.setClientUseHandler(ClientClickerActions::useClicker);
        AutomationTriggerPayload.setClientHandler(ClientAutomationService::handleTrigger);
        modEventBus.addListener(ClientEvents::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener(ClientEvents::onClientTick);
    }
}
