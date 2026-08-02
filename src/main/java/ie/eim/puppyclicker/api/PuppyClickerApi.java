package ie.eim.puppyclicker.api;

import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class PuppyClickerApi {
    private static final URI SELF_CLICK_URI = URI.create(
            "https://puppyclicker-api.boundfire.com/api/v2/clicks/self");
    private static final String SELF_CLICK_BODY = "{\"message\":\"Click from Minecraft\"}";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private PuppyClickerApi() {
    }

    public static CompletableFuture<ClickResult> sendSelfClick(String apiKey) {
        final HttpRequest request;
        try {
            request = HttpRequest.newBuilder(SELF_CLICK_URI)
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(SELF_CLICK_BODY))
                    .build();
        } catch (IllegalArgumentException exception) {
            return CompletableFuture.completedFuture(ClickResult.invalidRequest());
        }

        return HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .handle(PuppyClickerApi::toResult);
    }

    private static ClickResult toResult(HttpResponse<Void> response, Throwable throwable) {
        if (throwable != null) {
            Throwable cause = unwrap(throwable);
            if (cause instanceof HttpTimeoutException) {
                return ClickResult.timeout();
            }
            if (cause instanceof ConnectException || cause instanceof UnknownHostException) {
                return ClickResult.networkUnavailable();
            }
            return ClickResult.networkError();
        }

        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300) {
            return ClickResult.success(statusCode);
        }

        if (statusCode == 429) {
            String retryAfter = response.headers().firstValue("Retry-After").orElse("");
            return ClickResult.rateLimited(retryAfter);
        }

        return ClickResult.httpError(statusCode);
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
        RATE_LIMITED,
        HTTP_ERROR,
        TIMEOUT,
        NETWORK_UNAVAILABLE,
        NETWORK_ERROR
    }

    public record ClickResult(Outcome outcome, int statusCode, String retryAfter) {
        public static ClickResult success(int statusCode) {
            return new ClickResult(Outcome.SUCCESS, statusCode, "");
        }

        public static ClickResult invalidRequest() {
            return new ClickResult(Outcome.INVALID_REQUEST, 0, "");
        }

        public static ClickResult rateLimited(String retryAfter) {
            return new ClickResult(Outcome.RATE_LIMITED, 429, retryAfter);
        }

        public static ClickResult httpError(int statusCode) {
            return new ClickResult(Outcome.HTTP_ERROR, statusCode, "");
        }

        public static ClickResult timeout() {
            return new ClickResult(Outcome.TIMEOUT, 0, "");
        }

        public static ClickResult networkUnavailable() {
            return new ClickResult(Outcome.NETWORK_UNAVAILABLE, 0, "");
        }

        public static ClickResult networkError() {
            return new ClickResult(Outcome.NETWORK_ERROR, 0, "");
        }
    }
}
