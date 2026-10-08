package net.jarjar.jarjarmod.component.handling;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.jarjar.jarjarmod.client.InkflowClientCache;
import net.jarjar.jarjarmod.component.ModPlayerData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ArchivistPackets {

    public static final Identifier SYNC_INKFLOW_S2C = new Identifier("archivist", "sync_inkflow");

    public static void registerGlobalReceiversS2C() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_INKFLOW_S2C, ArchivistPackets::receiveSyncInkflow);
    }

    // Call this any time InkflowCurrent, InkflowCapacity (i.e. Script/Inscribe levels),
    // or Redact state changes server-side
    public static void syncInkflow(ServerPlayerEntity player) {
        ModPlayerData data = (ModPlayerData) player;
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(data.getInkflowCurrent());
        buf.writeInt(data.getInkflowCapacity());
        buf.writeFloat(data.getInkflowReplenishmentPerSecond());
        ServerPlayNetworking.send(player, SYNC_INKFLOW_S2C, buf);
    }

    private static void receiveSyncInkflow(MinecraftClient client, ClientPlayNetworkHandler handler,
                                           PacketByteBuf buf, PacketSender responseSender) {
        int current = buf.readInt();
        int capacity = buf.readInt();
        float replenishment = buf.readFloat();

        client.execute(() -> InkflowClientCache.update(current, capacity, replenishment));
    }
}