package com.cyberspectraa.cyberraces.race;

import com.cyberspectraa.cyberraces.character.CharacterSyncService;
import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.CloseRaceEvolutionPacket;
import com.cyberspectraa.cyberraces.network.packet.OpenRaceEvolutionPacket;
import com.cyberspectraa.cyberraces.progression.ProgressionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Sleep at Cyber Level 30 -> a short in-world awakening with particles,
 * sounds and Minecraft title text. Only Elves see a two-choice prompt.
 * Races with exactly one path evolve automatically at the end.
 */
public final class RaceEvolutionManager {
    private static final int MIN_SLEEP_TICKS = 1;
    private static final int OFFER_DELAY_TICKS = 8;
    private static final int AWAKENING_TICKS = 48;
    private static final Map<UUID, Long> SLEEP_STARTED = new HashMap<>();
    private static final Map<UUID, Long> WAKE_OFFER_DUE = new HashMap<>();
    private static final Map<UUID, Race> PENDING = new HashMap<>();
    private static final Map<UUID, Long> AWAKENING_STARTED = new HashMap<>();

    private RaceEvolutionManager() {}

    public static void handleLogin(ServerPlayer player) {
        if (player != null) {
            clearSession(player.getUUID());
            RaceManager.getEvolution(player);
        }
    }

    public static void tick(ServerPlayer player) {
        if (player == null) return;
        UUID id = player.getUUID();
        long now = player.serverLevel().getGameTime();

        // A pending Elf choice remains valid until selected or dismissed.
        if (PENDING.containsKey(id)) {
            Long started = AWAKENING_STARTED.get(id);
            if (started != null) {
                if (!player.isAlive() || player.isSleeping()) {
                    clearSession(id);
                    return;
                }
                int tick = (int) Math.max(0L, now - started);
                if (tick >= AWAKENING_TICKS) {
                    AWAKENING_STARTED.remove(id);
                    finishAwakening(player);
                } else {
                    animate(player, tick);
                }
            }
            return;
        }

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
        if (player == null || !player.isAlive()
                || RaceManager.isAcquiredUndead(player)
                || ProgressionManager.getLevel(player) < RaceEvolution.REQUIRED_LEVEL
                || RaceManager.getEvolution(player).isPresent()) return false;
        Race race = RaceManager.getBaseRace(player).orElse(null);
        return race != null && RaceEvolution.choicesFor(race).length > 0;
    }

