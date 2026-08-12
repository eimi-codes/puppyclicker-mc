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
import java.util.List;
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

    /**
     * Sends the configured OSC shock action to the authenticated player's own devices.
     *
     * <p>The self-action endpoint deliberately receives neither a friend identifier nor the
     * friend-only {@code message} and {@code integration} fields.</p>
     */
    public static CompletableFuture<ClickResult> sendSelfShock(String apiKey) {
        return sendAction(apiKey, SELF_ACTION_URI, selfShockRequestBody());
    }

    static String selfShockRequestBody() {
        JsonObject body = new JsonObject();
        body.addProperty("type", "osc");
        return body.toString();
    }

    public static CompletableFuture<ValidationResult> validateApiKey(String apiKey) {
        final HttpRequest request;
        try {
            request = requestBuilder(apiKey, ME_URI).GET().build();
        } catch (IllegalArgumentException exception) {
            return CompletableFuture.completedFuture(ValidationResult.invalidRequest());
        }

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .handle(PuppyClickerApi::toValidationResult);
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
            HttpResponse<Void> response,
            Throwable throwable) {
        if (throwable != null) {
            return ValidationResult.fromOutcome(networkOutcome(throwable));
        }

        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300) {
            return ValidationResult.success(statusCode);
        }
        if (statusCode == 429) {
            return ValidationResult.rateLimited(retryAfter(response));
        }
        return ValidationResult.httpError(statusCode);
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

    public record ValidationResult(Outcome outcome, int statusCode, String retryAfter) {
        public static ValidationResult success(int statusCode) {
            return new ValidationResult(Outcome.SUCCESS, statusCode, "");
        }

        public static ValidationResult invalidRequest() {
            return fromOutcome(Outcome.INVALID_REQUEST);
        }

        public static ValidationResult rateLimited(String retryAfter) {
            return new ValidationResult(Outcome.RATE_LIMITED, 429, retryAfter);
        }

        public static ValidationResult httpError(int statusCode) {
            return new ValidationResult(Outcome.HTTP_ERROR, statusCode, "");
        }

        public static ValidationResult fromOutcome(Outcome outcome) {
            return new ValidationResult(outcome, 0, "");
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
