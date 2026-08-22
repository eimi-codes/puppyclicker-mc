package ie.eim.puppyclicker.api;

import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Minimal asynchronous client for the PuppyClicker v2 REST API.
 *
 * <p>Every public operation returns a future and normalizes expected failures into an
 * {@link Outcome}; callers must marshal UI work back to the Minecraft client thread. The API
 * key is used only to build the Authorization header and must never be logged.</p>
 *
 * <p>TODO: Add a separately managed {@code /stream} SSE client when incoming-click support is
 * implemented. It needs explicit connection lifecycle, reconnect, and shutdown handling.</p>
 */
public final class PuppyClickerApi {
    private static final String API_BASE = "https://puppyclicker-api.boundfire.com/api/v2";
    private static final URI ME_URI = URI.create(API_BASE + "/me");
    private static final URI SELF_CLICK_URI = URI.create(API_BASE + "/clicks/self");
    private static final URI SELF_ACTION_URI = URI.create(API_BASE + "/puppies/self/actions");
    private static final URI FRIENDS_URI = URI.create(API_BASE + "/puppies");
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private PuppyClickerApi() {
    }

    public static CompletableFuture<ClickResult> sendSelfClick(String apiKey) {
        return sendSelfClick(apiKey, "Click from Minecraft");
    }

    public static CompletableFuture<ClickResult> sendSelfClick(String apiKey, String message) {
        JsonObject body = new JsonObject();
        body.addProperty("message", message);
        return sendAction(apiKey, SELF_CLICK_URI, body.toString());
    }

    /** Sends the backwards-compatible default shock action (Shock, 50 intensity, 500 ms). */
    public static CompletableFuture<ClickResult> sendSelfShock(String apiKey) {
        return sendAction(apiKey, SELF_ACTION_URI, selfShockRequestBody());
    }

    static String selfShockRequestBody() {
        return selfOscActionRequestBody(
                OscActionCapabilities.DEFAULT_SUBTYPE,
                OscActionCapabilities.DEFAULT_INTENSITY,
                OscActionCapabilities.DEFAULT_DURATION_MILLIS);
    }

    public static CompletableFuture<ClickResult> sendSelfOscAction(
            String apiKey,
            String subtype,
            int intensity,
            int durationMillis) {
        if (subtype == null || subtype.isBlank() || intensity < 0 || durationMillis <= 0) {
            return CompletableFuture.completedFuture(ClickResult.invalidRequest());
        }
        return sendAction(
                apiKey,
                SELF_ACTION_URI,
                selfOscActionRequestBody(subtype, intensity, durationMillis));
    }

    static String selfOscActionRequestBody(String subtype, int intensity, int durationMillis) {
        JsonObject body = new JsonObject();
        body.addProperty("type", "osc");
        body.addProperty("subtype", subtype);
        body.addProperty("intensity", intensity);
        body.addProperty("duration", durationMillis);
        return body.toString();
    }

