package net.jarjar.jarjarmod.mixin;

import io.github.apace100.apoli.power.Power;
import net.jarjar.jarjarmod.component.ModComponents;
import net.jarjar.jarjarmod.component.powersuppression.SuppressionComponent;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Power.class)
public abstract class PowerMixin {

    @Shadow
    @Final
    protected net.minecraft.entity.LivingEntity entity;

    @Inject(
            method = "isActive",
            at = @At("HEAD"),
            cancellable = true
    )
    private void jarjarmod$checkSuppression(CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof PlayerEntity player) {
            SuppressionComponent suppression = ModComponents.SUPPRESSION.get(player);

            if (suppression.isSuppressed()) {
                cir.setReturnValue(false);
            }
        }
    }
}