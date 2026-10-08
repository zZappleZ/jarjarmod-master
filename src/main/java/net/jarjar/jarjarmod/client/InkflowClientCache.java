package net.jarjar.jarjarmod.client;

public class InkflowClientCache {

    private static int current = 0;
    private static int capacity = 500;
    private static float replenishmentPerSecond = 1f;

    public static void update(int newCurrent, int newCapacity, float newReplenishment) {
        current = newCurrent;
        capacity = newCapacity;
        replenishmentPerSecond = newReplenishment;
    }

    public static int getCurrent() { return current; }
    public static int getCapacity() { return capacity; }
    public static float getReplenishmentPerSecond() { return replenishmentPerSecond; }
}
