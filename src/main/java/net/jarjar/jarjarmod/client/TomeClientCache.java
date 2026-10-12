
package net.jarjar.jarjarmod.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TomeClientCache {

    private static List<String> unlockedTomes = new ArrayList<>();
    private static List<String> equippedTomes = new ArrayList<>(
            java.util.Arrays.asList(null, null, null)
    );

    private TomeClientCache() {
    }

    public static void update(
            List<String> unlocked,
            List<String> equipped
    ) {
        unlockedTomes = new ArrayList<>(unlocked);

        equippedTomes = new ArrayList<>(
                java.util.Arrays.asList(null, null, null)
        );

        for (int i = 0; i < Math.min(3, equipped.size()); i++) {
            String tomeId = equipped.get(i);
            equippedTomes.set(i,
                    tomeId == null || tomeId.isEmpty() ? null : tomeId);
        }
    }

    public static List<String> getUnlockedTomes() {
        return Collections.unmodifiableList(
                new ArrayList<>(unlockedTomes)
        );
    }

    public static List<String> getEquippedTomes() {
        return Collections.unmodifiableList(
                new ArrayList<>(equippedTomes)
        );
    }

    public static String getEquippedTome(int slot) {
        if (slot < 0 || slot >= 3) {
            return null;
        }

        return equippedTomes.get(slot);
    }
}