    public static CompletableFuture<ValidationResult> validateApiKey(String apiKey) {
        final HttpRequest request;
        try {
            request = requestBuilder(apiKey, ME_URI).GET().build();
        } catch (IllegalArgumentException exception) {
            return CompletableFuture.completedFuture(ValidationResult.invalidRequest());
        }

        return HTTP_CLIENT.sendAsync(
                        request,
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .handle(PuppyClickerApi::toValidationResult)
                .thenCompose(result -> result.outcome() == Outcome.SUCCESS
                        ? fetchSelfActions(apiKey, result.accountId())
                        : CompletableFuture.completedFuture(result));
    }

    public static CompletableFuture<ClickResult> sendFriendClick(String apiKey, String friendId) {
        // Accept only canonical UUIDs before interpolating an identifier into the endpoint path.
        final UUID friendUuid;
        try {
            friendUuid = UUID.fromString(friendId);
        } catch (IllegalArgumentException exception) {
            return CompletableFuture.completedFuture(ClickResult.invalidRequest());
        }

        URI target = URI.create(API_BASE + "/puppies/" + friendUuid + "/actions");
        JsonObject body = new JsonObject();
        body.addProperty("type", "click");
        body.addProperty("message", "Click from Minecraft");
        return sendAction(apiKey, target, body.toString());
    }

    public static CompletableFuture<FriendsResult> fetchFriends(String apiKey) {
        final HttpRequest request;
        try {
            request = requestBuilder(apiKey, FRIENDS_URI).GET().build();
        } catch (IllegalArgumentException exception) {
            return CompletableFuture.completedFuture(FriendsResult.invalidRequest());
        }

        return HTTP_CLIENT.sendAsync(
                        request,
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .handle(PuppyClickerApi::toFriendsResult);
    }

    private static CompletableFuture<ValidationResult> fetchSelfActions(
            String apiKey,
            String accountId) {
        final HttpRequest request;
        try {
            request = requestBuilder(apiKey, SELF_ACTION_URI).GET().build();
        } catch (IllegalArgumentException exception) {
            return CompletableFuture.completedFuture(ValidationResult.invalidRequest());
        }

        return HTTP_CLIENT.sendAsync(
                        request,
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .handle((response, throwable) ->
                        toActionsValidationResult(response, throwable, accountId));
    }

    private static CompletableFuture<ClickResult> sendAction(String apiKey, URI uri, String body) {
        final HttpRequest request;
        try {
            request = requestBuilder(apiKey, uri)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();
        } catch (IllegalArgumentException exception) {
            return CompletableFuture.completedFuture(ClickResult.invalidRequest());
        }

        return HTTP_CLIENT.sendAsync(
                        request,
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .handle(PuppyClickerApi::toClickResult);
    }

    private static HttpRequest.Builder requestBuilder(String apiKey, URI uri) {
        // Do not add request/headers logging here: this header contains the user's credential.
        return HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json");
    }

    private static ClickResult toClickResult(HttpResponse<String> response, Throwable throwable) {
        if (throwable != null) {
            return ClickResult.fromOutcome(networkOutcome(throwable));
        }

        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300) {
            return ClickResult.success(statusCode, booleanValue(response.body(), "dndSuppressed"));
        }
        if (statusCode == 429) {
            return ClickResult.rateLimited(retryAfter(response));
        }
        return ClickResult.httpError(statusCode);
    }

    private static ValidationResult toValidationResult(
            HttpResponse<String> response,
            Throwable throwable) {
        if (throwable != null) {
            return ValidationResult.fromOutcome(networkOutcome(throwable));
        }

        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300) {
            try {
                return ValidationResult.success(
                        statusCode,
                        parseAccountId(response.body()),
                        OscActionCapabilities.unavailable());
            } catch (RuntimeException exception) {
                return ValidationResult.invalidResponse();
            }
        }
        if (statusCode == 429) {
            return ValidationResult.rateLimited(retryAfter(response));
        }
        return ValidationResult.httpError(statusCode);
    }

    private static ValidationResult toActionsValidationResult(
            HttpResponse<String> response,
            Throwable throwable,
            String accountId) {
        if (throwable != null) {
            return ValidationResult.fromOutcome(networkOutcome(throwable));
        }

        int statusCode = response.statusCode();
        if (statusCode == 429) {
            return ValidationResult.rateLimited(retryAfter(response));
        }
        if (statusCode < 200 || statusCode >= 300) {
            return ValidationResult.httpError(statusCode);
        }

        try {
            return ValidationResult.success(
                    statusCode,
                    accountId,
                    parseOscCapabilities(response.body()));
        } catch (RuntimeException exception) {
            return ValidationResult.invalidResponse();
        }
    }

    static String parseAccountId(String body) {
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        return UUID.fromString(stringValue(root, "id")).toString();
    }

    private static FriendsResult toFriendsResult(HttpResponse<String> response, Throwable throwable) {
        if (throwable != null) {
            return FriendsResult.fromOutcome(networkOutcome(throwable));
        }

        int statusCode = response.statusCode();
        if (statusCode == 429) {
            return FriendsResult.rateLimited(retryAfter(response));
        }
        if (statusCode < 200 || statusCode >= 300) {
            return FriendsResult.httpError(statusCode);
        }

        try {
            // The endpoint is expected to return {"puppies":[...]}.
            JsonObject root = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray puppies = root.getAsJsonArray("puppies");
            if (puppies == null) {
                return FriendsResult.invalidResponse();
            }

            List<PuppyFriend> friends = new ArrayList<>();
            for (JsonElement element : puppies) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject puppy = element.getAsJsonObject();
                String id = stringValue(puppy, "id");
                String username = stringValue(puppy, "username");
                if (id.isBlank() || username.isBlank()) {
                    continue;
                }
                UUID.fromString(id);
                friends.add(new PuppyFriend(id, username));
            }
            return FriendsResult.success(List.copyOf(friends));
        } catch (RuntimeException exception) {
            return FriendsResult.invalidResponse();
        }
    }

    static OscActionCapabilities parseOscCapabilities(String body) {
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        JsonArray actions = root.getAsJsonArray("actions");
        if (actions == null) {
            throw new IllegalArgumentException("Missing actions array");
        }

        for (JsonElement element : actions) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject action = element.getAsJsonObject();
            if (!"osc".equalsIgnoreCase(stringValue(action, "type"))) {
                continue;
            }
            if (!requiredBoolean(action, "online")) {
                continue;
            }

            List<String> subtypes = stringValues(action, "subtypes");
            JsonObject params = requiredObject(action, "params");
            JsonObject intensity = requiredObject(params, "intensity");
            JsonObject duration = requiredObject(params, "duration");
            int minIntensity = requiredInt(intensity, "min");
            int maxIntensity = Math.min(
                    requiredInt(action, "maxIntensity"),
                    requiredInt(intensity, "max"));
            int minDuration = requiredInt(duration, "min");
            int maxDuration = Math.min(
                    requiredInt(action, "maxDuration"),
                    requiredInt(duration, "max"));

            if (subtypes.isEmpty()
                    || minIntensity < 0
                    || maxIntensity < minIntensity
                    || minDuration < 0
                    || maxDuration < minDuration) {
                throw new IllegalArgumentException("Invalid OSC action limits");
            }
            return new OscActionCapabilities(
                    true,
                    subtypes,
                    minIntensity,
                    maxIntensity,
                    minDuration,
                    maxDuration);
        }
        return OscActionCapabilities.unavailable();
    }

