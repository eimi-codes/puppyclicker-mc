package ie.eim.puppyclicker.client;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import ie.eim.puppyclicker.api.PuppyClickerApi;
import ie.eim.puppyclicker.api.PuppyClickerApi.FriendsResult;
import ie.eim.puppyclicker.api.PuppyClickerApi.PuppyFriend;
import ie.eim.puppyclicker.component.BoundFriend;
import ie.eim.puppyclicker.config.PuppyClickerConfig;
import ie.eim.puppyclicker.network.BindClickerPayload;
import ie.eim.puppyclicker.network.ModNetworking;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
    private Component status = new TranslatableComponent("screen.puppyclicker.friends.loading");
    private boolean loadStarted;
    private boolean loading;
    private int page;

    public FriendPickerScreen(InteractionHand hand, BoundFriend currentFriend) {
        super(new TranslatableComponent("screen.puppyclicker.friends.title"));
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
                    ? new TranslatableComponent("screen.puppyclicker.friends.current", friend.username())
                    : new TextComponent(friend.username());
            this.addRenderableWidget(new Button(
                    left, 54 + row * 24, contentWidth, 20, label, button -> bind(friend)));
        }

        int controlsY = this.height - 52;
        if (page > 0) {
            this.addRenderableWidget(new Button(
                    left, controlsY, 98, 20,
                    new TranslatableComponent("screen.puppyclicker.friends.previous"),
                    button -> {
                        page--;
                        rebuildWidgets();
                    }));
        }
        if (lastIndex < friends.size()) {
            this.addRenderableWidget(new Button(
                    left + contentWidth - 98, controlsY, 98, 20,
                    new TranslatableComponent("screen.puppyclicker.friends.next"),
                    button -> {
                        page++;
                        rebuildWidgets();
                    }));
        }
        if (!loading) {
            this.addRenderableWidget(new Button(
                    this.width / 2 - 49, controlsY, 98, 20,
                    new TranslatableComponent("screen.puppyclicker.friends.refresh"),
                    button -> refresh()));
        }
        this.addRenderableWidget(new Button(
                this.width / 2 - 100, this.height - 27, 200, 20,
                new TranslatableComponent("gui.cancel"), button -> onClose()));

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

    private void rebuildWidgets() {
        clearWidgets();
        init();
    }

    private void refresh() {
        if (this.minecraft == null) {
            return;
        }
        String apiKey = PuppyClickerConfig.apiKey().trim();
        if (apiKey.isEmpty()) {
            loading = false;
            status = new TranslatableComponent("message.puppyclicker.missing_api_key");
            rebuildWidgets();
            return;
        }
        if (!apiKey.startsWith("pak_")) {
            loading = false;
            status = new TranslatableComponent("message.puppyclicker.invalid_api_key_format");
            rebuildWidgets();
            return;
        }

        loading = true;
        friends = List.of();
        page = 0;
        status = new TranslatableComponent("screen.puppyclicker.friends.loading");
        rebuildWidgets();

        PuppyClickerApi.fetchFriends(apiKey).whenComplete((result, throwable) ->
                // Futures complete off-thread; Screen state is only mutated on the client thread.
                this.minecraft.execute(() -> {
                    if (this.minecraft.screen != this) {
                        return;
                    }
                    loading = false;
                    if (throwable != null) {
                        status = new TranslatableComponent("message.puppyclicker.network_error");
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
                    ? new TranslatableComponent("screen.puppyclicker.friends.empty")
                    : new TranslatableComponent("screen.puppyclicker.friends.choose");
            return;
        }

        status = switch (result.outcome()) {
            case INVALID_REQUEST -> new TranslatableComponent("message.puppyclicker.invalid_api_key_format");
            case INVALID_RESPONSE -> new TranslatableComponent("message.puppyclicker.invalid_response");
            case RATE_LIMITED -> result.retryAfter().isBlank()
                    ? new TranslatableComponent("message.puppyclicker.rate_limited")
                    : new TranslatableComponent("message.puppyclicker.rate_limited_retry", result.retryAfter());
            case HTTP_ERROR -> new TranslatableComponent(
                    "message.puppyclicker.http_error", result.statusCode());
            case TIMEOUT -> new TranslatableComponent("message.puppyclicker.timeout");
            case NETWORK_UNAVAILABLE -> new TranslatableComponent("message.puppyclicker.network_unavailable");
            case NETWORK_ERROR -> new TranslatableComponent("message.puppyclicker.network_error");
            case SUCCESS -> new TranslatableComponent("screen.puppyclicker.friends.choose");
        };
    }

    private void bind(PuppyFriend friend) {
        // The server authoritatively applies binding data to the item currently in this hand.
        ModNetworking.sendToServer(new BindClickerPayload(
                hand == InteractionHand.MAIN_HAND,
                friend.id(),
                friend.username()));
        onClose();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        // Screen.render draws the blurred background and widgets. Draw static copy afterwards;
        // otherwise a second background pass blurs this text while leaving buttons sharp.
        super.render(poseStack, mouseX, mouseY, partialTick);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 16, 0xFFFFFF);
        drawCenteredString(poseStack, this.font, this.status, this.width / 2, 34, 0xB0B0B0);
        int contentWidth = Math.min(360, this.width - 32);
        MultiLineLabel.create(
                        this.font,
                        new TranslatableComponent("screen.puppyclicker.friends.binding_notice"),
                        contentWidth)
                .renderCentered(poseStack, this.width / 2, this.height - 79, 9, 0x808080);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
