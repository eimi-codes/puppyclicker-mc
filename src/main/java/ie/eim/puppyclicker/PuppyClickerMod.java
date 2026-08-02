package ie.eim.puppyclicker;

import ie.eim.puppyclicker.client.ClientEvents;
import ie.eim.puppyclicker.config.PuppyClickerConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = PuppyClickerMod.MOD_ID, dist = Dist.CLIENT)
public final class PuppyClickerMod {
    public static final String MOD_ID = "puppyclicker";

    public PuppyClickerMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(
                ModConfig.Type.CLIENT,
                PuppyClickerConfig.SPEC,
                "puppyclicker-client.toml");
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener(ClientEvents::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener(ClientEvents::onClientTick);
    }
}
