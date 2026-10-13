
package net.jarjar.jarjarmod.client;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AlchemyArchiveClientCache {

    private static Map<String, Integer> archive =
            Collections.emptyMap();

    private AlchemyArchiveClientCache() {
    }

    public static synchronized void update(Map<String, Integer> values) {
        archive = Collections.unmodifiableMap(
                new LinkedHashMap<>(values)
        );
    }

    public static synchronized Map<String, Integer> getArchive() {
        return archive;
    }

    public static synchronized int getStored(String itemId) {
        return archive.getOrDefault(itemId, 0);
    }

    public static synchronized void clear() {
        archive = Collections.emptyMap();
    }
}
