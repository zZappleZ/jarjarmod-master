
package net.jarjar.jarjarmod.tomes.alchemy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class AlchemyArchiveConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir()
            .resolve("jarjarmod")
            .resolve("alchemy_archive.json");

    private static final Set<String> DEFAULTS = Set.of(
            "minecraft:blaze_powder",
            "minecraft:nether_wart",
            "minecraft:redstone",
            "minecraft:glowstone_dust",
            "minecraft:spider_eye",
            "minecraft:fermented_spider_eye",
            "minecraft:sugar",
            "minecraft:ghast_tear",
            "minecraft:magma_cream",
            "minecraft:rabbit_foot",
            "minecraft:phantom_membrane",
            "minecraft:golden_carrot",
            "minecraft:turtle_helmet",
            "minecraft:pufferfish",
            "minecraft:dragon_breath"
    );

    private static Set<String> allowed = new LinkedHashSet<>();

    private AlchemyArchiveConfig() {
    }

    public static void load() {
        try {
            Files.createDirectories(PATH.getParent());

            if (Files.notExists(PATH)) {
                JsonObject json = new JsonObject();
                JsonArray items = new JsonArray();
                DEFAULTS.forEach(items::add);
                json.add("allowed_items", items);
                Files.writeString(PATH, GSON.toJson(json));
            }

            JsonObject json = GSON.fromJson(Files.readString(PATH), JsonObject.class);
            Set<String> loaded = new LinkedHashSet<>();

            if (json != null && json.has("allowed_items")
                    && json.get("allowed_items").isJsonArray()) {
                for (var element : json.getAsJsonArray("allowed_items")) {
                    if (!element.isJsonPrimitive()
                            || !element.getAsJsonPrimitive().isString()) {
                        continue;
                    }

                    Identifier id = Identifier.tryParse(element.getAsString());
                    if (id != null && Registries.ITEM.containsId(id)
                            && Registries.ITEM.get(id) != Items.AIR) {
                        loaded.add(id.toString());
                    }
                }
            }

            allowed = loaded;
        } catch (IOException | RuntimeException exception) {
            allowed = new LinkedHashSet<>(DEFAULTS);
            System.err.println("[JarJarMod] Could not load alchemy archive config: "
                    + exception.getMessage());
        }
    }

    public static boolean isAllowed(String itemId) {
        return allowed.contains(itemId);
    }

    public static Set<String> getAllowedItems() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(allowed));
    }
}
