package ie.eim.puppyclicker;

import ie.eim.puppyclicker.network.AutomationTriggerPayload;
import ie.eim.puppyclicker.network.AutomationTriggerPayload.AutomationTrigger;
import ie.eim.puppyclicker.network.ModNetworking;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;

/** Relays authoritative server gameplay events without exposing client configuration or keys. */
public final class AutomationGameplayEvents {
    private AutomationGameplayEvents() {
    }

    public static void onAdvancementEarned(AdvancementEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Recipe unlocks and other background mechanics also use advancements. Only events that
        // Minecraft considers toast-worthy count as a player-facing advancement automation.
        DisplayInfo display = event.getAdvancement().getDisplay();
        boolean showsToast = display != null && display.shouldShowToast();
        if (showsToast) {
            ModNetworking.sendToPlayer(
                    player,
                    new AutomationTriggerPayload(AutomationTrigger.ADVANCEMENT));
        }
    }

    public static void onPlayerDamaged(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && event.getAmount() > 0.0F) {
            ModNetworking.sendToPlayer(
                    player,
                    new AutomationTriggerPayload(AutomationTrigger.DAMAGE));
        }
    }
}
