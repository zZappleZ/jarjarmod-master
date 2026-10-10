
package net.jarjar.jarjarmod.client;

public final class InkflowClientCache {

    private static int current = 0;
    private static int capacity = 500;
    private static float replenishmentPerSecond = 1.0f;

    private static int focusScript = 0;
    private static int focusInscribe = 0;
    private static int focusRedact = 0;
    private static int focusManifest = 0;

    private InkflowClientCache() {
    }

    public static void update(
            int newCurrent,
            int newCapacity,
            float newReplenishment,
            int newFocusScript,
            int newFocusInscribe,
            int newFocusRedact,
            int newFocusManifest
    ) {
        current = Math.max(0, newCurrent);
        capacity = Math.max(1, newCapacity);
        replenishmentPerSecond = Math.max(0.0f, newReplenishment);

        focusScript = clampFocus(newFocusScript);
        focusInscribe = clampFocus(newFocusInscribe);
        focusRedact = clampFocus(newFocusRedact);
        focusManifest = clampFocus(newFocusManifest);
    }

    private static int clampFocus(int value) {
        return Math.max(0, Math.min(value, 7000));
    }

    public static int getCurrent() {
        return current;
    }

    public static int getCapacity() {
        return capacity;
    }

    public static float getReplenishmentPerSecond() {
        return replenishmentPerSecond;
    }

    public static int getFocusScript() {
        return focusScript;
    }

    public static int getFocusInscribe() {
        return focusInscribe;
    }

    public static int getFocusRedact() {
        return focusRedact;
    }

    public static int getFocusManifest() {
        return focusManifest;
    }
}
