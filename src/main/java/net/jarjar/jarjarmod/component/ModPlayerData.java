package net.jarjar.jarjarmod.component;

import java.util.List;

public interface ModPlayerData {

    // Inkflow
    int getInkflowCurrent();
    void setInkflowCurrent(int value);
    int getInkflowCapacity(); // read-only now — derived from focus levels
    float getInkflowReplenishmentPerSecond();

    // Tomes
    List<String> getUnlockedTomes();
    boolean unlockTome(String tomeId);
    boolean isTomeUnlocked(String tomeId);

    boolean setTomeInSlot(int slot, String tomeId); // now returns success/failure
    String getTomeInSlot(int slot);
    List<String> getEquippedTomes();

    int getActiveTomeSlot();
    void setActiveTomeSlot(int slot);
    String getActiveTome();

    // Focuses
    int getFocusScriptLevel();
    void setFocusScriptLevel(int level);

    int getFocusInscribeLevel();
    void setFocusInscribeLevel(int level);

    int getFocusRedactLevel();
    void setFocusRedactLevel(int level);

    int getFocusManifestLevel();
    void setFocusManifestLevel(int level);

    boolean isRedactActive();
    void setRedactActive(boolean active);
}
