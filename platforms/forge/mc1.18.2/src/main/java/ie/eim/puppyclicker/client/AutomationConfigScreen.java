package ie.eim.puppyclicker.client;

import ie.eim.puppyclicker.api.PuppyClickerApi.OscActionCapabilities;
import ie.eim.puppyclicker.config.PuppyClickerConfig;
import ie.eim.puppyclicker.shared.DamageTriggerMode;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

/** Accessible, explicitly opt-in controls for gameplay-triggered PuppyClicker actions. */
public final class AutomationConfigScreen extends Screen {
    private static final int CONTENT_WIDTH = 360;

    private final Screen parent;
    private final OscActionCapabilities capabilities;
    private boolean clickOnAdvancement;
    private boolean shockOnDamage;
    private DamageTriggerMode damageTriggerMode;
    private String actionType;
    private Button actionTypeButton;
    private Button damageTriggerModeButton;
    private IntSlider intensitySlider;
    private IntSlider durationSlider;
    private IntSlider cooldownSlider;

    public AutomationConfigScreen(Screen parent) {
        super(new TranslatableComponent("screen.puppyclicker.automations.title"));
        this.parent = parent;
        this.capabilities = PuppyClickerConfig.oscCapabilities();
        this.clickOnAdvancement = PuppyClickerConfig.clickOnAdvancement();
        this.shockOnDamage = capabilities.available() && PuppyClickerConfig.shockOnDamage();
        this.damageTriggerMode = PuppyClickerConfig.damageTriggerMode();
        this.actionType = capabilities.normalizeSubtype(PuppyClickerConfig.damageActionType());
    }

    @Override
    protected void init() {
        int contentWidth = Math.min(CONTENT_WIDTH, this.width - 24);
        int left = (this.width - contentWidth) / 2;
        int halfWidth = (contentWidth - 6) / 2;

        this.addRenderableWidget(CycleButton.onOffBuilder(clickOnAdvancement).create(
                left,
                58,
                halfWidth,
                20,
                new TranslatableComponent("screen.puppyclicker.automations.advancement_clicks"),
                (button, value) -> clickOnAdvancement = value));
        CycleButton<Boolean> damageButton = this.addRenderableWidget(
                CycleButton.onOffBuilder(shockOnDamage).create(
                left + halfWidth + 6,
                58,
                halfWidth,
                20,
                new TranslatableComponent("screen.puppyclicker.automations.damage_shocks"),
                (button, value) -> {
                    shockOnDamage = value;
                    updateActionControls();
                }));
        damageButton.active = capabilities.available();

        damageTriggerModeButton = this.addRenderableWidget(new Button(
                left, 84, halfWidth, 20, damageTriggerModeLabel(), button -> cycleDamageTriggerMode()));
        actionTypeButton = this.addRenderableWidget(new Button(
                left + halfWidth + 6, 84, halfWidth, 20,
                actionTypeLabel(), button -> cycleActionType()));
        intensitySlider = this.addRenderableWidget(new IntSlider(
                left, 110, contentWidth,
                capabilities.minIntensity(), capabilities.maxIntensity(),
                PuppyClickerConfig.damageActionIntensity(),
                "screen.puppyclicker.automations.intensity"));
        durationSlider = this.addRenderableWidget(new IntSlider(
                left, 136, contentWidth,
                capabilities.minDurationMillis(), capabilities.maxDurationMillis(),
                PuppyClickerConfig.damageActionDurationMillis(),
                "screen.puppyclicker.automations.duration"));

        cooldownSlider = this.addRenderableWidget(new IntSlider(
                left,
                162,
                contentWidth,
                15,
                300,
                PuppyClickerConfig.damageShockCooldownSeconds(),
                "screen.puppyclicker.automations.cooldown"));
        updateActionControls();

        this.addRenderableWidget(new Button(
                this.width / 2 - 100, this.height - 27, 98, 20,
                new TranslatableComponent("screen.puppyclicker.automations.save"),
                button -> saveAndClose()));
        this.addRenderableWidget(new Button(
                this.width / 2 + 2, this.height - 27, 98, 20,
                new TranslatableComponent("gui.cancel"), button -> onClose()));
    }

    private void saveAndClose() {
        PuppyClickerConfig.saveAutomationSettings(
                clickOnAdvancement,
                shockOnDamage,
                damageTriggerMode,
                cooldownSlider.intValue(),
                actionType,
                intensitySlider.intValue(),
                durationSlider.intValue());
        onClose();
    }

    private Component actionTypeLabel() {
        return new TranslatableComponent("screen.puppyclicker.automations.action_type", actionType);
    }

    private Component damageTriggerModeLabel() {
        return new TranslatableComponent(
                "screen.puppyclicker.automations.damage_mode",
                new TranslatableComponent(damageTriggerMode == DamageTriggerMode.ALL_DAMAGE
                        ? "screen.puppyclicker.automations.damage_mode.all"
                        : "screen.puppyclicker.automations.damage_mode.clicker_holder"));
    }

    private void cycleDamageTriggerMode() {
        damageTriggerMode = damageTriggerMode == DamageTriggerMode.ALL_DAMAGE
                ? DamageTriggerMode.ATTACKER_CARRIES_MY_CLICKER
                : DamageTriggerMode.ALL_DAMAGE;
        damageTriggerModeButton.setMessage(damageTriggerModeLabel());
    }

    private void cycleActionType() {
        int current = capabilities.subtypes().indexOf(actionType);
        actionType = capabilities.subtypes().get((current + 1) % capabilities.subtypes().size());
        actionTypeButton.setMessage(actionTypeLabel());
    }

    private void updateActionControls() {
        boolean active = shockOnDamage && capabilities.available();
        damageTriggerModeButton.active = active;
        actionTypeButton.active = active;
        intensitySlider.active = active;
        durationSlider.active = active;
        cooldownSlider.active = active;
    }

    @Override
    public Component getNarrationMessage() {
        return new TextComponent("")
                .append(this.title)
                .append(". ")
                .append(new TranslatableComponent(capabilities.available()
                        ? "screen.puppyclicker.automations.safety_notice"
                        : "screen.puppyclicker.automations.no_osc"));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        super.render(poseStack, mouseX, mouseY, partialTick);
        int contentWidth = Math.min(CONTENT_WIDTH, this.width - 24);
        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 16, 0xFFFFFF);
        MultiLineLabel.create(
                        this.font,
                        new TranslatableComponent("screen.puppyclicker.automations.description"),
                        contentWidth)
                .renderCentered(poseStack, this.width / 2, 34, 9, 0xB0B0B0);
        MultiLineLabel.create(
                        this.font,
                        new TranslatableComponent(capabilities.available()
                                ? "screen.puppyclicker.automations.safety_notice"
                                : "screen.puppyclicker.automations.no_osc"),
                        contentWidth)
                .renderCentered(poseStack, this.width / 2, 188, 9, 0xA0A0A0);
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
        private final String messageKey;

        private IntSlider(
                int x, int y, int width, int min, int max, int initialValue, String messageKey) {
            super(
                    x,
                    y,
                    width,
                    20,
                    new TextComponent(""),
                    max == min ? 0.0 : (double) (initialValue - min) / (max - min));
            this.min = min;
            this.max = max;
            this.messageKey = messageKey;
            updateMessage();
        }

        private int intValue() {
            return min + (int) Math.round(value * (max - min));
        }

        @Override
        protected void updateMessage() {
            setMessage(new TranslatableComponent(messageKey, intValue()));
        }

        @Override
        protected void applyValue() {
            // The normalized slider value is read only when Save & Done is chosen.
        }
    }
}
