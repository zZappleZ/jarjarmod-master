package net.jarjar.jarjarmod.component;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public class ModEntityComponents implements EntityComponentInitializer {

    public static final ComponentKey<KeyPressComponent> KEY_PRESS_COMPONENT =
            ComponentRegistry.getOrCreate(
                    new Identifier("archivist", "keys_pressed"),
                    KeyPressComponent.class
            );

    @Override
    public void registerEntityComponentFactories(
            EntityComponentFactoryRegistry registry
    ) {
        registry.registerForPlayers(
                KEY_PRESS_COMPONENT,
                KeyPressComponentImpl::new,
                RespawnCopyStrategy.NEVER_COPY
        );
    }
}
