package com.cyberspectraa.cyberraces.race;

import com.cyberspectraa.cyberraces.character.CharacterSyncService;
import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.CloseRaceEvolutionPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Dynamic evolution replaces the unconditional Level 30 selection GUI.
 * Server-side activity and context determine the earned path; existing
 * evolved players and admin force-set paths stay intact.
 */
public final class RaceEvolutionManager {
    private RaceEvolutionManager() {}

    public static void handleLogin(ServerPlayer player) {
        // No forced selection screen. Existing evolutions are preserved.
    }

    public static void tick(ServerPlayer player) {
        if (player != null && player.tickCount % 100 == 0) {
            RaceEvolutionJourney.tick(player);
        }
    }

    public static boolean eligible(ServerPlayer player) {
        return RaceEvolutionJourney.ready(player);
    }

    public static void openSelection(ServerPlayer player) {
        if (player != null) player.sendSystemMessage(
                Component.literal("Race evolution is earned through activity, not selected at a level-up. "
                        + "Use /cyberraces evolution progress to view your affinities.")
                        .withStyle(ChatFormatting.AQUA));
    }

    /** Legacy client packets cannot bypass the new earned-evolution system. */
    public static boolean complete(ServerPlayer player, RaceEvolution evolution) {
        return false;
    }

    public static void forceSet(ServerPlayer player, RaceEvolution evolution) {
        if (player == null || evolution == null) return;
        RaceManager.forceSetEvolution(player, evolution);
        CharacterSyncService.broadcast(player);
        CyberRacesNetwork.sendToPlayer(player, new CloseRaceEvolutionPacket());
    }

    public static void reset(ServerPlayer player) {
        if (player == null) return;
        RaceManager.clearEvolution(player);
        RaceEvolutionJourney.reset(player);
        CharacterSyncService.broadcast(player);
        CyberRacesNetwork.sendToPlayer(player, new CloseRaceEvolutionPacket());
    }

    public static void onLogout(ServerPlayer player) {
        // All journey data is persisted with the character.
    }
}
