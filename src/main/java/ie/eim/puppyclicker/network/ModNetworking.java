package ie.eim.puppyclicker.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Registers the small common networking surface used for item binding. */
public final class ModNetworking {
    private ModNetworking() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        // TODO: Increment this channel version if the binding payload becomes incompatible.
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                BindClickerPayload.TYPE,
                BindClickerPayload.STREAM_CODEC,
                BindClickerPayload::handle);
    }
}
