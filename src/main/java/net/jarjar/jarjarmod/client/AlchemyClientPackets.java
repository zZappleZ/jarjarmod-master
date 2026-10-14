
package net.jarjar.jarjarmod.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.jarjar.jarjarmod.component.handling.AlchemyPackets;
import net.jarjar.jarjarmod.tomes.alchemy.AlchemyPlayerData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class AlchemyClientPackets {

    private AlchemyClientPackets() {
    }

    public static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(
                AlchemyPackets.ARCHIVE_SYNC,
                (client, handler, buf, sender) -> {
                    int allowedCount = buf.readVarInt();
                    if (allowedCount < 0 || allowedCount > 10000) {
                        return;
                    }

                    Set<String> allowed = new LinkedHashSet<>();
                    for (int i = 0; i < allowedCount; i++) {
                        allowed.add(buf.readString(256));
                    }

                    int archiveCount = buf.readVarInt();
                    if (archiveCount < 0 || archiveCount > 10000) {
                        return;
                    }

                    Map<String, Integer> archive = new LinkedHashMap<>();
                    for (int i = 0; i < archiveCount; i++) {
                        String id = buf.readString(256);
                        int amount = buf.readVarInt();

                        if (amount > 0
                                && amount <= AlchemyPlayerData.ARCHIVE_LIMIT_PER_ITEM) {
                            archive.put(id, amount);
                        }
                    }

                    client.execute(() ->
                            AlchemyArchiveClientCache.update(archive, allowed)
                    );
                }
        );
    }

    public static void requestDeposit(String itemId, int amount) {
        sendAction(0, itemId, amount);
    }

    public static void requestWithdraw(String itemId, int amount) {
        sendAction(1, itemId, amount);
    }

    public static void requestDepositAll(String itemId) {
        if (itemId == null || itemId.length() > 256) {
            return;
        }

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(0); // Deposit
        buf.writeString(itemId);
        buf.writeVarInt(-1); // Server interprets -1 as "all available"
        ClientPlayNetworking.send(AlchemyPackets.ARCHIVE_ACTION, buf);
    }

    private static void sendAction(int action, String itemId, int amount) {
        if (itemId == null || itemId.length() > 256
                || amount < 1 || amount > 64) {
            return;
        }

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(action);
        buf.writeString(itemId);
        buf.writeVarInt(amount);

        ClientPlayNetworking.send(AlchemyPackets.ARCHIVE_ACTION, buf);
    }
}
