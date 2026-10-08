package net.jarjar.jarjarmod.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.jarjar.jarjarmod.JarJarMod;
import net.jarjar.jarjarmod.block.ModBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final ItemGroup RUBY_GROUP = Registry.register(Registries.ITEM_GROUP,
            new Identifier(JarJarMod.MOD_ID, "ruby"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.ruby"))
                    .icon(() -> new ItemStack(ModItems.RUBY)).entries((displayContext, entries) -> {

                        entries.add(ModItems.RUBY);
                        entries.add(ModItems.QUILL);
                        entries.add(ModBlocks.RUBY_BLOCK);
                        entries.add(ModBlocks.RUBY_ORE);
                        entries.add(ModBlocks.DEEPSLATE_RUBY_ORE);
                        entries.add(ModItems.METAL_DETECTOR);

                    }).build());

    public static void registerItemGroups(){
        JarJarMod.LOGGER.info("Registering Item Groups for " + JarJarMod.MOD_ID);
    }
}
