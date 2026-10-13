
package net.jarjar.jarjarmod.mixin;

import net.jarjar.jarjarmod.tomes.alchemy.AlchemyPlayerData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Mixin(PlayerEntity.class)
public abstract class AlchemyPlayerDataMixin implements AlchemyPlayerData {

    @Unique
    private final Map<String, Integer> jarjarmod$alchemyArchive = new HashMap<>();

    @Override
    public Map<String, Integer> getAlchemyArchive() {
        return Collections.unmodifiableMap(
                new HashMap<>(jarjarmod$alchemyArchive)
        );
    }

    @Override
    public int getAlchemyStored(String itemId) {
        return jarjarmod$alchemyArchive.getOrDefault(itemId, 0);
    }

    @Override
    public int addAlchemyStored(String itemId, int amount) {
        if (amount <= 0 || !jarjarmod$isValidItemId(itemId)) {
            return 0;
        }

        int current = getAlchemyStored(itemId);
        int accepted = Math.min(
                amount,
                ARCHIVE_LIMIT_PER_ITEM - current
        );

        if (accepted <= 0) {
            return 0;
        }

        jarjarmod$alchemyArchive.put(itemId, current + accepted);
        return accepted;
    }

    @Override
    public int removeAlchemyStored(String itemId, int amount) {
        if (amount <= 0 || !jarjarmod$isValidItemId(itemId)) {
            return 0;
        }

        int current = getAlchemyStored(itemId);
        int removed = Math.min(amount, current);

        if (removed <= 0) {
            return 0;
        }

        int remaining = current - removed;

        if (remaining == 0) {
            jarjarmod$alchemyArchive.remove(itemId);
        } else {
            jarjarmod$alchemyArchive.put(itemId, remaining);
        }

        return removed;
    }

    @Unique
    private boolean jarjarmod$isValidItemId(String itemId) {
        if (itemId == null) {
            return false;
        }

        Identifier identifier = Identifier.tryParse(itemId);

        return identifier != null
                && Registries.ITEM.containsId(identifier)
                && !Registries.ITEM.get(identifier).equals(
                net.minecraft.item.Items.AIR
        );
    }

    @Inject(
            method = "writeCustomDataToNbt",
            at = @At("TAIL")
    )
    private void jarjarmod$writeAlchemyData(
            NbtCompound nbt,
            CallbackInfo ci
    ) {
        NbtCompound archive = new NbtCompound();

        for (Map.Entry<String, Integer> entry
                : jarjarmod$alchemyArchive.entrySet()) {
            int amount = entry.getValue();

            if (amount > 0 && amount <= ARCHIVE_LIMIT_PER_ITEM) {
                archive.putInt(entry.getKey(), amount);
            }
        }

        nbt.put("JarJarModAlchemyArchive", archive);
    }

    @Inject(
            method = "readCustomDataFromNbt",
            at = @At("TAIL")
    )
    private void jarjarmod$readAlchemyData(
            NbtCompound nbt,
            CallbackInfo ci
    ) {
        jarjarmod$alchemyArchive.clear();

        if (!nbt.contains(
                "JarJarModAlchemyArchive",
                NbtElement.COMPOUND_TYPE
        )) {
            return;
        }

        NbtCompound archive =
                nbt.getCompound("JarJarModAlchemyArchive");

        for (String itemId : archive.getKeys()) {
            if (!jarjarmod$isValidItemId(itemId)) {
                continue;
            }

            int amount = archive.getInt(itemId);

            if (amount > 0) {
                jarjarmod$alchemyArchive.put(
                        itemId,
                        Math.min(amount, ARCHIVE_LIMIT_PER_ITEM)
                );
            }
        }
    }
}
