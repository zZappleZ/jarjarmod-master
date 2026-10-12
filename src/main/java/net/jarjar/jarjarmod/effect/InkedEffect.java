package net.jarjar.jarjarmod.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class InkedEffect extends StatusEffect {

    public InkedEffect() {
        super(StatusEffectCategory.HARMFUL, 0x171717);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getWorld().isClient) {
            return;
        }

        // Water immediately removes Inked and its associated blindness.
        if (entity.isSubmergedInWater()) {
            entity.removeStatusEffect(this);
            entity.removeStatusEffect(StatusEffects.BLINDNESS);
            return;
        }

        // Refresh Blindness so it remains active while Inked is active.
        entity.addStatusEffect(new StatusEffectInstance(
                StatusEffects.BLINDNESS,
                30,
                0,
                false,
                false,
                false
        ));

        // Deal damage every 30 ticks.
        int duration = entity.getStatusEffect(this) != null
                ? entity.getStatusEffect(this).getDuration()
                : 0;

        if (duration % 50 == 0) {
            int level = amplifier + 1;
            float damage = 1.5F + 1.5F * level;

            entity.damage(entity.getDamageSources().magic(), damage);
        }
    }
}
