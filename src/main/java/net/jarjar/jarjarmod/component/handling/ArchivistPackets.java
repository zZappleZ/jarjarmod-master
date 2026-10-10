
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

public final class ArchivistPackets {

    public static final Identifier SYNC_INKFLOW_S2C =
            new Identifier("archivist", "sync_inkflow");

    private ArchivistPackets() {
    }

    public static void registerGlobalReceiversS2C() {
        ClientPlayNetworking.registerGlobalReceiver(
                SYNC_INKFLOW_S2C,
                ArchivistPackets::receiveSyncInkflow
        );
    }

    public static void syncInkflow(ServerPlayerEntity player) {
        ModPlayerData data = (ModPlayerData) player;

        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeInt(data.getInkflowCurrent());
        buf.writeInt(data.getInkflowCapacity());
        buf.writeFloat(data.getInkflowReplenishmentPerSecond());

        buf.writeInt(data.getFocusScriptLevel());
        buf.writeInt(data.getFocusInscribeLevel());
        buf.writeInt(data.getFocusRedactLevel());
        buf.writeInt(data.getFocusManifestLevel());

        ServerPlayNetworking.send(player, SYNC_INKFLOW_S2C, buf);
    }

    private static void receiveSyncInkflow(
            MinecraftClient client,
            ClientPlayNetworkHandler handler,
            PacketByteBuf buf,
            PacketSender responseSender
    ) {
        int current = buf.readInt();
        int capacity = buf.readInt();
        float replenishment = buf.readFloat();

        int script = buf.readInt();
        int inscribe = buf.readInt();
        int redact = buf.readInt();
        int manifest = buf.readInt();

        client.execute(() -> InkflowClientCache.update(
                current,
                capacity,
                replenishment,
                script,
                inscribe,
                redact,
                manifest
        ));
    }
}
