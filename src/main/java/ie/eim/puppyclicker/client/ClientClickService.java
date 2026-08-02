package ie.eim.puppyclicker.client;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import ie.eim.puppyclicker.api.PuppyClickerApi;
import ie.eim.puppyclicker.api.PuppyClickerApi.ClickResult;
import ie.eim.puppyclicker.component.BoundFriend;
import ie.eim.puppyclicker.config.PuppyClickerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Coordinates outgoing click actions and their action-bar feedback.
 *
 * <p>The atomic guard prevents double submissions from rapid key/item use. HTTP completion may
 * occur on a JDK worker thread, so every Minecraft UI change is scheduled onto the client thread.</p>
 */
public final class ClientClickService {
    private static final long LOCAL_COOLDOWN_MILLIS = 1_000;
    private static final AtomicBoolean REQUEST_IN_FLIGHT = new AtomicBoolean();
    private static final AtomicLong NEXT_REQUEST_AT_MILLIS = new AtomicLong();

    private ClientClickService() {
    }

    public static void sendSelfClick() {
        sendClick(
                () -> PuppyClickerApi.sendSelfClick(PuppyClickerConfig.apiKey().trim()),
                Component.translatable("message.puppyclicker.click_sent"));
    }

    public static void sendFriendClick(BoundFriend friend) {
        sendClick(
                () -> PuppyClickerApi.sendFriendClick(
                        PuppyClickerConfig.apiKey().trim(), friend.id()),
                Component.translatable("message.puppyclicker.friend_click_sent", friend.name()));
    }

    private static void sendClick(
            Supplier<CompletableFuture<ClickResult>> request,
            Component successMessage) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        String apiKey = PuppyClickerConfig.apiKey().trim();
        if (apiKey.isEmpty()) {
            showActionBar(client, Component.translatable("message.puppyclicker.missing_api_key"));
            return;
        }
        if (!apiKey.startsWith("pak_")) {
            showActionBar(client, Component.translatable("message.puppyclicker.invalid_api_key_format"));
            return;
        }

        long remainingMillis = NEXT_REQUEST_AT_MILLIS.get() - System.currentTimeMillis();
        if (remainingMillis > 0) {
            long remainingSeconds = Math.max(1, TimeUnit.MILLISECONDS.toSeconds(remainingMillis) + 1);
            showActionBar(client, Component.translatable(
                    "message.puppyclicker.cooldown", remainingSeconds));
            return;
        }
        if (!REQUEST_IN_FLIGHT.compareAndSet(false, true)) {
            showActionBar(client, Component.translatable("message.puppyclicker.request_in_flight"));
            return;
        }

        final CompletableFuture<ClickResult> future;
        try {
            future = request.get();
        } catch (RuntimeException exception) {
            REQUEST_IN_FLIGHT.set(false);
            showActionBar(client, Component.translatable("message.puppyclicker.network_error"));
            return;
        }

        future.whenComplete((result, throwable) -> {
            // Release the cross-thread request guard before scheduling client-only UI work.
            REQUEST_IN_FLIGHT.set(false);
            client.execute(() -> {
                if (client.player == null) {
                    return;
                }
                if (throwable != null) {
                    showActionBar(client, Component.translatable("message.puppyclicker.network_error"));
                    return;
                }

                if (result.outcome() == PuppyClickerApi.Outcome.SUCCESS) {
                    NEXT_REQUEST_AT_MILLIS.set(
                            System.currentTimeMillis() + LOCAL_COOLDOWN_MILLIS);
                    // TODO: Append this action to recent-click history when that screen is added.
                    showActionBar(client, successMessage);
                    return;
                }
                if (result.outcome() == PuppyClickerApi.Outcome.RATE_LIMITED) {
                    applyRetryAfter(result.retryAfter());
                }
                showActionBar(client, resultMessage(result));
            });
        });
    }

    private static void applyRetryAfter(String retryAfter) {
        long now = System.currentTimeMillis();
        try {
            long seconds = Math.max(1, Long.parseLong(retryAfter));
            NEXT_REQUEST_AT_MILLIS.set(now + TimeUnit.SECONDS.toMillis(seconds));
            return;
        } catch (NumberFormatException ignored) {
            // RFC 9110 also permits an HTTP date instead of a delay in seconds.
        }

        try {
            long retryAt = ZonedDateTime.parse(retryAfter, DateTimeFormatter.RFC_1123_DATE_TIME)
                    .toInstant()
                    .toEpochMilli();
            NEXT_REQUEST_AT_MILLIS.set(Math.max(now + LOCAL_COOLDOWN_MILLIS, retryAt));
        } catch (DateTimeParseException ignored) {
            NEXT_REQUEST_AT_MILLIS.set(now + LOCAL_COOLDOWN_MILLIS);
        }
    }

    static Component resultMessage(ClickResult result) {
        return switch (result.outcome()) {
            case SUCCESS -> Component.translatable("message.puppyclicker.click_sent");
            case INVALID_REQUEST -> Component.translatable("message.puppyclicker.invalid_api_key_format");
            case INVALID_RESPONSE -> Component.translatable("message.puppyclicker.invalid_response");
            case RATE_LIMITED -> result.retryAfter().isBlank()
                    ? Component.translatable("message.puppyclicker.rate_limited")
                    : Component.translatable("message.puppyclicker.rate_limited_retry", result.retryAfter());
            case HTTP_ERROR -> Component.translatable(
                    "message.puppyclicker.http_error", result.statusCode());
            case TIMEOUT -> Component.translatable("message.puppyclicker.timeout");
            case NETWORK_UNAVAILABLE -> Component.translatable(
                    "message.puppyclicker.network_unavailable");
            case NETWORK_ERROR -> Component.translatable("message.puppyclicker.network_error");
        };
    }

    static void showActionBar(Minecraft client, Component message) {
        if (client.player != null) {
            client.player.displayClientMessage(message, true);
        }
    }
}
