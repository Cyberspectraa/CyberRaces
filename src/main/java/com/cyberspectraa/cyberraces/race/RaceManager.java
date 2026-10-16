package com.cyberspectraa.cyberraces.race;

import com.cyberspectraa.cyberraces.compat.FairyWingCompat;
import com.cyberspectraa.cyberraces.compat.NaturalWingCompat;
import com.cyberspectraa.cyberraces.character.CharacterSyncService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class RaceManager {
    private static final String ROOT_KEY = "CyberRaces";
    private static final String RACE_KEY = "Race";
    private static final String EVOLUTION_KEY = "Evolution";
    private static final String EVOLUTION_CHOSEN_KEY = "EvolutionChosen";
    public static final String UNDEAD_AFFLICTION_KEY = "AcquiredUndead";
    public static final String UNDEAD_CURE_REMAINING_KEY = "UndeadCureRemaining";

    public static final boolean EVOLUTIONS_ENABLED = true;

    private RaceManager() {
    }

    /** The character's permanent ancestry, unaffected by infections. */
    public static Optional<Race> getBaseRace(ServerPlayer player) {
        if (player == null) return Optional.empty();
        CompoundTag root = player.getPersistentData().getCompound(ROOT_KEY);
        return Race.byId(root.getString(RACE_KEY));
    }

    /** The acquired Undead form overlays, rather than replaces, saved ancestry. */
    public static boolean isAcquiredUndead(ServerPlayer player) {
        return player != null
            && getBaseRace(player).filter(race -> race != Race.UNDEAD).isPresent()
            && player.getPersistentData().getCompound(ROOT_KEY)
                .getBoolean(UNDEAD_AFFLICTION_KEY);
    }

    /** Effective race for attributes, V-key abilities, HUD and sync. */
    public static Optional<Race> getRace(ServerPlayer player) {
        Optional<Race> base = getBaseRace(player);
        return isAcquiredUndead(player) ? Optional.of(Race.UNDEAD) : base;
    }

    public static int undeadCureRemaining(ServerPlayer player) {
        return player == null ? 0 : Math.max(0, player.getPersistentData()
            .getCompound(ROOT_KEY).getInt(UNDEAD_CURE_REMAINING_KEY));
    }

    public static void setUndeadCureRemaining(ServerPlayer player, int ticks) {
        if (player == null) return;
        CompoundTag root = player.getPersistentData().getCompound(ROOT_KEY);
        if (ticks > 0) root.putInt(UNDEAD_CURE_REMAINING_KEY, ticks);
        else root.remove(UNDEAD_CURE_REMAINING_KEY);
        player.getPersistentData().put(ROOT_KEY, root);
    }

    public static void setAcquiredUndead(ServerPlayer player, boolean active) {
        if (player == null || getBaseRace(player).filter(r -> r != Race.UNDEAD).isEmpty()) {
            return;
        }
        CompoundTag root = player.getPersistentData().getCompound(ROOT_KEY);
        if (active) root.putBoolean(UNDEAD_AFFLICTION_KEY, true);
        else {
            root.remove(UNDEAD_AFFLICTION_KEY);
            root.remove(UNDEAD_CURE_REMAINING_KEY);
        }
        player.getPersistentData().put(ROOT_KEY, root);
        reapply(player);
        CharacterSyncService.broadcast(player);
    }

    public static boolean hasRace(ServerPlayer player) {
        return getRace(player).isPresent();
    }

    public static Optional<RaceEvolution> getEvolution(
        ServerPlayer player
    ) {
        if (player == null) return Optional.empty();
        CompoundTag root = player.getPersistentData().getCompound(ROOT_KEY);

        // Drop the old activity tracker and any discontinued evolution ID
        // when loading a pre-cleanup world. Keep the base race and other
        // character appearance data untouched.
        boolean changed = root.contains("EvolutionJourney");
        root.remove("EvolutionJourney");

        RaceEvolution evolution = RaceEvolution.byId(
                root.getString(EVOLUTION_KEY)).orElse(null);
        Race base = getBaseRace(player).orElse(null);
        if (evolution == null || evolution.baseRace() != base
                || !root.getBoolean(EVOLUTION_CHOSEN_KEY)) {
            changed |= root.contains(EVOLUTION_KEY)
                    || root.contains(EVOLUTION_CHOSEN_KEY);
            root.remove(EVOLUTION_KEY);
            root.remove(EVOLUTION_CHOSEN_KEY);
            if (changed) player.getPersistentData().put(ROOT_KEY, root);
            return Optional.empty();
        }

        if (changed) player.getPersistentData().put(ROOT_KEY, root);
        // Acquired Undead temporarily suppresses base-race evolution perks.
        return isAcquiredUndead(player) ? Optional.empty() : Optional.of(evolution);
    }

    public static String getEffectiveDisplayName(
        ServerPlayer player
    ) {
        if (isAcquiredUndead(player)) {
            return "Undead " + getBaseRace(player)
                .map(Race::displayName).orElse("Unknown");
        }
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
        root.remove(UNDEAD_AFFLICTION_KEY);
        root.remove(UNDEAD_CURE_REMAINING_KEY);
        // Remove any saved progress from the retired activity tracker.
        root.remove("EvolutionJourney");
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
        if (!EVOLUTIONS_ENABLED || player == null || evolution == null) {
            return false;
        }

        Race race = getRace(player).orElse(null);

        if (race == null
                || evolution.baseRace() != race
                || !evolution.availableToEvolve()
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
        if (!EVOLUTIONS_ENABLED || player == null || evolution == null) {
            return;
        }

        Race race = getRace(player).orElse(null);

        if (race == null || evolution.baseRace() != race
                || !evolution.availableToEvolve()) {
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
            root.remove(UNDEAD_AFFLICTION_KEY);
            root.remove(UNDEAD_CURE_REMAINING_KEY);
            root.remove("EvolutionJourney");
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
