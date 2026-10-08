package net.jarjar.jarjarmod.component;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.jarjar.jarjarmod.component.powersuppression.SuppressionComponent;
import net.jarjar.jarjarmod.component.powersuppression.SuppressionComponentImpl;
import net.minecraft.util.Identifier;

public class ModComponents implements EntityComponentInitializer {

    public static final ComponentKey<PlayerDomainComponent> DOMAIN =
            ComponentRegistry.getOrCreate(
                    Identifier.of("jarjarmod", "domain"),
                    PlayerDomainComponent.class
            );
    public static final ComponentKey<SuppressionComponent> SUPPRESSION =
            ComponentRegistry.getOrCreate(
                    Identifier.of("jarjarmod", "suppression"),
                    SuppressionComponent.class
            );

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(
                DOMAIN,
                PlayerDomainComponent::new,
                RespawnCopyStrategy.ALWAYS_COPY
        );
        registry.registerForPlayers(
                SUPPRESSION,
                SuppressionComponentImpl::new,
                RespawnCopyStrategy.ALWAYS_COPY
        );
    }
}
