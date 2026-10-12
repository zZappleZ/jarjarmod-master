package net.jarjar.jarjarmod.effect;

import net.jarjar.jarjarmod.JarJarMod;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public final class ModDamageTypes {

    public static final RegistryKey<DamageType> BLEED = create("bleed");
    public static final RegistryKey<DamageType> PAPERCUT = create("papercut");
    public static final RegistryKey<DamageType> INKED = create("inked");

    private static RegistryKey<DamageType> create(String name) {
        return RegistryKey.of(
                RegistryKeys.DAMAGE_TYPE,
                new Identifier(JarJarMod.MOD_ID, name)
        );
    }

    private ModDamageTypes() {
    }
}