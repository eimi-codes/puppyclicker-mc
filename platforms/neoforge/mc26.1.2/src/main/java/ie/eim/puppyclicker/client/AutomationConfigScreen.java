package ie.eim.puppyclicker.client;

import ie.eim.puppyclicker.config.PuppyClickerConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Accessible, explicitly opt-in controls for gameplay-triggered PuppyClicker actions. */
public final class AutomationConfigScreen extends Screen {
    private static final int CONTENT_WIDTH = 360;

    private final Screen parent;
    private boolean clickOnAdvancement;
    private boolean shockOnDamage;
    private IntSlider cooldownSlider;

    public AutomationConfigScreen(Screen parent) {
        super(Component.translatable("screen.puppyclicker.automations.title"));
        this.parent = parent;
        this.clickOnAdvancement = PuppyClickerConfig.clickOnAdvancement();
        this.shockOnDamage = PuppyClickerConfig.shockOnDamage();
    }

    @Override
    protected void init() {
        int contentWidth = Math.min(CONTENT_WIDTH, this.width - 24);
        int left = (this.width - contentWidth) / 2;
        int halfWidth = (contentWidth - 6) / 2;

        this.addRenderableWidget(CycleButton.onOffBuilder(clickOnAdvancement).create(
                left,
                72,
                halfWidth,
                20,
                Component.translatable("screen.puppyclicker.automations.advancement_clicks"),
                (button, value) -> clickOnAdvancement = value));
        this.addRenderableWidget(CycleButton.onOffBuilder(shockOnDamage).create(
                left + halfWidth + 6,
                72,
                halfWidth,
                20,
                Component.translatable("screen.puppyclicker.automations.damage_shocks"),
                (button, value) -> {
                    shockOnDamage = value;
                    cooldownSlider.active = value;
                }));

        cooldownSlider = this.addRenderableWidget(new IntSlider(
                left,
                102,
                contentWidth,
                15,
                300,
                PuppyClickerConfig.damageShockCooldownSeconds()));
        cooldownSlider.active = shockOnDamage;

        this.addRenderableWidget(Button.builder(
                        Component.translatable("screen.puppyclicker.automations.save"),
                        button -> saveAndClose())
                .bounds(this.width / 2 - 100, this.height - 27, 98, 20)
                .build());
        this.addRenderableWidget(Button.builder(
                        Component.translatable("gui.cancel"),
                        button -> onClose())
                .bounds(this.width / 2 + 2, this.height - 27, 98, 20)
                .build());
    }

    private void saveAndClose() {
        PuppyClickerConfig.saveAutomationSettings(
                clickOnAdvancement,
                shockOnDamage,
                cooldownSlider.intValue());
        onClose();
    }

    @Override
    public Component getNarrationMessage() {
        return Component.empty()
                .append(this.title)
                .append(". ")
                .append(Component.translatable("screen.puppyclicker.automations.safety_notice"));
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int contentWidth = Math.min(CONTENT_WIDTH, this.width - 24);
        graphics.centeredText(this.font, this.title, this.width / 2, 16, 0xFFFFFF);
        MultiLineLabel.create(
                        this.font,
                        Component.translatable("screen.puppyclicker.automations.description")
                                .withColor(0xB0B0B0),
                        contentWidth)
                .visitLines(TextAlignment.CENTER, this.width / 2, 34, 9, graphics.textRenderer());
        MultiLineLabel.create(
                        this.font,
                        Component.translatable("screen.puppyclicker.automations.safety_notice")
                                .withColor(0xA0A0A0),
                        contentWidth)
                .visitLines(TextAlignment.CENTER, this.width / 2, 136, 9, graphics.textRenderer());
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static final class IntSlider extends AbstractSliderButton {
        private final int min;
        private final int max;

        private IntSlider(int x, int y, int width, int min, int max, int initialValue) {
            super(
                    x,
                    y,
                    width,
                    20,
                    Component.empty(),
                    (double) (initialValue - min) / (max - min));
            this.min = min;
            this.max = max;
            updateMessage();
        }

        private int intValue() {
            return min + (int) Math.round(value * (max - min));
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.translatable(
                    "screen.puppyclicker.automations.cooldown", intValue()));
        }

        @Override
        protected void applyValue() {
            // The normalized slider value is read only when Save & Done is chosen.
        }
    }
}
