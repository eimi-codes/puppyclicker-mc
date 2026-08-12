package ie.eim.puppyclicker;

import ie.eim.puppyclicker.network.AutomationTriggerPayload;
import ie.eim.puppyclicker.network.AutomationTriggerPayload.AutomationTrigger;
import ie.eim.puppyclicker.network.ModNetworking;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;

/** Relays authoritative server damage events; advancement awards are bridged by a mixin. */
public final class AutomationGameplayEvents {
    private AutomationGameplayEvents() {
    }

    public static void register() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register(
                (entity, source, baseDamage, damageTaken, blocked) -> {
                    if (entity instanceof ServerPlayer player && damageTaken > 0.0F && !blocked) {
                        sendDamage(player);
                    }
                });
        // AFTER_DAMAGE deliberately excludes fatal damage, so cover that non-overlapping path.
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayer player) {
                sendDamage(player);
            }
        });
    }

    private static void sendDamage(ServerPlayer player) {
        ModNetworking.sendToPlayer(
                player,
                new AutomationTriggerPayload(AutomationTrigger.DAMAGE));
    }
}
