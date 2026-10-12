package net.jarjar.jarjarmod.effect;

import net.jarjar.jarjarmod.JarJarMod;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModEffects {

    public static final StatusEffect BLEED = register(
            "bleed",
            new BleedEffect()
    );

    public static final StatusEffect PAPERCUT = register(
            "papercut",
            new PapercutEffect()
    );

    public static final StatusEffect INKED = register(
            "inked",
            new InkedEffect()
    );

    private static StatusEffect register(String name, StatusEffect effect) {
        return Registry.register(
                Registries.STATUS_EFFECT,
                new Identifier(JarJarMod.MOD_ID, name),
                effect
        );
    }

    public static void registerModEffects() {
        JarJarMod.LOGGER.info("Registering custom status effects for " + JarJarMod.MOD_ID);
    }

    private ModEffects() {
    }
}
