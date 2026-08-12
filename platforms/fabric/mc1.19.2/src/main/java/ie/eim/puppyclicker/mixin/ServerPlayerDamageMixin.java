package ie.eim.puppyclicker.mixin;

import ie.eim.puppyclicker.network.AutomationTriggerPayload;
import ie.eim.puppyclicker.network.AutomationTriggerPayload.AutomationTrigger;
import ie.eim.puppyclicker.network.ModNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Relays successful self damage, including fatal damage, after vanilla has accepted it. */
@Mixin(ServerPlayer.class)
abstract class ServerPlayerDamageMixin {
    @Inject(method = "hurt", at = @At("RETURN"))
    private void puppyclicker$afterDamage(
            DamageSource source,
            float amount,
            CallbackInfoReturnable<Boolean> callback) {
        if (amount > 0.0F && callback.getReturnValueZ()) {
            ModNetworking.sendToPlayer(
                    (ServerPlayer) (Object) this,
                    new AutomationTriggerPayload(AutomationTrigger.DAMAGE));
        }
    }
}
