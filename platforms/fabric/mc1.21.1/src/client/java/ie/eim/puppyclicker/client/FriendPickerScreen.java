package ie.eim.puppyclicker.client;

import java.util.List;

import ie.eim.puppyclicker.api.PuppyClickerApi;
import ie.eim.puppyclicker.api.PuppyClickerApi.FriendsResult;
import ie.eim.puppyclicker.api.PuppyClickerApi.PuppyFriend;
import ie.eim.puppyclicker.component.BoundFriend;
import ie.eim.puppyclicker.config.PuppyClickerConfig;
import ie.eim.puppyclicker.network.BindClickerPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;

/**
 * Asynchronously lists accepted PuppyClicker friends and binds one to the held item.
 *
 * <p>Only the selected friend's public ID/name crosses the Minecraft connection. The API key and
 * the friends response remain client-side.</p>
 *
 * <p>TODO: Add search/filter controls if larger friend lists make pagination cumbersome.</p>
 */
public final class FriendPickerScreen extends Screen {
    private final InteractionHand hand;
    private final BoundFriend currentFriend;
    private List<PuppyFriend> friends = List.of();
    private Component status = Component.translatable("screen.puppyclicker.friends.loading");
    private boolean loadStarted;
    private boolean loading;
    private int page;

    public FriendPickerScreen(InteractionHand hand, BoundFriend currentFriend) {
        super(Component.translatable("screen.puppyclicker.friends.title"));
        this.hand = hand;
        this.currentFriend = currentFriend;
    }

    @Override
    protected void init() {
        int contentWidth = Math.min(320, this.width - 32);
        int left = (this.width - contentWidth) / 2;
        int pageSize = pageSize();
        int firstIndex = page * pageSize;
        int lastIndex = Math.min(friends.size(), firstIndex + pageSize);

        for (int index = firstIndex; index < lastIndex; index++) {
            PuppyFriend friend = friends.get(index);
            int row = index - firstIndex;
            Component label = currentFriend != null && currentFriend.id().equals(friend.id())
                    ? Component.translatable("screen.puppyclicker.friends.current", friend.username())
                    : Component.literal(friend.username());
            this.addRenderableWidget(Button.builder(label, button -> bind(friend))
                    .bounds(left, 54 + row * 24, contentWidth, 20)
                    .build());
        }

        int controlsY = this.height - 52;
        if (page > 0) {
            this.addRenderableWidget(Button.builder(
                            Component.translatable("screen.puppyclicker.friends.previous"),
                            button -> {
                                page--;
                                rebuildWidgets();
                            })
                    .bounds(left, controlsY, 98, 20)
                    .build());
        }
        if (lastIndex < friends.size()) {
            this.addRenderableWidget(Button.builder(
                            Component.translatable("screen.puppyclicker.friends.next"),
                            button -> {
                                page++;
                                rebuildWidgets();
                            })
                    .bounds(left + contentWidth - 98, controlsY, 98, 20)
                    .build());
        }
        if (!loading) {
            this.addRenderableWidget(Button.builder(
                            Component.translatable("screen.puppyclicker.friends.refresh"),
                            button -> refresh())
                    .bounds(this.width / 2 - 49, controlsY, 98, 20)
                    .build());
        }
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.cancel"),
                        button -> onClose())
                .bounds(this.width / 2 - 100, this.height - 27, 200, 20)
                .build());

        if (!loadStarted) {
            loadStarted = true;
            if (this.minecraft != null) {
                // Defer the first refresh so rebuildWidgets does not re-enter init.
                this.minecraft.execute(() -> {
                    if (this.minecraft.screen == this) {
                        refresh();
                    }
                });
            }
        }
    }

    private int pageSize() {
        return Math.max(1, Math.min(8, (this.height - 144) / 24));
    }

    private void refresh() {
        if (this.minecraft == null) {
            return;
        }
        String apiKey = PuppyClickerConfig.apiKey().trim();
        if (apiKey.isEmpty()) {
            loading = false;
            status = Component.translatable("message.puppyclicker.missing_api_key");
            rebuildWidgets();
            return;
        }
        if (!apiKey.startsWith("pak_")) {
            loading = false;
            status = Component.translatable("message.puppyclicker.invalid_api_key_format");
            rebuildWidgets();
            return;
        }

        loading = true;
        friends = List.of();
        page = 0;
        status = Component.translatable("screen.puppyclicker.friends.loading");
        rebuildWidgets();

        PuppyClickerApi.fetchFriends(apiKey).whenComplete((result, throwable) ->
                // Futures complete off-thread; Screen state is only mutated on the client thread.
                this.minecraft.execute(() -> {
                    if (this.minecraft.screen != this) {
                        return;
                    }
                    loading = false;
                    if (throwable != null) {
                        status = Component.translatable("message.puppyclicker.network_error");
                    } else {
                        applyResult(result);
                    }
                    rebuildWidgets();
                }));
    }

    private void applyResult(FriendsResult result) {
        if (result.outcome() == PuppyClickerApi.Outcome.SUCCESS) {
            friends = result.friends();
            status = friends.isEmpty()
                    ? Component.translatable("screen.puppyclicker.friends.empty")
                    : Component.translatable("screen.puppyclicker.friends.choose");
            return;
        }

        status = switch (result.outcome()) {
            case INVALID_REQUEST -> Component.translatable("message.puppyclicker.invalid_api_key_format");
            case INVALID_RESPONSE -> Component.translatable("message.puppyclicker.invalid_response");
            case RATE_LIMITED -> result.retryAfter().isBlank()
                    ? Component.translatable("message.puppyclicker.rate_limited")
                    : Component.translatable("message.puppyclicker.rate_limited_retry", result.retryAfter());
            case HTTP_ERROR -> Component.translatable(
                    "message.puppyclicker.http_error", result.statusCode());
            case TIMEOUT -> Component.translatable("message.puppyclicker.timeout");
            case NETWORK_UNAVAILABLE -> Component.translatable("message.puppyclicker.network_unavailable");
            case NETWORK_ERROR -> Component.translatable("message.puppyclicker.network_error");
            case SUCCESS -> Component.translatable("screen.puppyclicker.friends.choose");
        };
    }

    private void bind(PuppyFriend friend) {
        // The server authoritatively applies binding data to the item currently in this hand.
        FabricClientNetworking.sendToServer(new BindClickerPayload(
                hand == InteractionHand.MAIN_HAND,
                friend.id(),
                friend.username()));
        onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Screen.render draws the blurred background and widgets. Draw static copy afterwards;
        // otherwise a second background pass blurs this text while leaving buttons sharp.
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 0xFFFFFF);
        graphics.drawCenteredString(this.font, this.status, this.width / 2, 34, 0xB0B0B0);
        int contentWidth = Math.min(360, this.width - 32);
        MultiLineLabel.create(
                        this.font,
                        Component.translatable("screen.puppyclicker.friends.binding_notice"),
                        contentWidth)
                .renderCentered(graphics, this.width / 2, this.height - 79, 9, 0x808080);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
