package ie.eim.puppyclicker;

import ie.eim.puppyclicker.network.AutomationTriggerPayload;
import ie.eim.puppyclicker.network.AutomationTriggerPayload.AutomationTrigger;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Relays authoritative server gameplay events without exposing client configuration or keys. */
public final class AutomationGameplayEvents {
    private AutomationGameplayEvents() {
    }

    public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Recipe unlocks and other background mechanics also use advancements. Only events that
        // Minecraft considers toast-worthy count as a player-facing advancement automation.
        boolean showsToast = event.getAdvancement()
                .value()
                .display()
                .filter(DisplayInfo::shouldShowToast)
                .isPresent();
        if (showsToast) {
            PacketDistributor.sendToPlayer(
                    player,
                    new AutomationTriggerPayload(AutomationTrigger.ADVANCEMENT));
        }
    }

    public static void onPlayerDamaged(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getHealthDamage() > 0.0F) {
            PacketDistributor.sendToPlayer(
                    player,
                    new AutomationTriggerPayload(AutomationTrigger.DAMAGE));
        }
    }
}
