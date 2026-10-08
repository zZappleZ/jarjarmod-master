package net.jarjar.jarjarmod.component.handling;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.jarjar.jarjarmod.component.ModPlayerData;

import java.util.List;

public class ArchivistDataTransfer {

    public static void register() {
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            ModPlayerData oldData = (ModPlayerData) oldPlayer;
            ModPlayerData newData = (ModPlayerData) newPlayer;

            // Focus levels first — capacity recalculates automatically as a side effect
            newData.setFocusScriptLevel(oldData.getFocusScriptLevel());
            newData.setFocusInscribeLevel(oldData.getFocusInscribeLevel());
            newData.setFocusRedactLevel(oldData.getFocusRedactLevel());
            newData.setFocusManifestLevel(oldData.getFocusManifestLevel());

            newData.setInkflowCurrent(oldData.getInkflowCurrent()); // now clamps against correct capacity

            for (String tomeId : oldData.getUnlockedTomes()) {
                newData.unlockTome(tomeId);
            }

            List<String> equipped = oldData.getEquippedTomes();
            for (int i = 0; i < 3; i++) {
                newData.setTomeInSlot(i, equipped.get(i));
            }
            newData.setActiveTomeSlot(oldData.getActiveTomeSlot());

            newData.setRedactActive(oldData.isRedactActive());

            ArchivistPackets.syncInkflow(newPlayer);
        });
    }
}