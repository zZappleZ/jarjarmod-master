package net.jarjar.jarjarmod.mixin;

import net.jarjar.jarjarmod.component.ModPlayerData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements ModPlayerData {

    @Unique private static final int BASE_INKFLOW_CAPACITY = 500;

    @Unique private int inkflowCurrent = 0;
    @Unique private int inkflowCapacity = BASE_INKFLOW_CAPACITY; // recalculated, never set externally

    @Unique private final Set<String> unlockedTomes = new LinkedHashSet<>();
    @Unique private final String[] equippedTomes = new String[3];
    @Unique private int activeTomeSlot = 0;
    @Unique private String activeTome = null;

    @Unique private int focusScriptLevel = 0;
    @Unique private int focusInscribeLevel = 0;
    @Unique private int focusRedactLevel = 0;
    @Unique private int focusManifestLevel = 0;
    @Unique private boolean redactActive = false;

    // --- Inkflow ---
    @Override
    public int getInkflowCurrent() { return inkflowCurrent; }

    @Override
    public void setInkflowCurrent(int value) {
        this.inkflowCurrent = Math.max(0, Math.min(value, this.inkflowCapacity));
    }

    @Override
    public int getInkflowCapacity() { return inkflowCapacity; }

    @Unique
    private void recalculateInkflowCapacity() {
        int newCapacity = Math.round(BASE_INKFLOW_CAPACITY
                + (0.5f * this.focusScriptLevel)
                + (0.3f * this.focusInscribeLevel));
        this.inkflowCapacity = Math.max(1, newCapacity);
        this.inkflowCurrent = Math.min(this.inkflowCurrent, this.inkflowCapacity); // reclamp if capacity shrank
    }

    @Override
    public float getInkflowReplenishmentPerSecond() {
        float rate = 1f + (0.01f * this.focusScriptLevel);
        if (this.redactActive) {
            rate += 1f + (0.02f * this.focusRedactLevel);
        }
        return rate;
    }

    // --- Tomes ---
    @Override
    public List<String> getUnlockedTomes() {
        return new ArrayList<>(unlockedTomes);
    }

    @Override
    public boolean unlockTome(String tomeId) {
        return unlockedTomes.add(tomeId); // false if already unlocked
    }

    @Override
    public boolean isTomeUnlocked(String tomeId) {
        return unlockedTomes.contains(tomeId);
    }

    @Override
    public boolean setTomeInSlot(int slot, String tomeId) {
        if (tomeId == null || tomeId.isEmpty()) {
            equippedTomes[slot] = null; // clearing a slot is always allowed
            refreshActiveTome();
            return true;
        }
        if (!unlockedTomes.contains(tomeId)) {
            return false; // must be unlocked first
        }
        equippedTomes[slot] = tomeId;
        refreshActiveTome();
        return true;
    }

    @Override
    public String getTomeInSlot(int slot) { return equippedTomes[slot]; }

    @Override
    public List<String> getEquippedTomes() { return Arrays.asList(equippedTomes); }

    @Override
    public int getActiveTomeSlot() { return activeTomeSlot; }

    @Override
    public void setActiveTomeSlot(int slot) {
        this.activeTomeSlot = slot;
        refreshActiveTome();
    }

    @Override
    public String getActiveTome() { return activeTome; }

    @Unique
    private void refreshActiveTome() {
        this.activeTome = equippedTomes[activeTomeSlot];
    }

    // --- Focuses ---
    @Override
    public int getFocusScriptLevel() { return focusScriptLevel; }

    @Override
    public void setFocusScriptLevel(int level) {
        this.focusScriptLevel = Math.max(0, Math.min(level, 7000));
        recalculateInkflowCapacity();
    }

    @Override
    public int getFocusInscribeLevel() { return focusInscribeLevel; }

    @Override
    public void setFocusInscribeLevel(int level) {
        this.focusInscribeLevel = Math.max(0, Math.min(level, 7000));
        recalculateInkflowCapacity();
    }

    @Override
    public int getFocusRedactLevel() { return focusRedactLevel; }

    @Override
    public void setFocusRedactLevel(int level) {
        this.focusRedactLevel = Math.max(0, Math.min(level, 7000));
        // doesn't affect capacity — only replenishment, and only while active
    }

    @Override
    public int getFocusManifestLevel() { return focusManifestLevel; }

    @Override
    public void setFocusManifestLevel(int level) {
        this.focusManifestLevel = Math.max(0, Math.min(level, 7000));
    }

    @Override
    public boolean isRedactActive() { return redactActive; }

    @Override
    public void setRedactActive(boolean active) { this.redactActive = active; }

    // --- NBT write ---
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeModData(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("InkflowCurrent", this.inkflowCurrent);
        nbt.putInt("InkflowCapacity", this.inkflowCapacity); // stored for visibility; recomputed on load anyway

        NbtList unlockedList = new NbtList();
        for (String tomeId : unlockedTomes) {
            unlockedList.add(NbtString.of(tomeId));
        }
        nbt.put("UnlockedTomes", unlockedList);

        NbtList tomeList = new NbtList();
        for (String tomeId : equippedTomes) {
            tomeList.add(NbtString.of(tomeId == null ? "" : tomeId));
        }
        nbt.put("EquippedTomes", tomeList);

        nbt.putInt("ActiveTomeSlot", this.activeTomeSlot);
        nbt.putString("ActiveTome", this.activeTome == null ? "" : this.activeTome);

        nbt.putInt("FocusScriptLevel", this.focusScriptLevel);
        nbt.putInt("FocusInscribeLevel", this.focusInscribeLevel);
        nbt.putInt("FocusRedactLevel", this.focusRedactLevel);
        nbt.putInt("FocusManifestLevel", this.focusManifestLevel);

        nbt.putBoolean("RedactActive", this.redactActive);
    }

    // --- NBT read ---
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readModData(NbtCompound nbt, CallbackInfo ci) {

        if (nbt.contains("UnlockedTomes", NbtElement.LIST_TYPE)) {
            unlockedTomes.clear();
            NbtList unlockedList = nbt.getList("UnlockedTomes", NbtElement.STRING_TYPE);
            for (int i = 0; i < unlockedList.size(); i++) {
                unlockedTomes.add(unlockedList.getString(i));
            }
        }

        if (nbt.contains("EquippedTomes", NbtElement.LIST_TYPE)) {
            NbtList tomeList = nbt.getList("EquippedTomes", NbtElement.STRING_TYPE);
            for (int i = 0; i < Math.min(3, tomeList.size()); i++) {
                String value = tomeList.getString(i);
                equippedTomes[i] = value.isEmpty() ? null : value;
            }
        }

        if (nbt.contains("ActiveTomeSlot", NbtElement.INT_TYPE)) {
            this.activeTomeSlot = nbt.getInt("ActiveTomeSlot");
        }
        refreshActiveTome(); // recompute, don't trust the stored value

        if (nbt.contains("FocusScriptLevel", NbtElement.INT_TYPE)) {
            this.focusScriptLevel = nbt.getInt("FocusScriptLevel");
        }
        if (nbt.contains("FocusInscribeLevel", NbtElement.INT_TYPE)) {
            this.focusInscribeLevel = nbt.getInt("FocusInscribeLevel");
        }
        if (nbt.contains("FocusRedactLevel", NbtElement.INT_TYPE)) {
            this.focusRedactLevel = nbt.getInt("FocusRedactLevel");
        }
        if (nbt.contains("FocusManifestLevel", NbtElement.INT_TYPE)) {
            this.focusManifestLevel = nbt.getInt("FocusManifestLevel");
        }

        // Must happen AFTER focus levels are read, BEFORE InkflowCurrent —
        // capacity needs to be correct before we clamp current against it
        recalculateInkflowCapacity();

        if (nbt.contains("InkflowCurrent", NbtElement.INT_TYPE)) {
            this.setInkflowCurrent(nbt.getInt("InkflowCurrent")); // clamps against fresh capacity
        }

        if (nbt.contains("RedactActive", NbtElement.BYTE_TYPE)) {
            this.redactActive = nbt.getBoolean("RedactActive");
        }
    }
}