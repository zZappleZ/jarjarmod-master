package net.jarjar.jarjarmod.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;

public final class ModDamageSources {

    public static DamageSource create(
            LivingEntity target,
            RegistryKey<DamageType> key
    ) {
        RegistryEntry<DamageType> type = target.getWorld()
                .getRegistryManager()
                .get(RegistryKeys.DAMAGE_TYPE)
                .entryOf(key);

        Entity attacker = target.getAttacker();

        return new DamageSource(type, attacker, attacker);
    }

    private ModDamageSources() {
    }
}