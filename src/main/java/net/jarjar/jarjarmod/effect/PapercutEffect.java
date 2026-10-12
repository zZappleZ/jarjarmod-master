package net.jarjar.jarjarmod.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class PapercutEffect extends StatusEffect {

    public PapercutEffect() {
        super(StatusEffectCategory.HARMFUL, 0xE6DCC8);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        int level = amplifier + 1;
        int interval = Math.max(1, 10 / level);

        return duration % interval == 0;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        if (entity.getWorld().isClient) {
            return;
        }

        if (!isTouchingAnyFluid(entity)) {
            return;
        }

        entity.damage(
                ModDamageSources.create(entity, ModDamageTypes.PAPERCUT),
                0.5F
        );
    }

    private static boolean isTouchingAnyFluid(LivingEntity entity) {
        World world = entity.getWorld();
        Box box = entity.getBoundingBox();

        int minX = MathHelper.floor(box.minX);
        int minY = MathHelper.floor(box.minY);
        int minZ = MathHelper.floor(box.minZ);

        int maxX = MathHelper.floor(box.maxX);
        int maxY = MathHelper.floor(box.maxY);
        int maxZ = MathHelper.floor(box.maxZ);

        for (BlockPos pos : BlockPos.iterate(
                minX, minY, minZ,
                maxX, maxY, maxZ
        )) {
            if (!world.getFluidState(pos).isEmpty()) {
                return true;
            }
        }

        return false;
    }
}