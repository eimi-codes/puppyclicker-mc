package ie.eim.puppyclicker.shared;

import java.util.UUID;

/** Selects which successful damage events may start the client-side OSC automation. */
public enum DamageTriggerMode {
    ALL_DAMAGE,
    ATTACKER_CARRIES_MY_CLICKER;

    public static DamageTriggerMode fromConfigValue(String value) {
        try {
            return valueOf(value == null ? "" : value);
        } catch (IllegalArgumentException exception) {
            return ALL_DAMAGE;
        }
    }

    public boolean accepts(String accountId, String attackerBoundFriendIds) {
        if (this == ALL_DAMAGE) {
            return true;
        }

        final String canonicalAccountId;
        try {
            canonicalAccountId = UUID.fromString(accountId).toString();
        } catch (IllegalArgumentException | NullPointerException exception) {
            return false;
        }

        if (attackerBoundFriendIds == null || attackerBoundFriendIds.isBlank()) {
            return false;
        }
        for (String candidate : attackerBoundFriendIds.split(",")) {
            if (canonicalAccountId.equals(candidate)) {
                return true;
            }
        }
        return false;
    }
}
