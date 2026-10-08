package net.jarjar.jarjarmod.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.jarjar.jarjarmod.component.ModComponents;
import net.jarjar.jarjarmod.component.powersuppression.SuppressionComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class SuppressionCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("suppresspowers")
                    .then(CommandManager.literal("true")
                            .executes(ctx -> {
                                ServerCommandSource source = ctx.getSource();

                                if (!(source.getEntity() instanceof PlayerEntity player)) {
                                    source.sendError(Text.literal("Only players can use this command!"));
                                    return 0;
                                }

                                SuppressionComponent suppression = ModComponents.SUPPRESSION.get(player);
                                suppression.setSuppressed(true);

                                source.sendFeedback(
                                        () -> Text.literal("Power suppression enabled."),
                                        false
                                );

                                return 1;
                            }))
                    .then(CommandManager.literal("false")
                            .executes(ctx -> {
                                ServerCommandSource source = ctx.getSource();

                                if (!(source.getEntity() instanceof PlayerEntity player)) {
                                    source.sendError(Text.literal("Only players can use this command!"));
                                    return 0;
                                }

                                SuppressionComponent suppression = ModComponents.SUPPRESSION.get(player);
                                suppression.setSuppressed(false);

                                source.sendFeedback(
                                        () -> Text.literal("Power suppression disabled."),
                                                false
                                );

                                        return 1;
                            })));
        });
    }
}