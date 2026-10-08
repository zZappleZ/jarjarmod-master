package net.jarjar.jarjarmod.component;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;

public class PlayerDomainComponent implements DomainComponent, AutoSyncedComponent {
    private int domain = 0; // Default value
    private final Object provider; // The entity this is attached to

    public PlayerDomainComponent(Object provider) {
        this.provider = provider;
    }

    @Override
    public int getValue() {
        return domain;
    }

    @Override
    public void setValue(int value) {
        this.domain = Math.max(0, value); // Prevent negative mana
        // AutoSyncedComponent handles syncing automatically when we set this
    }

    // This is required for AutoSyncedComponent to send data to the client
    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return player == this.provider; // Only sync to the owner
    }

    @Override
    public void readFromNbt(NbtCompound nbtCompound) {

    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound) {

    }
}
