package ie.eim.puppyclicker.network;

import java.util.function.Consumer;

import ie.eim.puppyclicker.PuppyClickerMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/** Server-to-client notification that a supported gameplay automation occurred. */
public record AutomationTriggerPayload(AutomationTrigger trigger) {
    public static final ResourceLocation ID =
            new ResourceLocation(PuppyClickerMod.MOD_ID, "automation_trigger");
    private static volatile Consumer<AutomationTrigger> clientHandler = trigger -> {
    };

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(trigger);
    }

    public static AutomationTriggerPayload decode(FriendlyByteBuf buffer) {
        return new AutomationTriggerPayload(buffer.readEnum(AutomationTrigger.class));
    }

    public static void setClientHandler(Consumer<AutomationTrigger> handler) {
        clientHandler = handler;
    }

    public void handle() {
        clientHandler.accept(trigger);
    }

    public enum AutomationTrigger {
        ADVANCEMENT,
        DAMAGE
    }
}
