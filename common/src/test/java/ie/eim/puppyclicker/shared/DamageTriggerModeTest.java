package ie.eim.puppyclicker.shared;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DamageTriggerModeTest {
    private static final String SELF_ID = "3f1c8a2e-5b04-4d97-9a61-2e7c0f8b41d3";

    @Test
    void allDamageDoesNotRequireAnAttackerOrValidatedIdentity() {
        assertTrue(DamageTriggerMode.ALL_DAMAGE.accepts("", ""));
    }

    @Test
    void clickerHolderModeRequiresTheVictimsExactBoundId() {
        assertTrue(DamageTriggerMode.ATTACKER_CARRIES_MY_CLICKER.accepts(
                SELF_ID,
                "9c4d7b10-2f83-4e6a-b5c1-7d0928ea3f45," + SELF_ID));
        assertFalse(DamageTriggerMode.ATTACKER_CARRIES_MY_CLICKER.accepts(
                SELF_ID,
                "9c4d7b10-2f83-4e6a-b5c1-7d0928ea3f45"));
        assertFalse(DamageTriggerMode.ATTACKER_CARRIES_MY_CLICKER.accepts(SELF_ID, ""));
        assertFalse(DamageTriggerMode.ATTACKER_CARRIES_MY_CLICKER.accepts("", SELF_ID));
        assertFalse(DamageTriggerMode.ATTACKER_CARRIES_MY_CLICKER.accepts(null, SELF_ID));
    }

    @Test
    void malformedStoredModesFallBackToExistingAllDamageBehaviour() {
        assertSame(DamageTriggerMode.ALL_DAMAGE, DamageTriggerMode.fromConfigValue(null));
        assertSame(DamageTriggerMode.ALL_DAMAGE, DamageTriggerMode.fromConfigValue("unknown"));
        assertSame(
                DamageTriggerMode.ATTACKER_CARRIES_MY_CLICKER,
                DamageTriggerMode.fromConfigValue("ATTACKER_CARRIES_MY_CLICKER"));
    }
}
