package ie.eim.puppyclicker.client;

import ie.eim.puppyclicker.shared.AutomationCooldown;

import java.util.concurrent.TimeUnit;

import ie.eim.puppyclicker.config.PuppyClickerConfig;
import ie.eim.puppyclicker.network.AutomationTriggerPayload;
import ie.eim.puppyclicker.network.AutomationTriggerPayload.AutomationTrigger;

/** Applies client-only opt-in and safety settings to gameplay automation triggers. */
public final class ClientAutomationService {
    private static final AutomationCooldown DAMAGE_SHOCK_COOLDOWN = new AutomationCooldown();

    private ClientAutomationService() {
    }

    public static void handleTrigger(AutomationTriggerPayload payload) {
        switch (payload.trigger()) {
            case ADVANCEMENT -> handleAdvancement();
            case DAMAGE -> handleDamage(payload.attackerBoundFriendIds());
        }
    }

    private static void handleAdvancement() {
        if (PuppyClickerConfig.clickOnAdvancement()) {
            ClientClickService.sendAdvancementClick();
        }
    }

    private static void handleDamage(String attackerBoundFriendIds) {
        if (!PuppyClickerConfig.shockOnDamage()
                || !PuppyClickerConfig.damageTriggerMode().accepts(
                        PuppyClickerConfig.accountId(),
                        attackerBoundFriendIds)) {
            return;
        }

        long cooldownMillis = TimeUnit.SECONDS.toMillis(
                PuppyClickerConfig.damageShockCooldownSeconds());
        if (DAMAGE_SHOCK_COOLDOWN.tryAcquire(cooldownMillis)) {
            // Acquire before starting I/O: repeated damage cannot queue multiple physical actions.
            ClientClickService.sendDamageShock();
        }
    }
}