    private static void offerAfterRest(ServerPlayer player) {
        if (player.isSleeping() || PENDING.containsKey(player.getUUID())
                || !eligible(player)) return;
        Race race = RaceManager.getBaseRace(player).orElse(null);
        if (RaceEvolution.choicesFor(race).length == 0) return;

        PENDING.put(player.getUUID(), race);
        AWAKENING_STARTED.put(player.getUUID(), player.serverLevel().getGameTime());

        player.connection.send(new ClientboundSetTitlesAnimationPacket(8, 33, 10));
        player.connection.send(new ClientboundSetTitleTextPacket(
                Component.literal("Your bloodline awakens...")
                        .withStyle(ChatFormatting.GOLD)));
        player.connection.send(new ClientboundSetSubtitleTextPacket(
                Component.literal("A new strength rises within you")
                        .withStyle(ChatFormatting.YELLOW)));
        player.serverLevel().playSound(null, player.blockPosition(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.9F, 0.78F);
    }

    private static void animate(ServerPlayer player, int tick) {
        if (tick % 2 != 0) return;
        double cx = player.getX(), cy = player.getY() + 0.85D, cz = player.getZ();
        double angle = tick * 0.22D;
        double height = 0.15D + tick * 0.023D;
        double radius = 0.68D + Math.sin(tick * 0.10D) * 0.08D;

        int rgb = switch (PENDING.get(player.getUUID())) {
            case ELF -> 0xCBB6FF;
            case DWARF -> 0xD1AE77;
            case GOBLIN -> 0xA2CC62;
            case DRAGONBORN -> 0xE28B6B;
            default -> 0xEAD6A1;
        };
        DustParticleOptions colour = new DustParticleOptions(new Vector3f(
                ((rgb >> 16) & 255) / 255.0F,
                ((rgb >> 8) & 255) / 255.0F,
                (rgb & 255) / 255.0F), 1.0F);

        for (int arm = 0; arm < 3; arm++) {
            double a = angle + arm * Math.PI * 2.0D / 3.0D;
            player.serverLevel().sendParticles(colour,
                    cx + Math.cos(a) * radius, cy + height,
                    cz + Math.sin(a) * radius, 1, 0.03D, 0.08D, 0.03D, 0.003D);
        }
        player.serverLevel().sendParticles(ParticleTypes.ENCHANT,
                cx, cy + 0.5D, cz, 5, 0.75D, 0.6D, 0.75D, 0.12D);

        if (tick == 20) {
            player.serverLevel().playSound(null, player.blockPosition(),
                    SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.PLAYERS, 0.75F, 0.9F);
        }
        if (tick == 40) {
            player.serverLevel().sendParticles(ParticleTypes.END_ROD,
                    cx, cy + 0.5D, cz, 22, 0.6D, 0.75D, 0.6D, 0.055D);
        }
    }

    private static void finishAwakening(ServerPlayer player) {
        Race pendingRace = PENDING.get(player.getUUID());
        if (pendingRace == null || !eligible(player)
                || RaceManager.getBaseRace(player).orElse(null) != pendingRace) {
            clearSession(player.getUUID());
            return;
        }
        RaceEvolution[] choices = RaceEvolution.choicesFor(pendingRace);
        if (choices.length == 1) {
            // No selection menu for Dwarf, Goblin or Dragonborn.
            complete(player, choices[0]);
        } else if (choices.length == 2) {
            // Elves choose only after the short in-world animation.
            player.connection.send(new ClientboundSetTitlesAnimationPacket(0, 1, 4));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.empty()));
            CyberRacesNetwork.sendToPlayer(player,
                    new OpenRaceEvolutionPacket(choices[0].id()));
        } else {
            clearSession(player.getUUID());
        }
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
                "Reach Cyber Level 30 and sleep in a bed to awaken your bloodline.")
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    public static boolean complete(ServerPlayer player, RaceEvolution evolution) {
        if (player == null || evolution == null || !eligible(player)
                || AWAKENING_STARTED.containsKey(player.getUUID())) return false;
        Race pendingRace = PENDING.get(player.getUUID());
        if (pendingRace == null || evolution.baseRace() != pendingRace
                || RaceManager.getBaseRace(player).orElse(null) != pendingRace
                || !RaceManager.setEvolution(player, evolution)) return false;

        clearSession(player.getUUID());
        CharacterSyncService.broadcast(player);
        CyberRacesNetwork.sendToPlayer(player, new CloseRaceEvolutionPacket());
        player.serverLevel().sendParticles(ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1.0D, player.getZ(),
                55, 0.55D, 0.85D, 0.55D, 0.045D);
        player.serverLevel().playSound(null, player.blockPosition(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.95F, 1.18F);

        player.connection.send(new ClientboundSetTitlesAnimationPacket(6, 42, 12));
        player.connection.send(new ClientboundSetTitleTextPacket(
                Component.literal(evolution.displayName())
                        .withStyle(ChatFormatting.GOLD)));
        player.connection.send(new ClientboundSetSubtitleTextPacket(
                Component.literal("Your evolution is complete")
                        .withStyle(ChatFormatting.YELLOW)));
        return true;
    }

    public static void decline(ServerPlayer player) {
        if (player == null) return;
        if (AWAKENING_STARTED.containsKey(player.getUUID())) return;
        if (PENDING.remove(player.getUUID()) != null) {
            player.sendSystemMessage(Component.literal(
                    "Evolution postponed. Sleep again when you're ready.")
                    .withStyle(ChatFormatting.AQUA));
        }
        clearSession(player.getUUID());
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
        AWAKENING_STARTED.remove(id);
        PENDING.remove(id);
    }
}
