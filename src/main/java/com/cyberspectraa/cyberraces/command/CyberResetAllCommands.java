package com.cyberspectraa.cyberraces.command;

import com.cyberspectraa.cyberraces.character.CharacterManager;
import com.cyberspectraa.cyberraces.progression.ProgressionManager;
import com.cyberspectraa.cyberraces.race.RaceEvolutionManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import java.lang.reflect.Method;

/** Admin-only testing reset: race, appearance, class, both evolutions and levels. */
public final class CyberResetAllCommands {
    private CyberResetAllCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // The full spelling and commonly mistyped version are both accepted.
        // Commands must still be run by an operator (permission level 2).
        registerNamed(dispatcher, "cyberresetall");
        registerNamed(dispatcher, "cyberresetal");
    }

    private static void registerNamed(CommandDispatcher<CommandSourceStack> dispatcher,
                                       String name) {
        dispatcher.register(Commands.literal(name)
            .requires(source -> source.hasPermission(2))
            .executes(ctx -> reset(ctx.getSource(), ctx.getSource().getPlayerOrException()))
            .then(Commands.argument("player", EntityArgument.player())
                .executes(ctx -> reset(ctx.getSource(),
                    EntityArgument.getPlayer(ctx, "player")))));
    }

    private static int reset(CommandSourceStack source, ServerPlayer player) {
        // Release any in-progress Pope greeting and re-arm the first-arrival
        // introduction before reopening character creation.
        invoke("cybernpc", "com.cyberspectraa.cybernpc.intro.CyberIntroService",
               "resetForRetest", player);

        // Clear all class data, including advancement and quest unlocks.
        // CyberClasses remains optional when CyberRaces runs on its own.
        invoke("cyberclasses", "com.cyberspectraa.cyberclasses.classdata.ClassCreationManager",
               "onLogout", player);
        invoke("cyberclasses", "com.cyberspectraa.cyberclasses.classdata.ClassAdvancementManager",
               "onLogout", player);
        player.getPersistentData().remove("CyberClasses");
        player.getPersistentData().remove("ClassLevel");

        // CyberClasses reads the common CyberProgression level and XP.
        player.getPersistentData().remove(ProgressionManager.ROOT_KEY);
        ProgressionManager.setLevel(player, 1);
        RaceEvolutionManager.onLogout(player);

        // Restore vanilla XP as well for a genuinely fresh testing state.
        player.experienceLevel = 0;
        player.totalExperience = 0;
        player.experienceProgress = 0F;

        CharacterManager.resetCharacter(player);
        source.sendSuccess(() -> Component.literal(
            "Reset " + player.getGameProfile().getName()
                + ": race, appearance, class, class advancement, race evolution,"
                + " Cyber Level/XP, class unlocks and vanilla XP. Character creator reopened."
        ), true);
        return 1;
    }

    private static void invoke(String mod, String clazz, String name, ServerPlayer player) {
        if (!ModList.get().isLoaded(mod)) return;
        try {
            Class<?> target = Class.forName(clazz);
            Method method = target.getMethod(name, ServerPlayer.class);
            method.invoke(null, player);
        } catch (ReflectiveOperationException | RuntimeException err) {
            com.cyberspectraa.cyberraces.CyberRaces.LOGGER.warn(
                    "Optional reset hook {}.{} failed for {}", clazz, name,
                    player.getGameProfile().getName(), err);
        }
    }
}
