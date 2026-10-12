
package net.jarjar.jarjarmod.component.handling;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jarjar.jarjarmod.client.TomeClientCache;
import net.jarjar.jarjarmod.component.ModPlayerData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class TomePackets {

    public static final Identifier SELECT_TOME_C2S =
            new Identifier("archivist", "select_tome");

    public static final Identifier SYNC_TOMES_S2C =
            new Identifier("archivist", "sync_tomes");

    private TomePackets() {
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(
                SELECT_TOME_C2S,
                (server, player, handler, buf, responseSender) -> {
                    int slot = buf.readInt();
                    String tomeId = buf.readString(32767);

                    server.execute(() -> {
                        if (slot < 0 || slot >= 3) {
                            return;
                        }

                        ModPlayerData data = (ModPlayerData) player;

                        // An empty ID means the player wants to clear the slot.
                        String selectedId = tomeId.isEmpty() ? null : tomeId;

                        // setTomeInSlot validates that nonempty IDs are unlocked.
                        if (data.setTomeInSlot(slot, selectedId)) {
                            syncTomes(player);
                        }
                    });
                }
        );
    }

    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(
                SYNC_TOMES_S2C,
                TomePackets::receiveSyncTomes
        );
    }

    public static void selectTome(int slot, String tomeId) {
        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeInt(slot);
        buf.writeString(tomeId == null ? "" : tomeId);

        ClientPlayNetworking.send(SELECT_TOME_C2S, buf);
    }

    public static void syncTomes(ServerPlayerEntity player) {
        ModPlayerData data = (ModPlayerData) player;

        PacketByteBuf buf = PacketByteBufs.create();

        List<String> unlocked = data.getUnlockedTomes();
        buf.writeVarInt(unlocked.size());

        for (String tomeId : unlocked) {
            buf.writeString(tomeId);
        }

        for (int slot = 0; slot < 3; slot++) {
            String tomeId = data.getTomeInSlot(slot);
            buf.writeString(tomeId == null ? "" : tomeId);
        }

        ServerPlayNetworking.send(player, SYNC_TOMES_S2C, buf);
    }

    private static void receiveSyncTomes(
            MinecraftClient client,
            ClientPlayNetworkHandler handler,
            PacketByteBuf buf,
            PacketSender responseSender
    ) {
        int count = buf.readVarInt();

        // Defensive limit for incoming packet data.
        if (count < 0 || count > 10000) {
            return;
        }

        List<String> unlocked = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            unlocked.add(buf.readString(32767));
        }

        List<String> equipped = new ArrayList<>(3);

        for (int slot = 0; slot < 3; slot++) {
            equipped.add(buf.readString(32767));
        }

        client.execute(() ->
                TomeClientCache.update(unlocked, equipped)
        );
    }
}
