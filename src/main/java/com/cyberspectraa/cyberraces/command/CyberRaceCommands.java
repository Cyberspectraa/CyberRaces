package com.cyberspectraa.cyberraces.command;

import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import com.cyberspectraa.cyberraces.character.CharacterManager;
import com.cyberspectraa.cyberraces.compat.IcarusCompat;
import com.cyberspectraa.cyberraces.compat.IronSpellsCompat;
import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class CyberRaceCommands {
    private static final SuggestionProvider<CommandSourceStack> RACE_SUGGESTIONS =
        (context, builder) -> {
            for (Race race : Race.values()) {
                builder.suggest(race.id());
            }
            return builder.buildFuture();
        };

    private CyberRaceCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("cyberraces")
                .then(Commands.literal("race")
                    .then(Commands.literal("list")
                        .executes(context -> list(context.getSource())))
                    .then(Commands.literal("get")
                        .executes(context -> getSelf(context.getSource()))
                        .then(Commands.argument("player", EntityArgument.player())
                            .executes(context -> get(
                                context.getSource(),
                                EntityArgument.getPlayer(context, "player")
                            ))))
                    .then(Commands.literal("info")
                        .then(Commands.argument("race", StringArgumentType.word())
                            .suggests(RACE_SUGGESTIONS)
                            .executes(context -> info(
                                context.getSource(),
                                StringArgumentType.getString(context, "race")
                            ))))
                    .then(Commands.literal("choose")
                        .then(Commands.argument("race", StringArgumentType.word())
                            .suggests(RACE_SUGGESTIONS)
                            .executes(context -> choose(
                                context.getSource(),
                                StringArgumentType.getString(context, "race")
                            ))))
                    .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                            .then(Commands.argument("race", StringArgumentType.word())
                                .suggests(RACE_SUGGESTIONS)
                                .executes(context -> set(
                                    context.getSource(),
                                    EntityArgument.getPlayer(context, "player"),
                                    StringArgumentType.getString(context, "race")
                                )))))
                    .then(Commands.literal("clear")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                            .executes(context -> resetCharacter(
                                context.getSource(),
                                EntityArgument.getPlayer(context, "player")
                            ))))
                    .then(Commands.literal("compat")
                        .executes(context -> compat(context.getSource())))
                )
                .then(Commands.literal("character")
                    .then(Commands.literal("reset")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                            .executes(context -> resetCharacter(
                                context.getSource(),
                                EntityArgument.getPlayer(context, "player")
                            ))))
                    .then(Commands.literal("status")
                        .executes(context -> characterStatus(
                            context.getSource(),
                            context.getSource().getPlayerOrException()
                        ))
                        .then(Commands.argument("player", EntityArgument.player())
                            .requires(source -> source.hasPermission(2))
                            .executes(context -> characterStatus(
                                context.getSource(),
                                EntityArgument.getPlayer(context, "player")
                            ))))
                )
        );
    }

    private static int list(CommandSourceStack source) {
        String races = Arrays.stream(Race.values())
            .map(Race::id)
            .collect(Collectors.joining(", "));

        source.sendSuccess(
            () -> Component.literal("CyberRaces: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(races).withStyle(ChatFormatting.YELLOW)),
            false
        );
        return Race.values().length;
    }

    private static int getSelf(CommandSourceStack source) {
        try {
            return get(source, source.getPlayerOrException());
        } catch (Exception ignored) {
            source.sendFailure(Component.literal("This command needs a player."));
            return 0;
        }
    }

    private static int get(CommandSourceStack source, ServerPlayer player) {
        RaceManager.getRace(player).ifPresentOrElse(
            race -> source.sendSuccess(
                () -> Component.literal(player.getGameProfile().getName() + " is a " + race.displayName() + "."),
                false
            ),
            () -> source.sendSuccess(
                () -> Component.literal(player.getGameProfile().getName() + " has not chosen a race."),
                false
            )
        );
        return 1;
    }

    private static int info(CommandSourceStack source, String raceId) {
        Race race = Race.byId(raceId).orElse(null);
        if (race == null) {
            source.sendFailure(Component.literal("Unknown race: " + raceId));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(race.displayName()).withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal(
            String.format(
                "Scale %.2fx | Health %.0f | Speed %.0f%% | Mana %.0f%% | Mana regen %.0f%% | Spell resist %.0f%%",
                race.scale(),
                race.maxHealth(),
                race.movementMultiplier() * 100.0,
                race.maxManaMultiplier() * 100.0,
                race.manaRegenMultiplier() * 100.0,
                race.spellResistanceMultiplier() * 100.0
            )
        ), false);

        return 1;
    }

    private static int choose(CommandSourceStack source, String raceId) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception ignored) {
            source.sendFailure(Component.literal("Only a player can choose a race."));
            return 0;
        }

        if (CharacterManager.isCharacterCreated(player)) {
            source.sendFailure(Component.literal(
                "Your character is already complete. An admin must reset or change it."
            ));
            return 0;
        }

        Race race = Race.byId(raceId).orElse(null);
        if (race == null) {
            source.sendFailure(Component.literal("Unknown race: " + raceId));
            return 0;
        }

        CharacterManager.completeCharacter(player, race, CharacterAppearance.defaults());
        source.sendSuccess(
            () -> Component.literal("You are now a " + race.displayName() + ".").withStyle(ChatFormatting.GREEN),
            false
        );

        return 1;
    }

    private static int set(CommandSourceStack source, ServerPlayer player, String raceId) {
        Race race = Race.byId(raceId).orElse(null);
        if (race == null) {
            source.sendFailure(Component.literal("Unknown race: " + raceId));
            return 0;
        }

        CharacterManager.forceComplete(player, race, CharacterManager.getAppearance(player));
        source.sendSuccess(
            () -> Component.literal("Set " + player.getGameProfile().getName() + "'s race to " + race.displayName() + "."),
            true
        );
        return 1;
    }

    private static int resetCharacter(CommandSourceStack source, ServerPlayer player) {
        CharacterManager.resetCharacter(player);
        source.sendSuccess(
            () -> Component.literal("Reset " + player.getGameProfile().getName() + "'s CyberRaces character creator."),
            true
        );
        return 1;
    }

    private static int characterStatus(CommandSourceStack source, ServerPlayer player) {
        boolean complete = CharacterManager.isCharacterCreated(player);
        CharacterAppearance appearance = CharacterManager.getAppearance(player);

        source.sendSuccess(
            () -> Component.literal(
                player.getGameProfile().getName()
                    + " character complete=" + complete
                    + " | feature=" + appearance.featureStyle()
            ),
            false
        );
        return 1;
    }

    private static int compat(CommandSourceStack source) {
        source.sendSuccess(
            () -> Component.literal("Iron's Spells: " + (IronSpellsCompat.isLoaded() ? "loaded" : "not loaded")),
            false
        );
        source.sendSuccess(
            () -> Component.literal("Icarus: " + (IcarusCompat.isLoaded() ? "loaded" : "not loaded")),
            false
        );
        return 1;
    }
}
