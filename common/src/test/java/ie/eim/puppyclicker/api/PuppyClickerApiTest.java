package ie.eim.puppyclicker.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;

class PuppyClickerApiTest {
    @Test
    void parsesTheValidatedAccountIdFromMe() {
        assertEquals(
                "3f1c8a2e-5b04-4d97-9a61-2e7c0f8b41d3",
                PuppyClickerApi.parseAccountId("""
                        {
                          "id": "3f1c8a2e-5b04-4d97-9a61-2e7c0f8b41d3",
                          "username": "puppy"
                        }
                        """));
    }

    @Test
    void selfShockIncludesAllRequiredOscParameters() {
        JsonObject body = JsonParser.parseString(PuppyClickerApi.selfShockRequestBody())
                .getAsJsonObject();

        assertEquals(4, body.size());
        assertEquals("osc", body.get("type").getAsString());
        assertEquals("Shock", body.get("subtype").getAsString());
        assertEquals(50, body.get("intensity").getAsInt());
        assertEquals(500, body.get("duration").getAsInt());
        assertFalse(body.has("message"));
        assertFalse(body.has("integration"));
        assertFalse(body.has("targetUserId"));
    }

    @Test
    void selfOscActionUsesConfiguredValues() {
        JsonObject body = JsonParser.parseString(
                        PuppyClickerApi.selfOscActionRequestBody("Vibrate", 23, 900))
                .getAsJsonObject();

        assertEquals("osc", body.get("type").getAsString());
        assertEquals("Vibrate", body.get("subtype").getAsString());
        assertEquals(23, body.get("intensity").getAsInt());
        assertEquals(900, body.get("duration").getAsInt());
    }

    @Test
    void actionsResponseProvidesOnlineOscLimits() {
        String response = """
                {
                  "actions": [
                    {
                      "type": "click",
                      "params": {}
                    },
                    {
                      "type": "osc",
                      "subtypes": ["Shock", "Vibrate", "Sound", "Stop"],
                      "maxIntensity": 60,
                      "maxDuration": 5000,
                      "online": true,
                      "params": {
                        "intensity": {"min": 0, "max": 60, "required": true},
                        "duration": {"min": 300, "max": 5000, "required": true}
                      }
                    }
                  ]
                }
                """;

        PuppyClickerApi.OscActionCapabilities capabilities =
                PuppyClickerApi.parseOscCapabilities(response);

        assertTrue(capabilities.available());
        assertEquals(List.of("Shock", "Vibrate", "Sound"), capabilities.subtypes());
        assertEquals(0, capabilities.minIntensity());
        assertEquals(60, capabilities.maxIntensity());
        assertEquals(300, capabilities.minDurationMillis());
        assertEquals(5000, capabilities.maxDurationMillis());
        assertEquals(60, capabilities.clampIntensity(80));
        assertEquals(300, capabilities.clampDurationMillis(100));
    }

    @Test
    void offlineOscActionIsUnavailable() {
        String response = """
                {
                  "actions": [{
                    "type": "osc",
                    "subtypes": ["Shock"],
                    "maxIntensity": 50,
                    "maxDuration": 15000,
                    "online": false,
                    "params": {
                      "intensity": {"min": 0, "max": 50, "required": true},
                      "duration": {"min": 300, "max": 15000, "required": true}
                    }
                  }]
                }
                """;

        assertFalse(PuppyClickerApi.parseOscCapabilities(response).available());
    }

    @Test
    void capabilitiesRoundTripThroughClientConfigValue() {
        PuppyClickerApi.OscActionCapabilities original =
                new PuppyClickerApi.OscActionCapabilities(
                        true,
                        List.of("Shock", "Vibrate"),
                        0,
                        40,
                        300,
                        4000);

        assertEquals(
                original,
                PuppyClickerApi.OscActionCapabilities.fromConfigString(original.toConfigString()));
    }

    @Test
    void stopIsNotAvailableForDamageAutomation() {
        PuppyClickerApi.OscActionCapabilities capabilities =
                new PuppyClickerApi.OscActionCapabilities(
                        true,
                        List.of("Stop", "Vibrate"),
                        0,
                        40,
                        300,
                        4000);

        assertEquals(List.of("Vibrate"), capabilities.subtypes());
        assertEquals("Vibrate", capabilities.normalizeSubtype("Stop"));
    }
}
