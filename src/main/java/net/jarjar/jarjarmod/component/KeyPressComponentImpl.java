package net.jarjar.jarjarmod.component;

import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Active;
import io.github.apace100.apoli.power.Power;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class KeyPressComponentImpl implements KeyPressComponent, CommonTickingComponent {

    private int previousPowerSize = 0;

    private final Set<Active.Key> keysToCheck = new HashSet<>();

    private Set<Active.Key> previouslyUsedKeys = new HashSet<>();

    private final Set<Active.Key> currentlyUsedKeys = new HashSet<>();

    private final PlayerEntity provider;

    public KeyPressComponentImpl(PlayerEntity provider) {
        this.provider = provider;
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
    }

    @Override
    public Set<Active.Key> getCurrentlyUsedKeys() {
        return currentlyUsedKeys;
    }

    @Override
    public Set<Active.Key> getPreviouslyUsedKeys() {
        return previouslyUsedKeys;
    }

    @Override
    public void setPreviouslyUsedKeys() {
        previouslyUsedKeys = currentlyUsedKeys.stream()
                .filter(key -> key.continuous)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<Active.Key> getKeysToCheck() {
        return keysToCheck;
    }

    @Override
    public void addKeyToCheck(Active.Key key) {
        keysToCheck.add(key);
    }

    @Override
    public void addKey(Active.Key key) {
        currentlyUsedKeys.add(key);
    }

    @Override
    public void removeKey(Active.Key key) {
        currentlyUsedKeys.remove(key);
    }

    @Override
    public void tick() {
        int powerSize = PowerHolderComponent.KEY
                .get(provider)
                .getPowers(Power.class, true)
                .size();

        if (previousPowerSize != powerSize) {
            keysToCheck.clear();
            previouslyUsedKeys.clear();
            currentlyUsedKeys.clear();
        }

        previousPowerSize = powerSize;
    }
}