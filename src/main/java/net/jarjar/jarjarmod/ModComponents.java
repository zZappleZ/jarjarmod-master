package net.jarjar.jarjarmod;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.jarjar.jarjarmod.component.PlayerDomainComponent;
import net.minecraft.util.Identifier;

public class ModComponents implements EntityComponentInitializer {
    public static final ComponentKey<PlayerDomainComponent> DOMAIN =
            ComponentRegistry.getOrCreate(
                    Identifier.of("jarjarmod", "domain"),
                    PlayerDomainComponent.class
            );

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(
                DOMAIN,
                PlayerDomainComponent::new,
                RespawnCopyStrategy.ALWAYS_COPY
        );
    }
}
