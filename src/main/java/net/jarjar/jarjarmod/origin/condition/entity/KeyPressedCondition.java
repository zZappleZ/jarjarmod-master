package net.jarjar.jarjarmod.origin.condition.entity;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.data.SerializableData;
import net.jarjar.jarjarmod.component.KeyPressComponent;
import net.jarjar.jarjarmod.component.ModEntityComponents;
import net.jarjar.jarjarmod.network.KeyPressNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class KeyPressedCondition {

    public static final ConditionFactory<Entity> FACTORY =
            new ConditionFactory<>(
                    new Identifier(
                            "archivist",
                            "key_pressed"
                    ),

                    new SerializableData()
                            .add(
                                    "key",
                                    ApoliDataTypes.KEY
                            ),

                    (data, entity) -> {

                        if (!(entity instanceof PlayerEntity player)) {
                            return false;
                        }

                        Active.Key key =
                                data.get("key");

                        KeyPressComponent component =
                                ModEntityComponents
                                        .KEY_PRESS_COMPONENT
                                        .get(player);

                        if (!component
                                .getKeysToCheck()
                                .contains(key)) {

                            component.addKeyToCheck(key);

                            if (!player.getWorld().isClient()
                                    && player instanceof ServerPlayerEntity serverPlayer) {

                                KeyPressNetworking
                                        .sendAddKeyToCheck(
                                                serverPlayer,
                                                key
                                        );
                            }
                        }

                        return component
                                .getCurrentlyUsedKeys()
                                .contains(key);
                    }
            );
}
