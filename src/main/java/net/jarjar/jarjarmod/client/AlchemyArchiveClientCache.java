
package net.jarjar.jarjarmod.client;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class AlchemyArchiveClientCache {
    private static Map<String, Integer> archive = Collections.emptyMap();
    private static Set<String> allowedItems = Collections.emptySet();

    private AlchemyArchiveClientCache() {
    }

    public static synchronized void update(
            Map<String, Integer> newArchive,
            Set<String> newAllowedItems
    ) {
        archive = Collections.unmodifiableMap(
                new LinkedHashMap<>(newArchive)
        );
        allowedItems = Collections.unmodifiableSet(
                new LinkedHashSet<>(newAllowedItems)
        );
    }

    public static synchronized Map<String, Integer> getArchive() {
        return archive;
    }

    public static synchronized Set<String> getAllowedItems() {
        return allowedItems;
    }

    public static synchronized int getStored(String itemId) {
        return archive.getOrDefault(itemId, 0);
    }

    public static synchronized void clear() {
        archive = Collections.emptyMap();
        allowedItems = Collections.emptySet();
    }
}
