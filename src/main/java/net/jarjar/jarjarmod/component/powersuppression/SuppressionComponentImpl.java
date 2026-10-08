package net.jarjar.jarjarmod.component.powersuppression;

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.jarjar.jarjarmod.component.ModComponents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

public class SuppressionComponentImpl implements SuppressionComponent, AutoSyncedComponent {

    private final PlayerEntity provider;

    private boolean suppressed = false;

    public SuppressionComponentImpl(PlayerEntity provider) {
        this.provider = provider;
    }

    @Override
    public boolean isSuppressed() {
        return suppressed;
    }

    @Override
    public void setSuppressed(boolean suppressed) {
        if (this.suppressed == suppressed) {
            return;
        }

        this.suppressed = suppressed;

        ModComponents.SUPPRESSION.sync(provider);
    }

    @Override
    public void readFromNbt(NbtCompound nbt) {
        this.suppressed = nbt.getBoolean("Suppressed");
    }

    @Override
    public void writeToNbt(NbtCompound nbt) {
        nbt.putBoolean("Suppressed", this.suppressed);
    }
}