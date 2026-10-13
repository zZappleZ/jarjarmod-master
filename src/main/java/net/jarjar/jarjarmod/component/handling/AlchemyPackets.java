
package net.jarjar.jarjarmod.component.handling;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class AlchemyPackets {

    public static final Identifier OPEN_ALCHEMY_BOOK =
            new Identifier("archivist", "open_alchemy_book");

    private AlchemyPackets() {
    }

    public static void openBook(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, OPEN_ALCHEMY_BOOK, PacketByteBufs.create());
    }
}