package com.cyberspectraa.cyberraces.race;

import com.cyberspectraa.cyberraces.character.CharacterSyncService;
import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.CloseRaceEvolutionPacket;
import com.cyberspectraa.cyberraces.network.packet.OpenRaceEvolutionPacket;
import com.cyberspectraa.cyberraces.progression.ProgressionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class RaceEvolutionManager {
    private static final Set<UUID> PROMPTED = new HashSet<>();

    private RaceEvolutionManager() {
    }

    public static void handleLogin(ServerPlayer player) {
        if (player == null) {
            return;
        }

        PROMPTED.remove(player.getUUID());
        tryPrompt(player);
    }

    public static void tick(ServerPlayer player) {
        if (player == null
                || player.tickCount % 20 != 0
                || PROMPTED.contains(player.getUUID())) {
            return;
        }

        tryPrompt(player);
    }

    public static boolean eligible(ServerPlayer player) {
        return player != null
            && RaceManager.hasRace(player)
            && RaceManager.getEvolution(player).isEmpty()
            && ProgressionManager.getLevel(player)
                >= RaceEvolution.REQUIRED_LEVEL;
    }

    public static void openSelection(ServerPlayer player) {
        Race race = RaceManager.getRace(player).orElse(null);

        if (race == null || !eligible(player)) {
            return;
        }

        PROMPTED.add(player.getUUID());
        CyberRacesNetwork.sendToPlayer(
            player,
            new OpenRaceEvolutionPacket(race.id())
        );
    }

    public static boolean complete(
        ServerPlayer player,
        RaceEvolution evolution
    ) {
        if (!eligible(player)
                || !RaceManager.setEvolution(player, evolution)) {
            return false;
        }

        PROMPTED.remove(player.getUUID());
        CharacterSyncService.broadcast(player);
        CyberRacesNetwork.sendToPlayer(
            player,
            new CloseRaceEvolutionPacket()
        );

        player.sendSystemMessage(
            Component.literal("Race Evolved: ")
                .withStyle(ChatFormatting.GOLD)
                .append(
                    Component.literal(evolution.displayName())
                        .withStyle(ChatFormatting.YELLOW)
                )
        );

        return true;
    }

    public static void forceSet(
        ServerPlayer player,
        RaceEvolution evolution
    ) {
        if (player == null || evolution == null) {
            return;
        }

        RaceManager.forceSetEvolution(player, evolution);
        PROMPTED.remove(player.getUUID());
        CharacterSyncService.broadcast(player);
        CyberRacesNetwork.sendToPlayer(
            player,
            new CloseRaceEvolutionPacket()
        );
    }

    public static void reset(ServerPlayer player) {
        if (player == null) {
            return;
        }

        RaceManager.clearEvolution(player);
        PROMPTED.remove(player.getUUID());
        CharacterSyncService.broadcast(player);

        if (eligible(player)) {
            openSelection(player);
        }
    }

    public static void onLogout(ServerPlayer player) {
        if (player != null) {
            PROMPTED.remove(player.getUUID());
        }
    }

    private static void tryPrompt(ServerPlayer player) {
        if (eligible(player)) {
            openSelection(player);
        }
    }
}
