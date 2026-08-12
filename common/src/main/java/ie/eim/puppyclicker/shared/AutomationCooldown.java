package ie.eim.puppyclicker.shared;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/** Thread-safe cooldown gate with an injectable clock for focused unit tests. */
public final class AutomationCooldown {
    private final LongSupplier clock;
    private final AtomicLong nextAllowedAtMillis = new AtomicLong();

    public AutomationCooldown() {
        this(System::currentTimeMillis);
    }

    public AutomationCooldown(LongSupplier clock) {
        this.clock = clock;
    }

    public boolean tryAcquire(long cooldownMillis) {
        long now = clock.getAsLong();
        while (true) {
            long nextAllowed = nextAllowedAtMillis.get();
            if (now < nextAllowed) {
                return false;
            }
            if (nextAllowedAtMillis.compareAndSet(nextAllowed, now + cooldownMillis)) {
                return true;
            }
        }
    }
}
