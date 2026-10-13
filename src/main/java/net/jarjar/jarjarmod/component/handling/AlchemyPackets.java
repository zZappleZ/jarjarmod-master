
package net.jarjar.jarjarmod.component.handling;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jarjar.jarjarmod.client.AlchemyArchiveClientCache;
import net.jarjar.jarjarmod.tomes.alchemy.AlchemyArchiveConfig;
import net.jarjar.jarjarmod.tomes.alchemy.AlchemyPlayerData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AlchemyPackets {

    public static final Identifier OPEN_ALCHEMY_BOOK =
            new Identifier("archivist", "open_alchemy_book");

    public static final Identifier ARCHIVE_SYNC_S2C =
            new Identifier("archivist", "alchemy_archive_sync");

    public static final Identifier ARCHIVE_ACTION_C2S =
            new Identifier("archivist", "alchemy_archive_action");

    private static final int ACTION_DEPOSIT = 0;
    private static final int ACTION_WITHDRAW = 1;
    private static final int MAX_ACTION_AMOUNT = 1024;

    private AlchemyPackets() {
    }

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(
                ARCHIVE_ACTION_C2S,
                (server, player, handler, buf, responseSender) -> {
                    int action = buf.readVarInt();
                    String itemId = buf.readString(256);
                    int amount = buf.readVarInt();

                    server.execute(() -> {
                        if (amount <= 0 || amount > MAX_ACTION_AMOUNT) {
                            return;
                        }

                        if (action != ACTION_DEPOSIT
                                && action != ACTION_WITHDRAW) {
                            return;
                        }

                        if (!AlchemyArchiveConfig.isAllowed(itemId)) {
                            syncArchive(player);
                            return;
                        }

                        Identifier id = Identifier.tryParse(itemId);

                        if (id == null || !Registries.ITEM.containsId(id)
                                || Registries.ITEM.get(id)
                                == net.minecraft.item.Items.AIR) {
                            syncArchive(player);
                            return;
                        }

                        if (action == ACTION_DEPOSIT) {
                            deposit(player, itemId, amount);
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
                ARCHIVE_SYNC_S2C,
                AlchemyPackets::receiveArchiveSync
        );
    }

    public static void openBook(ServerPlayerEntity player) {
        ServerPlayNetworking.send(
                player,
                OPEN_ALCHEMY_BOOK,
                PacketByteBufs.create()
        );

        syncArchive(player);
    }

    public static void requestDeposit(String itemId, int amount) {
        sendAction(ACTION_DEPOSIT, itemId, amount);
    }

    public static void requestWithdraw(String itemId, int amount) {
        sendAction(ACTION_WITHDRAW, itemId, amount);
    }

    private static void sendAction(
            int action,
            String itemId,
            int amount
    ) {
        if (itemId == null || itemId.length() > 256
                || amount <= 0 || amount > MAX_ACTION_AMOUNT) {
            return;
        }

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(action);
        buf.writeString(itemId);
        buf.writeVarInt(amount);

        ClientPlayNetworking.send(ARCHIVE_ACTION_C2S, buf);
    }

    private static void deposit(
            ServerPlayerEntity player,
            String itemId,
            int requested
    ) {
        AlchemyPlayerData data = (AlchemyPlayerData) player;

        int room = AlchemyPlayerData.ARCHIVE_LIMIT_PER_ITEM
                - data.getAlchemyStored(itemId);

        if (room <= 0) {
            return;
        }

        int available = 0;

        for (ItemStack stack : player.getInventory().main) {
            if (!stack.isEmpty()
                    && Registries.ITEM.getId(stack.getItem()).toString()
                    .equals(itemId)) {
                available += stack.getCount();
            }
        }

        int toMove = Math.min(requested, Math.min(room, available));

        if (toMove <= 0) {
            return;
        }

        int added = data.addAlchemyStored(itemId, toMove);
        int remainingToRemove = added;

        for (ItemStack stack : player.getInventory().main) {
            if (remainingToRemove <= 0) {
                break;
            }

            if (!stack.isEmpty()
                    && Registries.ITEM.getId(stack.getItem()).toString()
                    .equals(itemId)) {
                int removed = Math.min(
                        stack.getCount(),
                        remainingToRemove
                );

                stack.decrement(removed);
                remainingToRemove -= removed;
            }
        }

        // Defensive correction if the inventory changed unexpectedly.
        if (remainingToRemove > 0) {
            data.removeAlchemyStored(itemId, remainingToRemove);
        }
    }

    private static void withdraw(
            ServerPlayerEntity player,
            String itemId,
            int requested
    ) {
        AlchemyPlayerData data = (AlchemyPlayerData) player;
        int removed = data.removeAlchemyStored(itemId, requested);

        if (removed <= 0) {
            return;
        }

        ItemStack stack = new ItemStack(
                Registries.ITEM.get(new Identifier(itemId)),
                removed
        );

        player.getInventory().insertStack(stack);

        if (!stack.isEmpty()) {
            player.dropItem(stack, false);
        }
    }

    public static void syncArchive(ServerPlayerEntity player) {
        AlchemyPlayerData data = (AlchemyPlayerData) player;
        Map<String, Integer> archive = data.getAlchemyArchive();

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(archive.size());

        for (Map.Entry<String, Integer> entry : archive.entrySet()) {
            buf.writeString(entry.getKey(), 256);
            buf.writeVarInt(entry.getValue());
        }

        ServerPlayNetworking.send(player, ARCHIVE_SYNC_S2C, buf);
    }

    private static void receiveArchiveSync(
            MinecraftClient client,
            ClientPlayNetworkHandler handler,
            PacketByteBuf buf,
            PacketSender responseSender
    ) {
        int count = buf.readVarInt();

        if (count < 0 || count > 10000) {
            return;
        }

        Map<String, Integer> archive = new LinkedHashMap<>();

        for (int i = 0; i < count; i++) {
            String itemId = buf.readString(256);
            int amount = buf.readVarInt();

            if (amount > 0
                    && amount <= AlchemyPlayerData.ARCHIVE_LIMIT_PER_ITEM) {
                archive.put(itemId, amount);
            }
        }

        client.execute(() ->
                AlchemyArchiveClientCache.update(archive)
        );
    }
}
