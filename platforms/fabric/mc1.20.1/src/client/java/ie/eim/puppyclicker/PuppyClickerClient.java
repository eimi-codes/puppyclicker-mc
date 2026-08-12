package ie.eim.puppyclicker;

import ie.eim.puppyclicker.client.ClientAutomationService;
import ie.eim.puppyclicker.client.ClientClickerActions;
import ie.eim.puppyclicker.client.ClientEvents;
import ie.eim.puppyclicker.client.FabricClientNetworking;
import ie.eim.puppyclicker.config.PuppyClickerConfig;
import ie.eim.puppyclicker.item.ClickerItem;
import ie.eim.puppyclicker.network.AutomationTriggerPayload;
import net.fabricmc.api.ClientModInitializer;

/** Physical-client adapter for credentials, screens, key mappings, and API actions. */
public final class PuppyClickerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PuppyClickerConfig.load();
        ClickerItem.setClientUseHandler(ClientClickerActions::useClicker);
        AutomationTriggerPayload.setClientHandler(ClientAutomationService::handleTrigger);
        FabricClientNetworking.register();
        ClientEvents.register();
    }
}
