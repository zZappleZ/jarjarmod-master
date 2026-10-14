
package net.jarjar.jarjarmod.component.handling;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.jarjar.jarjarmod.client.AlchemyArchiveClientCache;
import net.jarjar.jarjarmod.tomes.alchemy.AlchemyArchiveConfig;
import net.jarjar.jarjarmod.tomes.alchemy.AlchemyPlayerData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class AlchemyPackets {
    public static final Identifier OPEN_ALCHEMY_BOOK =
            new Identifier("archivist", "open_alchemy_book");
    public static final Identifier ARCHIVE_SYNC =
            new Identifier("archivist", "alchemy_archive_sync");
    public static final Identifier ARCHIVE_ACTION =
            new Identifier("archivist", "alchemy_archive_action");

    private static final int DEPOSIT = 0;
    private static final int WITHDRAW = 1;

    private AlchemyPackets() {
    }

    public static void openBook(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, OPEN_ALCHEMY_BOOK, PacketByteBufs.create());
        syncArchive(player);
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(
                ARCHIVE_ACTION,
                (server, player, handler, buf, responseSender) -> {
                    int action = buf.readVarInt();
                    String itemId = buf.readString(256);
                    int amount = buf.readVarInt();

                    server.execute(() -> {
                        boolean depositAll = action == 0 && amount == -1;

                        if ((!depositAll && (amount < 1 || amount > 64))
                                || (action != 0 && action != 1)
                                || !AlchemyArchiveConfig.isAllowed(itemId)) {
                            syncArchive(player);
                            return;
                        }

                        Identifier id = Identifier.tryParse(itemId);
                        if (id == null || !Registries.ITEM.containsId(id)
                                || Registries.ITEM.get(id) == Items.AIR) {
                            syncArchive(player);
                            return;
                        }

                        if (action == 0) {
                            deposit(player, itemId, depositAll ? Integer.MAX_VALUE : amount);
                        } else {
                            withdraw(player, itemId, amount);
                        }

                        syncArchive(player);
                    });
                }
        );
    }

    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(
                ARCHIVE_SYNC,
                (client, handler, buf, responseSender) ->
                        receiveArchiveSync(client, buf)
        );
    }

    public static void requestDeposit(String itemId, int amount) {
        sendAction(DEPOSIT, itemId, amount);
    }

    public static void requestWithdraw(String itemId, int amount) {
        sendAction(WITHDRAW, itemId, amount);
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
        ClientPlayNetworking.send(ARCHIVE_ACTION, buf);
    }

    private static void deposit(
            ServerPlayerEntity player, String itemId, int requested
    ) {
        AlchemyPlayerData data = (AlchemyPlayerData) player;
        int room = AlchemyPlayerData.ARCHIVE_LIMIT_PER_ITEM
                - data.getAlchemyStored(itemId);
        if (room <= 0) return;

        int available = 0;
        for (ItemStack stack : player.getInventory().main) {
            if (!stack.isEmpty()
                    && Registries.ITEM.getId(stack.getItem()).toString().equals(itemId)) {
                available += stack.getCount();
            }
        }

        int toMove = Math.min(requested, Math.min(room, available));
        if (toMove <= 0) return;

        int added = data.addAlchemyStored(itemId, toMove);
        int remaining = added;

        for (ItemStack stack : player.getInventory().main) {
            if (remaining <= 0) break;
            if (!stack.isEmpty()
                    && Registries.ITEM.getId(stack.getItem()).toString().equals(itemId)) {
                int removed = Math.min(stack.getCount(), remaining);
                stack.decrement(removed);
                remaining -= removed;
            }
        }

        if (remaining > 0) {
            data.removeAlchemyStored(itemId, remaining);
        }
    }

    private static void withdraw(
            ServerPlayerEntity player, String itemId, int requested
    ) {
        AlchemyPlayerData data = (AlchemyPlayerData) player;
        int removed = data.removeAlchemyStored(itemId, requested);
        if (removed <= 0) return;

        Identifier id = Identifier.tryParse(itemId);
        if (id == null || !Registries.ITEM.containsId(id)) {
            data.addAlchemyStored(itemId, removed);
            return;
        }

        ItemStack stack = new ItemStack(Registries.ITEM.get(id), removed);
        player.getInventory().insertStack(stack);
        if (!stack.isEmpty()) {
            player.dropItem(stack, false);
        }
    }

    public static void syncArchive(ServerPlayerEntity player) {
        AlchemyPlayerData data = (AlchemyPlayerData) player;
        Map<String, Integer> archive = data.getAlchemyArchive();
        Set<String> allowed = AlchemyArchiveConfig.getAllowedItems();

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(allowed.size());
        for (String id : allowed) buf.writeString(id, 256);

        buf.writeVarInt(archive.size());
        for (Map.Entry<String, Integer> entry : archive.entrySet()) {
            buf.writeString(entry.getKey(), 256);
            buf.writeVarInt(entry.getValue());
        }

        ServerPlayNetworking.send(player, ARCHIVE_SYNC, buf);
    }

    private static void receiveArchiveSync(
            MinecraftClient client, PacketByteBuf buf
    ) {
        int allowedCount = buf.readVarInt();
        if (allowedCount < 0 || allowedCount > 10000) return;

        Set<String> allowed = new java.util.LinkedHashSet<>();
        for (int i = 0; i < allowedCount; i++) {
            allowed.add(buf.readString(256));
        }

        int archiveCount = buf.readVarInt();
        if (archiveCount < 0 || archiveCount > 10000) return;

        Map<String, Integer> archive = new LinkedHashMap<>();
        for (int i = 0; i < archiveCount; i++) {
            String id = buf.readString(256);
            int amount = buf.readVarInt();
            if (amount > 0 && amount <= AlchemyPlayerData.ARCHIVE_LIMIT_PER_ITEM) {
                archive.put(id, amount);
            }
        }

        client.execute(() -> AlchemyArchiveClientCache.update(archive, allowed));
    }
}
