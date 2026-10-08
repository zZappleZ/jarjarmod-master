package net.jarjar.jarjarmod.system;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.jarjar.jarjarmod.component.ModPlayerData;
import net.jarjar.jarjarmod.component.handling.ArchivistPackets;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InkflowReplenishmentHandler {

    // Tracks leftover fractional Inkflow per player between ticks,
    // since InkflowCurrent is an int but replenishment rates are fractional.
    private static final Map<UUID, Float> ACCUMULATORS = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tick(player);
            }
        });

        // Avoid leaking accumulator entries for players who log off
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                ACCUMULATORS.remove(handler.getPlayer().getUuid()));
    }

    private static void tick(ServerPlayerEntity player) {
        ModPlayerData data = (ModPlayerData) player;

        int current = data.getInkflowCurrent();
        int capacity = data.getInkflowCapacity();
        if (current >= capacity) {
            ACCUMULATORS.remove(player.getUuid()); // already full, nothing to accumulate
            return;
        }

        float perTick = data.getInkflowReplenishmentPerSecond() / 20f; // 20 ticks per second
        float accumulated = ACCUMULATORS.getOrDefault(player.getUuid(), 0f) + perTick;

        int wholeUnits = (int) accumulated;
        if (wholeUnits > 0) {
            data.setInkflowCurrent(current + wholeUnits); // setter re-clamps to capacity
            accumulated -= wholeUnits;
            ArchivistPackets.syncInkflow(player); // only sync when something actually changed
        }

        ACCUMULATORS.put(player.getUuid(), accumulated);
    }
}
