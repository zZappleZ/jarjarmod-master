package net.jarjar.jarjarmod.commands;

import net.jarjar.jarjarmod.component.ModPlayerData;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.jarjar.jarjarmod.component.handling.ArchivistPackets;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ArchivistTestCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("archivist")

                    // /archivist dump  -> prints every field to chat
                    .then(CommandManager.literal("dump")
                            .executes(ctx -> dumpData(ctx.getSource())))

                    // /archivist inkflow <amount>  -> sets InkflowCurrent
                    .then(CommandManager.literal("inkflow")
                            .then(CommandManager.literal("set")
                                    .then(CommandManager.argument("amount", IntegerArgumentType.integer(0))
                                            .executes(ctx -> {
                                                int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                                ServerPlayerEntity player = ctx.getSource().getPlayer();
                                                ModPlayerData data = (ModPlayerData) player;
                                                data.setInkflowCurrent(amount);
                                                ArchivistPackets.syncInkflow(player);
                                                ctx.getSource().sendFeedback(() ->
                                                        Text.literal("InkflowCurrent set to " + data.getInkflowCurrent()), false);
                                                return 1;
                                            })))
                            .then(CommandManager.literal("add")
                                    .then(CommandManager.argument("amount", IntegerArgumentType.integer()) // no min/max = allows negatives
                                            .executes(ctx -> {
                                                int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                                ServerPlayerEntity player = ctx.getSource().getPlayer();
                                                ModPlayerData data = (ModPlayerData) player;

                                                int before = data.getInkflowCurrent();
                                                data.setInkflowCurrent(before + amount); // setter still clamps to [0, capacity]
                                                ArchivistPackets.syncInkflow(player);

                                                int after = data.getInkflowCurrent();
                                                ctx.getSource().sendFeedback(() ->
                                                        Text.literal("Inkflow " + (amount >= 0 ? "+" : "") + amount +
                                                                " -> " + after + " / " + data.getInkflowCapacity()), false);
                                                return 1;
                                            }))))
                    .then(CommandManager.literal("tomeslot")
                            .then(CommandManager.argument("slot", IntegerArgumentType.integer(0, 2))
                                    .executes(ctx -> {
                                        int slot = IntegerArgumentType.getInteger(ctx, "slot");
                                        ModPlayerData data = getData(ctx.getSource());
                                        data.setActiveTomeSlot(slot);
                                        ctx.getSource().sendFeedback(() ->
                                                Text.literal("Active slot switched to " + slot + " (" + data.getActiveTome() + ")"), false);
                                        return 1;
                                    })))


                    // /archivist equip <slot 0-2> <tomeId>
                    .then(CommandManager.literal("equip")
                            .then(CommandManager.argument("slot", IntegerArgumentType.integer(0, 2))
                                    .then(CommandManager.argument("tomeId", StringArgumentType.string())
                                            .executes(ctx -> {
                                                int slot = IntegerArgumentType.getInteger(ctx, "slot");
                                                String tomeId = StringArgumentType.getString(ctx, "tomeId");
                                                ModPlayerData data = getData(ctx.getSource());

                                                boolean success = data.setTomeInSlot(slot, tomeId);
                                                if (success) {
                                                    ctx.getSource().sendFeedback(() ->
                                                            Text.literal("Slot " + slot + " set to " + tomeId), false);
                                                } else {
                                                    ctx.getSource().sendError(
                                                            Text.literal(tomeId + " is not unlocked. Use /archivist unlock first."));
                                                }
                                                return success ? 1 : 0;
                                            }))))

                    // /archivist cycle  -> moves ActiveTomeSlot to the next slot (wraps 0->1->2->0)
                    .then(CommandManager.literal("cycle")
                            .executes(ctx -> {
                                ModPlayerData data = getData(ctx.getSource());
                                int next = (data.getActiveTomeSlot() + 1) % 3;
                                data.setActiveTomeSlot(next);
                                ctx.getSource().sendFeedback(() ->
                                        Text.literal("Active slot now " + next + " (" + data.getActiveTome() + ")"), false);
                                return 1;
                            }))

                    .then(CommandManager.literal("unlock")
                            .then(CommandManager.argument("tomeId", StringArgumentType.string())
                                    .executes(ctx -> {
                                        String tomeId = StringArgumentType.getString(ctx, "tomeId");
                                        ModPlayerData data = getData(ctx.getSource());
                                        boolean added = data.unlockTome(tomeId);
                                        ctx.getSource().sendFeedback(() ->
                                                Text.literal(added ? "Unlocked " + tomeId : tomeId + " was already unlocked"), false);
                                        return 1;
                                    })))

                    .then(CommandManager.literal("redact")
                            .then(CommandManager.literal("toggle")
                                    .executes(ctx -> {
                                        ModPlayerData data = getData(ctx.getSource());
                                        boolean newState = !data.isRedactActive();
                                        data.setRedactActive(newState);
                                        ctx.getSource().sendFeedback(() ->
                                                Text.literal("Redact " + (newState ? "ACTIVE" : "inactive") +
                                                        " (replenishment: " + String.format("%.2f", data.getInkflowReplenishmentPerSecond()) + "/s)"), false);
                                        return 1;
                                    })))

                    // /archivist focus <script|inscribe|redact|manifest> <0-7000>
                    .then(CommandManager.literal("focus")
                            .then(CommandManager.argument("name", StringArgumentType.word())
                                    .then(CommandManager.argument("level", IntegerArgumentType.integer(0, 7000)) // was 700
                                            .executes(ctx -> {
                                                String name = StringArgumentType.getString(ctx, "name");
                                                int level = IntegerArgumentType.getInteger(ctx, "level");
                                                ServerPlayerEntity player = ctx.getSource().getPlayer();
                                                ModPlayerData data = (ModPlayerData) player;

                                                switch (name.toLowerCase()) {
                                                    case "script" -> data.setFocusScriptLevel(level);
                                                    case "inscribe" -> data.setFocusInscribeLevel(level);
                                                    case "redact" -> data.setFocusRedactLevel(level);
                                                    case "manifest" -> data.setFocusManifestLevel(level);
                                                    default -> {
                                                        ctx.getSource().sendError(Text.literal("Unknown focus: " + name));
                                                        return 0;
                                                    }
                                                }

                                                ArchivistPackets.syncInkflow(player); // capacity may have changed
                                                ctx.getSource().sendFeedback(() ->
                                                        Text.literal("Set " + name + " focus to " + level +
                                                                " (capacity now " + data.getInkflowCapacity() + ")"), false);
                                                return 1;
                                            })))));

        });
    }

    private static ModPlayerData getData(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer(); // null if run from console
        return (ModPlayerData) player;
    }

    private static int dumpData(ServerCommandSource source) {
        ModPlayerData data = getData(source);

        if (data == null) {
            source.sendError(Text.literal("This command must be run by a player."));
            return 0;
        }

        source.sendFeedback(() -> Text.literal("=== Archivist Data ==="), false);
        source.sendFeedback(() -> Text.literal("Inkflow: " + data.getInkflowCurrent() + " / " + data.getInkflowCapacity()), false);
        source.sendFeedback(() -> Text.literal("Equipped Tomes: " + data.getEquippedTomes()), false);
        source.sendFeedback(() -> Text.literal("Active Slot: " + data.getActiveTomeSlot() + " -> " + data.getActiveTome()), false);
        source.sendFeedback(() -> Text.literal("Focus - Script: " + data.getFocusScriptLevel()), false);
        source.sendFeedback(() -> Text.literal("Focus - Inscribe: " + data.getFocusInscribeLevel()), false);
        source.sendFeedback(() -> Text.literal("Focus - Redact: " + data.getFocusRedactLevel()), false);
        source.sendFeedback(() -> Text.literal("Focus - Manifest: " + data.getFocusManifestLevel()), false);
        source.sendFeedback(() -> Text.literal("Unlocked Tomes: " + data.getUnlockedTomes()), false);
        source.sendFeedback(() -> Text.literal("Redact Active: " + data.isRedactActive()), false);
        source.sendFeedback(() -> Text.literal("Replenishment: " + String.format("%.2f", data.getInkflowReplenishmentPerSecond()) + "/s"), false);
        return 1;
    }
}