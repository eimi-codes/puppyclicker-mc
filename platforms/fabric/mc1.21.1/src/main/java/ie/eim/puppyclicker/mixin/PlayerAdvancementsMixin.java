package ie.eim.puppyclicker.mixin;

import ie.eim.puppyclicker.network.AutomationTriggerPayload;
import ie.eim.puppyclicker.network.AutomationTriggerPayload.AutomationTrigger;
import ie.eim.puppyclicker.network.ModNetworking;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Emits only newly completed, toast-visible advancements. */
@Mixin(PlayerAdvancements.class)
abstract class PlayerAdvancementsMixin {
    @Shadow private ServerPlayer player;
    @Shadow public abstract AdvancementProgress getOrStartProgress(AdvancementHolder advancement);

    @Inject(method = "award", at = @At("RETURN"))
    private void puppyclicker$afterAward(
            AdvancementHolder advancement,
            String criterion,
            CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValueZ()
                && getOrStartProgress(advancement).isDone()
                && advancement.value().display().filter(display -> display.shouldShowToast()).isPresent()) {
            ModNetworking.sendToPlayer(
                    player,
                    new AutomationTriggerPayload(AutomationTrigger.ADVANCEMENT));
        }
    }
}
