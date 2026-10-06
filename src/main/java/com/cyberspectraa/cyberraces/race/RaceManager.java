package com.cyberspectraa.cyberraces.race;

import com.cyberspectraa.cyberraces.compat.FairyWingCompat;
import com.cyberspectraa.cyberraces.compat.NaturalWingCompat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class RaceManager {
    private static final String ROOT_KEY = "CyberRaces";
    private static final String RACE_KEY = "Race";
    private static final String EVOLUTION_KEY = "Evolution";
    private static final String EVOLUTION_CHOSEN_KEY = "EvolutionChosen";

    private RaceManager() {
    }

    public static Optional<Race> getRace(ServerPlayer player) {
        CompoundTag root = player.getPersistentData().getCompound(ROOT_KEY);
        if (!root.contains(RACE_KEY)) {
            return Optional.empty();
        }

        return Race.byId(root.getString(RACE_KEY));
    }

    public static boolean hasRace(ServerPlayer player) {
        return getRace(player).isPresent();
    }

    public static Optional<RaceEvolution> getEvolution(
        ServerPlayer player
    ) {
        if (player == null) {
            return Optional.empty();
        }

        CompoundTag root =
            player.getPersistentData().getCompound(ROOT_KEY);

        if (!root.getBoolean(EVOLUTION_CHOSEN_KEY)
                || !root.contains(EVOLUTION_KEY)) {
            return Optional.empty();
        }

        Race race = getRace(player).orElse(null);
        RaceEvolution evolution = RaceEvolution.byId(
            root.getString(EVOLUTION_KEY)
        ).orElse(null);

        if (race == null
                || evolution == null
                || evolution.baseRace() != race) {
            return Optional.empty();
        }

        return Optional.of(evolution);
    }

    public static String getEffectiveDisplayName(
        ServerPlayer player
    ) {
        return getEvolution(player)
            .map(RaceEvolution::displayName)
            .orElseGet(() ->
                getRace(player)
                    .map(Race::displayName)
                    .orElse("No Race")
            );
    }

    public static void setRace(ServerPlayer player, Race race) {
        Race previousRace = getRace(player).orElse(null);

        if (previousRace == Race.FAIRY && race != Race.FAIRY) {
            FairyWingCompat.removeRacialWings(player);
        }

        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.contains(ROOT_KEY)
            ? persistent.getCompound(ROOT_KEY)
            : new CompoundTag();

        root.putString(RACE_KEY, race.id());
        root.remove(EVOLUTION_KEY);
        root.putBoolean(EVOLUTION_CHOSEN_KEY, false);
        persistent.put(ROOT_KEY, root);

        RaceAttributeApplier.apply(player, race, null);

        if (race == Race.FAIRY) {
            FairyWingCompat.ensureEquipped(player);
        } else {
            FairyWingCompat.removeRacialWings(player);
        }

        NaturalWingCompat.ensureCorrectWing(player);
    }

    public static boolean setEvolution(
        ServerPlayer player,
        RaceEvolution evolution
    ) {
        if (player == null || evolution == null) {
            return false;
        }

        Race race = getRace(player).orElse(null);

        if (race == null
                || evolution.baseRace() != race
                || com.cyberspectraa.cyberraces.progression.ProgressionManager
                    .getLevel(player) < RaceEvolution.REQUIRED_LEVEL) {
            return false;
        }

        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.contains(ROOT_KEY)
            ? persistent.getCompound(ROOT_KEY)
            : new CompoundTag();

        root.putString(EVOLUTION_KEY, evolution.id());
        root.putBoolean(EVOLUTION_CHOSEN_KEY, true);
        persistent.put(ROOT_KEY, root);

        reapply(player);
        return true;
    }

    public static void forceSetEvolution(
        ServerPlayer player,
        RaceEvolution evolution
    ) {
        if (player == null || evolution == null) {
            return;
        }

        Race race = getRace(player).orElse(null);

        if (race == null || evolution.baseRace() != race) {
            return;
        }

        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.contains(ROOT_KEY)
            ? persistent.getCompound(ROOT_KEY)
            : new CompoundTag();

        root.putString(EVOLUTION_KEY, evolution.id());
        root.putBoolean(EVOLUTION_CHOSEN_KEY, true);
        persistent.put(ROOT_KEY, root);

        reapply(player);
    }

    public static void clearEvolution(ServerPlayer player) {
        if (player == null) {
            return;
        }

        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.contains(ROOT_KEY)
            ? persistent.getCompound(ROOT_KEY)
            : new CompoundTag();

        root.remove(EVOLUTION_KEY);
        root.putBoolean(EVOLUTION_CHOSEN_KEY, false);
        persistent.put(ROOT_KEY, root);

        reapply(player);
    }

    public static double effectiveFireDamageMultiplier(
        ServerPlayer player
    ) {
        Race race = getRace(player).orElse(null);

        if (race == null) {
            return 1.0D;
        }

        double multiplier = race.fireDamageMultiplier();

        RaceEvolution evolution = getEvolution(player).orElse(null);
        if (evolution != null) {
            multiplier *= evolution.stats().fireDamageMultiplier();
        }

        return multiplier;
    }

    public static void clearRace(ServerPlayer player) {
        FairyWingCompat.removeRacialWings(player);
        NaturalWingCompat.removeNaturalWings(player);

        CompoundTag persistent = player.getPersistentData();
        if (persistent.contains(ROOT_KEY)) {
            CompoundTag root = persistent.getCompound(ROOT_KEY);
            root.remove(RACE_KEY);
            root.remove(EVOLUTION_KEY);
            root.remove(EVOLUTION_CHOSEN_KEY);

            if (root.isEmpty()) {
                persistent.remove(ROOT_KEY);
            } else {
                persistent.put(ROOT_KEY, root);
            }
        }

        RaceAttributeApplier.clear(player);
    }

    public static void reapply(ServerPlayer player) {
        getRace(player).ifPresentOrElse(
            race -> {
                RaceAttributeApplier.apply(
                    player,
                    race,
                    getEvolution(player).orElse(null)
                );
                if (race == Race.FAIRY) {
                    FairyWingCompat.ensureEquipped(player);
                } else {
                    FairyWingCompat.removeRacialWings(player);
                }

                NaturalWingCompat.ensureCorrectWing(player);
            },
            () -> {
                FairyWingCompat.removeRacialWings(player);
                NaturalWingCompat.removeNaturalWings(player);
                RaceAttributeApplier.clear(player);
            }
        );
    }

    public static void copyRaceData(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
        CompoundTag oldPersistent = oldPlayer.getPersistentData();
        if (!oldPersistent.contains(ROOT_KEY)) {
            return;
        }

        newPlayer.getPersistentData().put(ROOT_KEY, oldPersistent.getCompound(ROOT_KEY).copy());
    }
}
