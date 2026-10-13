
package net.jarjar.jarjarmod.tomes.alchemy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public final class AlchemyArchiveConfig {

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir()
                    .resolve("jarjarmod")
                    .resolve("alchemy_archive.json");

    private static final Set<String> DEFAULT_ITEMS = Set.of(
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

    private static Set<String> allowedItems = new LinkedHashSet<>();

    private AlchemyArchiveConfig() {
    }

    public static void load() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            if (Files.notExists(CONFIG_PATH)) {
                JsonObject defaults = new JsonObject();
                JsonArray items = new JsonArray();

                for (String id : DEFAULT_ITEMS) {
                    items.add(id);
                }

                defaults.add("allowed_items", items);

                Files.writeString(
                        CONFIG_PATH,
                        GSON.toJson(defaults)
                );
            }

            JsonObject root = GSON.fromJson(
                    Files.readString(CONFIG_PATH),
                    JsonObject.class
            );

            Set<String> loaded = new LinkedHashSet<>();

            if (root != null && root.has("allowed_items")
                    && root.get("allowed_items").isJsonArray()) {
                for (var element : root.getAsJsonArray("allowed_items")) {
                    if (!element.isJsonPrimitive()
                            || !element.getAsJsonPrimitive().isString()) {
                        continue;
                    }

                    Identifier id = Identifier.tryParse(
                            element.getAsString()
                    );

                    if (id != null && Registries.ITEM.containsId(id)
                            && Registries.ITEM.get(id)
                            != net.minecraft.item.Items.AIR) {
                        loaded.add(id.toString());
                    }
                }
            }

            allowedItems = loaded;
        } catch (IOException | RuntimeException exception) {
            allowedItems = new LinkedHashSet<>(DEFAULT_ITEMS);
            System.err.println(
                    "[JarJarMod] Failed to load alchemy archive config: "
                            + exception.getMessage()
            );
        }
    }

    public static boolean isAllowed(String itemId) {
        return itemId != null && allowedItems.contains(itemId);
    }

    public static Set<String> getAllowedItems() {
        return Collections.unmodifiableSet(
                new LinkedHashSet<>(allowedItems)
        );
    }
}