    private static JsonObject requiredObject(JsonObject object, String key) {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonObject()) {
            throw new IllegalArgumentException("Missing object: " + key);
        }
        return value.getAsJsonObject();
    }

    private static boolean requiredBoolean(JsonObject object, String key) {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException("Missing boolean: " + key);
        }
        return value.getAsBoolean();
    }

    private static int requiredInt(JsonObject object, String key) {
        JsonElement value = object.get(key);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("Missing number: " + key);
        }
        double number = value.getAsDouble();
        int integer = value.getAsInt();
        if (!Double.isFinite(number) || number != integer) {
            throw new IllegalArgumentException("Expected integer: " + key);
        }
        return integer;
    }

    private static List<String> stringValues(JsonObject object, String key) {
        JsonArray values = object.getAsJsonArray(key);
        if (values == null) {
            throw new IllegalArgumentException("Missing array: " + key);
        }

        Set<String> normalized = new LinkedHashSet<>();
        List<String> result = new ArrayList<>();
        for (JsonElement value : values) {
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
                throw new IllegalArgumentException("Expected string in: " + key);
            }
            String subtype = value.getAsString().trim();
            if (subtype.isEmpty() || subtype.length() > 32) {
                throw new IllegalArgumentException("Invalid OSC subtype");
            }
            if (normalized.add(subtype.toLowerCase(Locale.ROOT))) {
                result.add(subtype);
            }
        }
        return List.copyOf(result);
    }

    private static String stringValue(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString() : "";
    }

    private static boolean booleanValue(String body, String key) {
        try {
            JsonElement value = JsonParser.parseString(body).getAsJsonObject().get(key);
            return value != null && value.isJsonPrimitive() && value.getAsBoolean();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static String retryAfter(HttpResponse<?> response) {
        return response.headers().firstValue("Retry-After").orElse("");
    }

    private static Outcome networkOutcome(Throwable throwable) {
        Throwable cause = unwrap(throwable);
        if (cause instanceof HttpTimeoutException) {
            return Outcome.TIMEOUT;
        }
        if (cause instanceof ConnectException || cause instanceof UnknownHostException) {
            return Outcome.NETWORK_UNAVAILABLE;
        }
        return Outcome.NETWORK_ERROR;
    }

    private static Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        while (current instanceof CompletionException && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    public enum Outcome {
        SUCCESS,
        INVALID_REQUEST,
        INVALID_RESPONSE,
        RATE_LIMITED,
        HTTP_ERROR,
        TIMEOUT,
        NETWORK_UNAVAILABLE,
        NETWORK_ERROR
    }

    public record PuppyFriend(String id, String username) {
    }

    public record ClickResult(
            Outcome outcome,
            int statusCode,
            String retryAfter,
            boolean dndSuppressed) {
        public static ClickResult success(int statusCode, boolean dndSuppressed) {
            return new ClickResult(Outcome.SUCCESS, statusCode, "", dndSuppressed);
        }

        public static ClickResult invalidRequest() {
            return fromOutcome(Outcome.INVALID_REQUEST);
        }

        public static ClickResult rateLimited(String retryAfter) {
            return new ClickResult(Outcome.RATE_LIMITED, 429, retryAfter, false);
        }

        public static ClickResult httpError(int statusCode) {
            return new ClickResult(Outcome.HTTP_ERROR, statusCode, "", false);
        }

        public static ClickResult fromOutcome(Outcome outcome) {
            return new ClickResult(outcome, 0, "", false);
        }
    }

    public record ValidationResult(
            Outcome outcome,
            int statusCode,
            String retryAfter,
            String accountId,
            OscActionCapabilities oscCapabilities) {
        public ValidationResult {
            accountId = accountId == null ? "" : accountId;
            oscCapabilities = oscCapabilities == null
                    ? OscActionCapabilities.unavailable()
                    : oscCapabilities;
        }

        public static ValidationResult success(int statusCode) {
            return success(statusCode, "", OscActionCapabilities.unavailable());
        }

        public static ValidationResult success(
                int statusCode,
                OscActionCapabilities oscCapabilities) {
            return success(statusCode, "", oscCapabilities);
        }

        public static ValidationResult success(
                int statusCode,
                String accountId,
                OscActionCapabilities oscCapabilities) {
            return new ValidationResult(
                    Outcome.SUCCESS,
                    statusCode,
                    "",
                    accountId,
                    oscCapabilities);
        }

        public static ValidationResult invalidRequest() {
            return fromOutcome(Outcome.INVALID_REQUEST);
        }

        public static ValidationResult rateLimited(String retryAfter) {
            return new ValidationResult(
                    Outcome.RATE_LIMITED,
                    429,
                    retryAfter,
                    "",
                    OscActionCapabilities.unavailable());
        }

        public static ValidationResult httpError(int statusCode) {
            return new ValidationResult(
                    Outcome.HTTP_ERROR,
                    statusCode,
                    "",
                    "",
                    OscActionCapabilities.unavailable());
        }

        public static ValidationResult invalidResponse() {
            return fromOutcome(Outcome.INVALID_RESPONSE);
        }

        public static ValidationResult fromOutcome(Outcome outcome) {
            return new ValidationResult(
                    outcome,
                    0,
                    "",
                    "",
                    OscActionCapabilities.unavailable());
        }
    }

    public record OscActionCapabilities(
            boolean online,
            List<String> subtypes,
            int minIntensity,
            int maxIntensity,
            int minDurationMillis,
            int maxDurationMillis) {
        public static final String DEFAULT_SUBTYPE = "Shock";
        public static final int DEFAULT_INTENSITY = 50;
        public static final int DEFAULT_DURATION_MILLIS = 500;

        public OscActionCapabilities {
            Set<String> normalized = new LinkedHashSet<>();
            List<String> sanitized = new ArrayList<>();
            if (subtypes != null) {
                for (String subtype : subtypes) {
                    String trimmed = subtype == null ? "" : subtype.trim();
                    if (!trimmed.isEmpty()
                            && trimmed.length() <= 32
                            && !trimmed.contains(",")
                            && !trimmed.contains("|")
                            && normalized.add(trimmed.toLowerCase(Locale.ROOT))) {
                        sanitized.add(trimmed);
                    }
                }
            }
            subtypes = List.copyOf(sanitized);
        }

        public static OscActionCapabilities unavailable() {
            return new OscActionCapabilities(false, List.of(), 0, 0, 0, 0);
        }

        public boolean available() {
            return online
                    && !subtypes.isEmpty()
                    && minIntensity >= 0
                    && maxIntensity >= minIntensity
                    && minDurationMillis >= 0
                    && maxDurationMillis >= minDurationMillis;
        }

        public String normalizeSubtype(String subtype) {
            for (String availableSubtype : subtypes) {
                if (availableSubtype.equalsIgnoreCase(subtype == null ? "" : subtype.trim())) {
                    return availableSubtype;
                }
            }
            for (String availableSubtype : subtypes) {
                if (availableSubtype.equalsIgnoreCase(DEFAULT_SUBTYPE)) {
                    return availableSubtype;
                }
            }
            return subtypes.isEmpty() ? DEFAULT_SUBTYPE : subtypes.get(0);
        }

        public int clampIntensity(int intensity) {
            if (!available()) {
                return Math.max(0, intensity);
            }
            return Math.max(minIntensity, Math.min(maxIntensity, intensity));
        }

        public int clampDurationMillis(int durationMillis) {
            if (!available()) {
                return Math.max(0, durationMillis);
            }
            return Math.max(minDurationMillis, Math.min(maxDurationMillis, durationMillis));
        }

        public String toConfigString() {
            if (!available()) {
                return "";
            }
            return String.join(",", subtypes)
                    + "|" + minIntensity
                    + "|" + maxIntensity
                    + "|" + minDurationMillis
                    + "|" + maxDurationMillis;
        }

        public static OscActionCapabilities fromConfigString(String encoded) {
            if (encoded == null || encoded.isBlank()) {
                return unavailable();
            }
            try {
                String[] fields = encoded.split("\\|", -1);
                if (fields.length != 5) {
                    return unavailable();
                }
                List<String> subtypes = List.of(fields[0].split(","));
                OscActionCapabilities capabilities = new OscActionCapabilities(
                        true,
                        subtypes,
                        Integer.parseInt(fields[1]),
                        Integer.parseInt(fields[2]),
                        Integer.parseInt(fields[3]),
                        Integer.parseInt(fields[4]));
                return capabilities.available() ? capabilities : unavailable();
            } catch (RuntimeException exception) {
                return unavailable();
            }
        }
    }

    public record FriendsResult(
            Outcome outcome,
            int statusCode,
            String retryAfter,
            List<PuppyFriend> friends) {
        public FriendsResult {
            friends = List.copyOf(friends);
        }

        public static FriendsResult success(List<PuppyFriend> friends) {
            return new FriendsResult(Outcome.SUCCESS, 200, "", friends);
        }

        public static FriendsResult invalidRequest() {
            return fromOutcome(Outcome.INVALID_REQUEST);
        }

        public static FriendsResult invalidResponse() {
            return fromOutcome(Outcome.INVALID_RESPONSE);
        }

        public static FriendsResult rateLimited(String retryAfter) {
            return new FriendsResult(Outcome.RATE_LIMITED, 429, retryAfter, List.of());
        }

        public static FriendsResult httpError(int statusCode) {
            return new FriendsResult(Outcome.HTTP_ERROR, statusCode, "", List.of());
        }

        public static FriendsResult fromOutcome(Outcome outcome) {
            return new FriendsResult(outcome, 0, "", List.of());
        }
    }
}
