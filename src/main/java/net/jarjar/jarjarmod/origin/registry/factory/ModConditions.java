package net.jarjar.jarjarmod.origin.registry.factory;

import io.github.apace100.apoli.registry.ApoliRegistries;
import net.jarjar.jarjarmod.origin.condition.entity.KeyPressedCondition;
import net.minecraft.registry.Registry;

public class ModConditions {

    public static void register() {

        Registry.register(
                ApoliRegistries.ENTITY_CONDITION,
                KeyPressedCondition.FACTORY.getSerializerId(),
                KeyPressedCondition.FACTORY
        );
    }
}
