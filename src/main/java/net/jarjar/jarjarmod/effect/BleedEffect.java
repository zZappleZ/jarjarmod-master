package net.jarjar.jarjarmod.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class BleedEffect extends StatusEffect {

    public BleedEffect() {
        super(StatusEffectCategory.HARMFUL, 0xA61B1B);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getWorld().isClient) {
            return;
        }

        int level = amplifier + 1;
        float damage = 3.0F + level;

        entity.damage(
                ModDamageSources.create(entity, ModDamageTypes.BLEED),
                damage
        );
    }
}