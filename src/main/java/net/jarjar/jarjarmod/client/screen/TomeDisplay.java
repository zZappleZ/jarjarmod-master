
package net.jarjar.jarjarmod.client.screen;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public final class TomeDisplay {

    private TomeDisplay() {
    }

    public static ItemStack getIcon(String tomeId) {
        if (tomeId == null || tomeId.isEmpty()) {
            return ItemStack.EMPTY;
        }

        return switch (tomeId) {
            case "minecraft:signed_book" ->
                    new ItemStack(Items.WRITTEN_BOOK);

            case "minecraft:fire_aspect",
                 "minecraft:sharpness",
                 "minecraft:knockback",
                 "minecraft:sweeping" ->
                    new ItemStack(Items.ENCHANTED_BOOK);

            default -> new ItemStack(Items.BOOK);
        };
    }

    public static Text getName(String tomeId) {
        if (tomeId == null || tomeId.isEmpty()) {
            return Text.literal("Empty Slot");
        }

        String path = tomeId.contains(":")
                ? tomeId.substring(tomeId.indexOf(':') + 1)
                : tomeId;

        String[] words = path.replace('_', ' ').split(" ");
        StringBuilder name = new StringBuilder();

        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }

            if (name.length() > 0) {
                name.append(' ');
            }

            name.append(Character.toUpperCase(word.charAt(0)));

            if (word.length() > 1) {
                name.append(word.substring(1));
            }
        }

        return Text.literal(name.toString());
    }
}
