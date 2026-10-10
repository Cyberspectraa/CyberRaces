package com.cyberspectraa.cyberraces.command;

import com.cyberspectraa.cyberraces.progression.ProgressionManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class CyberProgressionCommands {
    private CyberProgressionCommands() {
    }

    public static void register(
        CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        dispatcher.register(
            Commands.literal("cyberraces")
                .then(Commands.literal("level")
                .then(
                    Commands.literal("status")
                        .executes(context -> status(
                            context.getSource(),
                            context.getSource().getPlayerOrException()
                        ))
                        .then(
                            Commands.argument(
                                    "player",
                                    EntityArgument.player()
                                )
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> status(
                                    context.getSource(),
                                    EntityArgument.getPlayer(
                                        context,
                                        "player"
                                    )
                                ))
                        )
                )
                .then(
                    Commands.literal("addxp")
                        .requires(source -> source.hasPermission(2))
                        .then(
                            Commands.argument(
                                    "player",
                                    EntityArgument.player()
                                )
                                .then(
                                    Commands.argument(
                                            "amount",
                                            IntegerArgumentType.integer(
                                                1,
                                                1_000_000
                                            )
                                        )
                                        .executes(context -> addXp(
                                            context.getSource(),
                                            EntityArgument.getPlayer(
                                                context,
                                                "player"
                                            ),
                                            IntegerArgumentType.getInteger(
                                                context,
                                                "amount"
                                            )
                                        ))
                                )
                        )
                )
                .then(
                    Commands.literal("setlevel")
                        .requires(source -> source.hasPermission(2))
                        .then(
                            Commands.argument(
                                    "player",
                                    EntityArgument.player()
                                )
                                .then(
                                    Commands.argument(
                                            "level",
                                            IntegerArgumentType.integer(
                                                1,
                                                ProgressionManager.MAX_LEVEL
                                            )
                                        )
                                        .executes(context -> setLevel(
                                            context.getSource(),
                                            EntityArgument.getPlayer(
                                                context,
                                                "player"
                                            ),
                                            IntegerArgumentType.getInteger(
                                                context,
                                                "level"
                                            )
                                        ))
                                )
                        )
                )
                )
        );
    }

    private static int status(
        CommandSourceStack source,
        ServerPlayer player
    ) {
        int level = ProgressionManager.getLevel(player);
        long into = ProgressionManager.getExperienceIntoLevel(player);
        long needed = ProgressionManager.getExperienceForNextLevel(player);
        long total = ProgressionManager.getTotalExperience(player);

        source.sendSuccess(
            () -> Component.literal(
                player.getGameProfile().getName()
                    + " — Cyber Level " + level
            ).withStyle(ChatFormatting.GOLD),
            false
        );

        if (needed > 0L) {
            source.sendSuccess(
                () -> Component.literal(
                    "XP: " + into + " / " + needed
                        + " | Total: " + total
                ).withStyle(ChatFormatting.YELLOW),
                false
            );
        } else {
            source.sendSuccess(
                () -> Component.literal(
                    "Maximum Cyber Level reached."
                ).withStyle(ChatFormatting.YELLOW),
                false
            );
        }

        return level;
    }

    private static int addXp(
        CommandSourceStack source,
        ServerPlayer player,
        int amount
    ) {
        var change = ProgressionManager.addExperience(
            player,
            amount,
            "command"
        );

        source.sendSuccess(
            () -> Component.literal(
                "Added " + change.experienceAdded()
                    + " Cyber XP to "
                    + player.getGameProfile().getName()
                    + ". Level=" + change.newLevel()
            ),
            true
        );

        return change.newLevel();
    }

    private static int setLevel(
        CommandSourceStack source,
        ServerPlayer player,
        int level
    ) {
        ProgressionManager.setLevel(player, level);

        source.sendSuccess(
            () -> Component.literal(
                "Set " + player.getGameProfile().getName()
                    + " to Cyber Level " + level + "."
            ),
            true
        );

        return level;
    }
}
