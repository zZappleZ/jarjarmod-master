package net.jarjar.jarjarmod.tomes.alchemy;

import java.util.Map;

public interface AlchemyPlayerData {
    int ARCHIVE_LIMIT_PER_ITEM = 1024;

    Map<String, Integer> getAlchemyArchive();

    int getAlchemyStored(String itemId);

    /**
     * Adds up to the remaining per-item capacity.
     * Returns the number of items actually added.
     */
    int addAlchemyStored(String itemId, int amount);

    /**
     * Removes up to the currently stored amount.
     * Returns the number of items actually removed.
     */
    int removeAlchemyStored(String itemId, int amount);
}

