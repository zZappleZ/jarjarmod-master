package net.jarjar.jarjarmod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.jarjar.jarjarmod.block.ModBlocks;
import net.jarjar.jarjarmod.commands.ArchivistTestCommands;
import net.jarjar.jarjarmod.component.DomainComponent;
import net.jarjar.jarjarmod.component.handling.ArchivistDataTransfer;
import net.jarjar.jarjarmod.component.handling.ArchivistPackets;
import net.jarjar.jarjarmod.item.ModItemGroups;
import net.jarjar.jarjarmod.item.ModItems;
import net.jarjar.jarjarmod.system.InkflowReplenishmentHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarJarMod implements ModInitializer {
	public static final String MOD_ID = "jarjarmod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ModItemGroups.registerItemGroups();
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ArchivistTestCommands.register();
		ArchivistDataTransfer.register();
		InkflowReplenishmentHandler.register();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("checkvalue")
					.executes(ctx -> {
						ServerCommandSource source = ctx.getSource();
						Entity sender = source.getEntity();

						if (!(sender instanceof PlayerEntity player)) {
							source.sendError(Text.literal("Only players can use this command!"));
							return 0;
						}

						// Try to get the component
						DomainComponent domain = ModComponents.DOMAIN.getNullable(player);

						if (domain == null) {
							source.sendFeedback(() -> Text.literal("No component attached to player!"), false);
						} else {
							source.sendFeedback(() -> Text.literal("component present: " + domain.getValue()), false);
						}

						return 1;
					}));
			dispatcher.register(CommandManager.literal("addvalue")
					.then(CommandManager.argument("amount", IntegerArgumentType.integer())
							.executes( ctx -> {
								ServerCommandSource source = ctx.getSource();
								Entity sender = source.getEntity();

								if (!(sender instanceof PlayerEntity player)) {
									source.sendError(Text.literal("Only players can use this command!"));
									return 0;
								}

								// Try to get the component
								DomainComponent domain = ModComponents.DOMAIN.getNullable(player);
								if (domain == null) {
									source.sendFeedback(() -> Text.literal("No component attached to player!"), false);
								} else {
									// Get the component and modify
									ModComponents.DOMAIN.get(player).addMana(IntegerArgumentType.getInteger(ctx, "amount"));
								}
								return 1;
							})));

		});

		//HUD Sync
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ArchivistPackets.syncInkflow(handler.player);
		});
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer,newPlayer,alive) -> {
			ArchivistPackets.syncInkflow(newPlayer);
		});
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			ArchivistPackets.syncInkflow(newPlayer);
		});
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player,oldWorld,newWorld) -> {
				ArchivistPackets.syncInkflow(player);
		});

		LOGGER.info("Hello Fabric world!");
	}
}