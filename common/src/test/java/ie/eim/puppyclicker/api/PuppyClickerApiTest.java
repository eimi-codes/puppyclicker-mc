package ie.eim.puppyclicker.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;

class PuppyClickerApiTest {
    @Test
    void selfShockUsesOnlyTheOscActionType() {
        JsonObject body = JsonParser.parseString(PuppyClickerApi.selfShockRequestBody())
                .getAsJsonObject();

        assertEquals(1, body.size());
        assertEquals("osc", body.get("type").getAsString());
        assertFalse(body.has("message"));
        assertFalse(body.has("integration"));
        assertFalse(body.has("targetUserId"));
    }
}
