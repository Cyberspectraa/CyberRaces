package com.cyberspectraa.cyberraces.race;

import com.cyberspectraa.cyberraces.character.CharacterSyncService;
import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.CloseRaceEvolutionPacket;
import com.cyberspectraa.cyberraces.network.packet.OpenRaceEvolutionPacket;
import com.cyberspectraa.cyberraces.progression.ProgressionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simple evolution: reach Cyber Level 30 and sleep in a bed.
 * The client can choose any allowed evolution for its existing base race;
 * the server validates the exact choice against the pending race.
 */
public final class RaceEvolutionManager {
    private static final int MIN_SLEEP_TICKS = 40;
    private static final int OFFER_DELAY_TICKS = 8;
    private static final Map<UUID, Long> SLEEP_STARTED = new HashMap<>();
    private static final Map<UUID, Long> WAKE_OFFER_DUE = new HashMap<>();
    private static final Map<UUID, Race> PENDING = new HashMap<>();

    private RaceEvolutionManager() {}

    public static void handleLogin(ServerPlayer player) {
        if (player != null) {
            clearSession(player.getUUID());
            RaceManager.getEvolution(player); // Discard old, unsupported saved paths.
        }
    }

    public static void tick(ServerPlayer player) {
        if (player == null) return;
        UUID id = player.getUUID();
        long now = player.serverLevel().getGameTime();

        if (player.isSleeping()) {
            SLEEP_STARTED.putIfAbsent(id, now);
            WAKE_OFFER_DUE.remove(id);
            return;
        }

        Long began = SLEEP_STARTED.remove(id);
        if (began != null && now - began >= MIN_SLEEP_TICKS) {
            WAKE_OFFER_DUE.put(id, now + OFFER_DELAY_TICKS);
        }

        Long due = WAKE_OFFER_DUE.get(id);
        if (due != null && now >= due) {
            WAKE_OFFER_DUE.remove(id);
            offerAfterRest(player);
        }
    }

    public static boolean eligible(ServerPlayer player) {
        if (player == null || RaceManager.isAcquiredUndead(player)
                || ProgressionManager.getLevel(player) < RaceEvolution.REQUIRED_LEVEL
                || RaceManager.getEvolution(player).isPresent()) return false;
        Race race = RaceManager.getBaseRace(player).orElse(null);
        return race != null && RaceEvolution.choicesFor(race).length > 0;
    }

    private static void offerAfterRest(ServerPlayer player) {
        if (player.isSleeping() || PENDING.containsKey(player.getUUID())
                || !eligible(player)) return;

        Race race = RaceManager.getBaseRace(player).orElse(null);
        RaceEvolution[] choices = RaceEvolution.choicesFor(race);
        if (choices.length == 0) return;

        PENDING.put(player.getUUID(), race);
        // The first valid ID establishes the race; the screen shows every
        // option for that race (both High and Dark Elf, for instance).
        CyberRacesNetwork.sendToPlayer(player,
                new OpenRaceEvolutionPacket(choices[0].id()));
    }

    public static void openSelection(ServerPlayer player) {
        if (player == null) return;
        if (RaceManager.getEvolution(player).isPresent()) {
            player.sendSystemMessage(Component.literal("You've already evolved."));
        } else if (RaceEvolution.choicesFor(
                RaceManager.getBaseRace(player).orElse(null)).length == 0) {
            player.sendSystemMessage(Component.literal("Your race has no evolution."));
        } else {
            player.sendSystemMessage(Component.literal(
                "Reach Cyber Level 30, then sleep in a bed to choose your evolution.")
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    public static boolean complete(ServerPlayer player, RaceEvolution evolution) {
        if (player == null || evolution == null || !eligible(player)) return false;
        Race pendingRace = PENDING.get(player.getUUID());
        if (pendingRace == null || evolution.baseRace() != pendingRace
                || RaceManager.getBaseRace(player).orElse(null) != pendingRace
                || !RaceManager.setEvolution(player, evolution)) return false;

        PENDING.remove(player.getUUID());
        CharacterSyncService.broadcast(player);
        CyberRacesNetwork.sendToPlayer(player, new CloseRaceEvolutionPacket());
        player.serverLevel().sendParticles(ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1.0D, player.getZ(),
                70, 0.65D, 0.85D, 0.65D, 0.035D);
        player.serverLevel().playSound(null, player.blockPosition(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9F, 1.15F);
        player.sendSystemMessage(Component.literal("Evolved into ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(evolution.displayName())
                        .withStyle(ChatFormatting.YELLOW)));
        return true;
    }

    public static void decline(ServerPlayer player) {
        if (player == null) return;
        if (PENDING.remove(player.getUUID()) != null) {
            player.sendSystemMessage(Component.literal(
                    "Evolution postponed. Sleep again whenever you're ready.")
                    .withStyle(ChatFormatting.AQUA));
        }
        CyberRacesNetwork.sendToPlayer(player, new CloseRaceEvolutionPacket());
    }

    public static void forceSet(ServerPlayer player, RaceEvolution evolution) {
        if (player == null || evolution == null) return;
        RaceManager.forceSetEvolution(player, evolution);
        clearSession(player.getUUID());
        CharacterSyncService.broadcast(player);
        CyberRacesNetwork.sendToPlayer(player, new CloseRaceEvolutionPacket());
    }

    public static void reset(ServerPlayer player) {
        if (player == null) return;
        clearSession(player.getUUID());
        RaceManager.clearEvolution(player);
        CharacterSyncService.broadcast(player);
        CyberRacesNetwork.sendToPlayer(player, new CloseRaceEvolutionPacket());
    }

    public static void onLogout(ServerPlayer player) {
        if (player != null) clearSession(player.getUUID());
    }

    private static void clearSession(UUID id) {
        SLEEP_STARTED.remove(id);
        WAKE_OFFER_DUE.remove(id);
        PENDING.remove(id);
    }
}
