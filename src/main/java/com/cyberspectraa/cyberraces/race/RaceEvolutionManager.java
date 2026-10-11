package com.cyberspectraa.cyberraces.race;

import com.cyberspectraa.cyberraces.character.CharacterSyncService;
import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.CloseRaceEvolutionPacket;
import com.cyberspectraa.cyberraces.network.packet.OpenRaceEvolutionPacket;
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
 * Level 30 and affinity thresholds make a player eligible, but a race never
 * evolves until the player sleeps and accepts the personal recommendation.
 * Waiting/declining is fully safe for an existing character and their world.
 */
public final class RaceEvolutionManager {
    private static final int MIN_SLEEP_TICKS = 40;
    private static final int OFFER_DELAY_TICKS = 8;
    private static final Map<UUID, Long> SLEEP_STARTED = new HashMap<>();
    private static final Map<UUID, Long> WAKE_OFFER_DUE = new HashMap<>();
    private static final Map<UUID, RaceEvolution> PENDING = new HashMap<>();

    private RaceEvolutionManager() {}

    public static void handleLogin(ServerPlayer player) {
        if (player != null) clearSession(player.getUUID());
    }

    public static void tick(ServerPlayer player) {
        if (player == null) return;
        if (player.tickCount % 100 == 0) RaceEvolutionJourney.tick(player);

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
        return RaceEvolutionJourney.ready(player);
    }

    private static void offerAfterRest(ServerPlayer player) {
        if (player.isSleeping() || PENDING.containsKey(player.getUUID())) return;
        RaceEvolution recommended = RaceEvolutionJourney.recommendation(player);
        if (recommended == null) return;
        PENDING.put(player.getUUID(), recommended);
        CyberRacesNetwork.sendToPlayer(player,
                new OpenRaceEvolutionPacket(recommended.id()));
    }

    /** Useful for debugging; does not grant a free evolution choice. */
    public static void openSelection(ServerPlayer player) {
        if (player == null) return;
        player.sendSystemMessage(Component.literal(
                "Once your racial affinities are ready, sleep in a bed to discover "
                + "your recommended evolution. You may accept or wait.")
                .withStyle(ChatFormatting.AQUA));
    }

    /** Called by the player's acceptance response, with strict server checks. */
    public static boolean complete(ServerPlayer player, RaceEvolution evolution) {
        if (player == null || evolution == null) return false;
        RaceEvolution pending = PENDING.get(player.getUUID());
        if (pending != evolution || !eligible(player)
                || !RaceManager.setEvolution(player, evolution)) return false;

        PENDING.remove(player.getUUID());
        CharacterSyncService.broadcast(player);
        CyberRacesNetwork.sendToPlayer(player, new CloseRaceEvolutionPacket());
        player.serverLevel().sendParticles(ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1.0D, player.getZ(),
                70, 0.65D, 0.85D, 0.65D, 0.035D);
        player.serverLevel().playSound(null, player.blockPosition(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9F, 1.15F);
        player.sendSystemMessage(Component.literal("Your lineage has evolved: ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(evolution.displayName())
                        .withStyle(ChatFormatting.YELLOW)));
        return true;
    }

    /** A decline expires this proposal, without resetting earned affinities. */
    public static void decline(ServerPlayer player) {
        if (player == null) return;
        if (PENDING.remove(player.getUUID()) != null) {
            player.sendSystemMessage(Component.literal(
                    "You chose not to evolve. Your affinity progress is safe. "
                    + "Play differently to influence your next offer, then sleep again.")
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
        RaceEvolutionJourney.reset(player);
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
