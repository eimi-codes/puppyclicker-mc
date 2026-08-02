package ie.eim.puppyclicker.client;

import java.util.concurrent.atomic.AtomicBoolean;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import ie.eim.puppyclicker.api.PuppyClickerApi;
import ie.eim.puppyclicker.api.PuppyClickerApi.ClickResult;
import ie.eim.puppyclicker.config.PuppyClickerConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public final class ClientEvents {
    private static final KeyMapping SEND_SELF_CLICK = new KeyMapping(
            "key.puppyclicker.send_self_click",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "key.categories.puppyclicker");
    private static final AtomicBoolean REQUEST_IN_FLIGHT = new AtomicBoolean();

    private ClientEvents() {
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(SEND_SELF_CLICK);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        while (SEND_SELF_CLICK.consumeClick()) {
            sendSelfClick();
        }
    }

    private static void sendSelfClick() {
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
        if (!REQUEST_IN_FLIGHT.compareAndSet(false, true)) {
            showActionBar(client, Component.translatable("message.puppyclicker.request_in_flight"));
            return;
        }

        PuppyClickerApi.sendSelfClick(apiKey).whenComplete((result, throwable) ->
                client.execute(() -> {
                    REQUEST_IN_FLIGHT.set(false);
                    if (client.player == null) {
                        return;
                    }

                    if (throwable != null) {
                        showActionBar(client, Component.translatable("message.puppyclicker.network_error"));
                        return;
                    }
                    showResult(client, result);
                }));
    }

    private static void showResult(Minecraft client, ClickResult result) {
        Component message = switch (result.outcome()) {
            case SUCCESS -> Component.translatable("message.puppyclicker.click_sent");
            case INVALID_REQUEST -> Component.translatable("message.puppyclicker.invalid_api_key_format");
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
        showActionBar(client, message);
    }

    private static void showActionBar(Minecraft client, Component message) {
        if (client.player != null) {
            client.player.displayClientMessage(message, true);
        }
    }
}
