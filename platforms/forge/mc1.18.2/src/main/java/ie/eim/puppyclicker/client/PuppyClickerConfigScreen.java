package ie.eim.puppyclicker.client;

import ie.eim.puppyclicker.api.PuppyClickerApi;
import ie.eim.puppyclicker.api.PuppyClickerApi.ValidationResult;
import ie.eim.puppyclicker.config.PuppyClickerConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

/**
 * Credential editor exposed through Mods -> Config.
 *
 * <p>The draft remains unsaved until {@code GET /me} accepts it. The field is masked by default,
 * and its narration deliberately omits the value so screen readers cannot announce a secret.</p>
 */
public final class PuppyClickerConfigScreen extends Screen {
    private static final int CONTENT_WIDTH = 360;
    private static final int FIELD_HEIGHT = 20;

    private final Screen parent;
    private String draftApiKey;
    private Component status;
    private MaskedEditBox apiKeyField;
    private Button visibilityButton;
    private Button validateButton;
    private Button clearButton;
    private boolean masked = true;
    private boolean validating;

    public PuppyClickerConfigScreen(Screen parent) {
        super(new TranslatableComponent("screen.puppyclicker.config.title"));
        this.parent = parent;
        this.draftApiKey = PuppyClickerConfig.apiKey();
        this.status = draftApiKey.isBlank()
                ? new TranslatableComponent("screen.puppyclicker.config.status.enter_key")
                : new TranslatableComponent("screen.puppyclicker.config.status.stored");
    }

    @Override
    protected void init() {
        int contentWidth = Math.min(CONTENT_WIDTH, this.width - 32);
        int left = (this.width - contentWidth) / 2;
        int visibilityWidth = 72;

        apiKeyField = new MaskedEditBox(
                this.font,
                left,
                72,
                contentWidth - visibilityWidth - 6,
                FIELD_HEIGHT,
                new TranslatableComponent("screen.puppyclicker.config.api_key"));
        apiKeyField.setMaxLength(256);
        apiKeyField.setValue(draftApiKey);
        apiKeyField.setResponder(value -> {
            draftApiKey = value;
            updateValidateButton();
        });
        applyMaskFormatter();
        this.addRenderableWidget(apiKeyField);

        visibilityButton = this.addRenderableWidget(new Button(
                left + contentWidth - visibilityWidth, 72, visibilityWidth, FIELD_HEIGHT,
                visibilityLabel(), button -> toggleVisibility()));

        validateButton = this.addRenderableWidget(new Button(
                this.width / 2 - 100, 126, 200, 20,
                new TranslatableComponent("screen.puppyclicker.config.validate_save"),
                button -> validateAndSave()));

        clearButton = this.addRenderableWidget(new Button(
                this.width / 2 - 100, 152, 98, 20,
                new TranslatableComponent("screen.puppyclicker.config.clear"),
                button -> clearSavedKey()));

        this.addRenderableWidget(new Button(
                this.width / 2 + 2, 152, 98, 20,
                new TranslatableComponent("gui.done"), button -> onClose()));

        this.addRenderableWidget(new Button(
                this.width / 2 - 100, 178, 200, 20,
                new TranslatableComponent("screen.puppyclicker.config.automations"),
                button -> {
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(new AutomationConfigScreen(this));
                    }
                }));

