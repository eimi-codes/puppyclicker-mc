package ie.eim.puppyclicker.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicLong;

import ie.eim.puppyclicker.shared.AutomationCooldown;

import org.junit.jupiter.api.Test;

class AutomationCooldownTest {
    @Test
    void permitsOneAttemptPerCooldownWindow() {
        AtomicLong now = new AtomicLong(1_000);
        AutomationCooldown cooldown = new AutomationCooldown(now::get);

        assertTrue(cooldown.tryAcquire(15_000));
        assertFalse(cooldown.tryAcquire(15_000));

        now.set(15_999);
        assertFalse(cooldown.tryAcquire(15_000));

        now.set(16_000);
        assertTrue(cooldown.tryAcquire(15_000));
    }

    @Test
    void aClockMovingBackwardsCannotBypassTheGate() {
        AtomicLong now = new AtomicLong(50_000);
        AutomationCooldown cooldown = new AutomationCooldown(now::get);

        assertTrue(cooldown.tryAcquire(30_000));
        now.set(10_000);

        assertFalse(cooldown.tryAcquire(30_000));
    }
}
