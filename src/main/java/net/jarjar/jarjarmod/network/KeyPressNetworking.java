package net.jarjar.jarjarmod.network;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.Active;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jarjar.jarjarmod.component.ModEntityComponents;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public class KeyPressNetworking {

    public static final Identifier ADD_KEY_TO_CHECK =
            new Identifier("archivist", "add_key_to_check");

    public static final Identifier UPDATE_KEYS_PRESSED =
            new Identifier("archivist", "update_keys_pressed");

    public static void registerServer() {

        ServerPlayNetworking.registerGlobalReceiver(
                UPDATE_KEYS_PRESSED,
                (server, player, handler, buf, responseSender) -> {

                    Set<Active.Key> addedKeys = readKeys(buf);
                    Set<Active.Key> removedKeys = readKeys(buf);

                    server.execute(() -> {

                        var component =
                                ModEntityComponents.KEY_PRESS_COMPONENT.get(player);

                        addedKeys.forEach(component::addKey);
                        removedKeys.forEach(component::removeKey);

                        component.setPreviouslyUsedKeys();
                    });
                }
        );
    }

    private static Set<Active.Key> readKeys(PacketByteBuf buf) {

        Set<Active.Key> keys = new HashSet<>();

        int size = buf.readInt();

        for (int i = 0; i < size; i++) {
            keys.add(ApoliDataTypes.KEY.receive(buf));
        }

        return keys;
    }

    public static void sendAddKeyToCheck(
            ServerPlayerEntity player,
            Active.Key key
    ) {

        PacketByteBuf buf =
                net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();

        buf.writeInt(player.getId());

        ApoliDataTypes.KEY.send(buf, key);

        ServerPlayNetworking.send(
                player,
                ADD_KEY_TO_CHECK,
                buf
        );
    }

    public static void sendUpdatedKeys(
            Set<Active.Key> addedKeys,
            Set<Active.Key> removedKeys
    ) {

        PacketByteBuf buf =
                net.fabricmc.fabric.api.networking.v1.PacketByteBufs.create();

        writeKeys(buf, addedKeys);
        writeKeys(buf, removedKeys);

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                UPDATE_KEYS_PRESSED,
                buf
        );
    }

    private static void writeKeys(
            PacketByteBuf buf,
            Set<Active.Key> keys
    ) {

        buf.writeInt(keys.size());

        for (Active.Key key : keys) {
            ApoliDataTypes.KEY.send(buf, key);
        }
    }
}
