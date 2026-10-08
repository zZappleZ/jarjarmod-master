package net.jarjar.jarjarmod.client;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Active;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.jarjar.jarjarmod.component.ModEntityComponents;
import net.jarjar.jarjarmod.network.KeyPressNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class KeyPressClientNetworking {

    public static void register() {

        ClientPlayNetworking.registerGlobalReceiver(
                KeyPressNetworking.ADD_KEY_TO_CHECK,
                (client, handler, buf, responseSender) -> {

                    int entityId = buf.readInt();

                    Active.Key key =
                            ApoliDataTypes.KEY.receive(buf);

                    client.execute(() -> {

                        if (client.world == null) {
                            return;
                        }

                        Entity entity =
                                client.world.getEntityById(entityId);

                        if (!(entity instanceof PlayerEntity player)) {
                            return;
                        }

                        if (!player.isMainPlayer()) {
                            return;
                        }

                        ModEntityComponents.KEY_PRESS_COMPONENT
                                .get(player)
                                .addKeyToCheck(key);
                    });
                }
        );
    }
}