        updateValidateButton();
        this.setInitialFocus(apiKeyField);
    }

    private void toggleVisibility() {
        masked = !masked;
        applyMaskFormatter();
        visibilityButton.setMessage(visibilityLabel());
    }

    private Component visibilityLabel() {
        return new TranslatableComponent(masked
                ? "screen.puppyclicker.config.show"
                : "screen.puppyclicker.config.hide");
    }

    private void applyMaskFormatter() {
        if (apiKeyField == null) {
            return;
        }
        // Formatter changes presentation only; getValue still returns the real draft for saving.
        apiKeyField.setFormatter(masked
                ? (value, offset) -> FormattedCharSequence.forward(
                        "•".repeat(value.length()), Style.EMPTY)
                : (value, offset) -> FormattedCharSequence.forward(value, Style.EMPTY));
    }

    private void updateValidateButton() {
        if (validateButton != null) {
            validateButton.active = !validating && !draftApiKey.trim().isEmpty();
        }
        if (apiKeyField != null) {
            apiKeyField.setEditable(!validating);
        }
        if (visibilityButton != null) {
            visibilityButton.active = !validating;
        }
        if (clearButton != null) {
            clearButton.active = !validating && !PuppyClickerConfig.apiKey().isBlank();
        }
    }

    private void clearSavedKey() {
        PuppyClickerConfig.saveApiKey("");
        draftApiKey = "";
        apiKeyField.setValue("");
        status = new TranslatableComponent("screen.puppyclicker.config.status.cleared")
                .withStyle(ChatFormatting.GREEN);
        updateValidateButton();
        narrateStatus();
    }

    private void validateAndSave() {
        if (this.minecraft == null || validating) {
            return;
        }

        String candidate = draftApiKey.trim();
        if (!candidate.startsWith("pak_")) {
            status = new TranslatableComponent("message.puppyclicker.invalid_api_key_format")
                    .withStyle(ChatFormatting.RED);
            narrateStatus();
            return;
        }

        validating = true;
        status = new TranslatableComponent("screen.puppyclicker.config.status.validating");
        updateValidateButton();
        narrateStatus();

        PuppyClickerApi.validateApiKey(candidate).whenComplete((result, throwable) ->
                // Validation completes on an HTTP worker; return before touching Screen state.
                this.minecraft.execute(() -> {
                    if (this.minecraft.screen != this) {
                        return;
                    }
                    validating = false;
                    if (throwable != null) {
                        status = new TranslatableComponent("message.puppyclicker.network_error")
                                .withStyle(ChatFormatting.RED);
                    } else {
                        applyValidationResult(candidate, result);
                    }
                    updateValidateButton();
                    narrateStatus();
                }));
    }

    private void applyValidationResult(String candidate, ValidationResult result) {
        status = switch (result.outcome()) {
            case SUCCESS -> {
                PuppyClickerConfig.saveApiKey(candidate);
                draftApiKey = candidate;
                yield new TranslatableComponent("screen.puppyclicker.config.status.saved")
                        .withStyle(ChatFormatting.GREEN);
            }
            case INVALID_REQUEST -> new TranslatableComponent("message.puppyclicker.invalid_api_key_format")
                    .withStyle(ChatFormatting.RED);
            case RATE_LIMITED -> result.retryAfter().isBlank()
                    ? new TranslatableComponent("message.puppyclicker.rate_limited")
                            .withStyle(ChatFormatting.RED)
                    : new TranslatableComponent("message.puppyclicker.rate_limited_retry", result.retryAfter())
                            .withStyle(ChatFormatting.RED);
            case HTTP_ERROR -> result.statusCode() == 401 || result.statusCode() == 403
                    ? new TranslatableComponent(
                                    "screen.puppyclicker.config.status.rejected",
                                    result.statusCode())
                            .withStyle(ChatFormatting.RED)
                    : new TranslatableComponent("message.puppyclicker.http_error", result.statusCode())
                            .withStyle(ChatFormatting.RED);
            case TIMEOUT -> new TranslatableComponent("message.puppyclicker.timeout")
                    .withStyle(ChatFormatting.RED);
            case NETWORK_UNAVAILABLE -> new TranslatableComponent("message.puppyclicker.network_unavailable")
                    .withStyle(ChatFormatting.RED);
            case NETWORK_ERROR, INVALID_RESPONSE -> new TranslatableComponent("message.puppyclicker.network_error")
                    .withStyle(ChatFormatting.RED);
        };
    }

    private void narrateStatus() {
        this.triggerImmediateNarration(true);
    }

    @Override
    public Component getNarrationMessage() {
        return new TextComponent("").append(this.title).append(". ").append(this.status);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        // Screen.render owns the background pass. Render it and the widgets first so the
        // background blur cannot be applied over labels drawn by this screen.
        super.render(poseStack, mouseX, mouseY, partialTick);
        int contentWidth = Math.min(CONTENT_WIDTH, this.width - 32);
        int left = (this.width - contentWidth) / 2;
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 28, 0xFFFFFF);
        drawString(
                poseStack, this.font,
                new TranslatableComponent("screen.puppyclicker.config.api_key"),
                left,
                59,
                0xA0A0A0);
        MultiLineLabel.create(this.font, this.status, contentWidth)
                .renderCentered(poseStack, this.width / 2, 100, 9, 0xFFFFFF);
        MultiLineLabel.create(
                        this.font,
                        new TranslatableComponent("screen.puppyclicker.config.privacy"),
                        contentWidth)
                .renderCentered(poseStack, this.width / 2, 207, 9, 0x808080);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class MaskedEditBox extends EditBox {
        private MaskedEditBox(
                Font font,
                int x,
                int y,
                int width,
                int height,
                Component message) {
            super(font, x, y, width, height, message);
        }

        @Override
        protected MutableComponent createNarrationMessage() {
            // Never include EditBox#getValue here: it is a bearer credential.
            return new TranslatableComponent("screen.puppyclicker.config.api_key_narration");
        }
    }
}
