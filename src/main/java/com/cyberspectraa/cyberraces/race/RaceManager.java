package com.cyberspectraa.cyberraces.race;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class RaceManager {
    private static final String ROOT_KEY = "CyberRaces";
    private static final String RACE_KEY = "Race";

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

    public static void setRace(ServerPlayer player, Race race) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.contains(ROOT_KEY)
            ? persistent.getCompound(ROOT_KEY)
            : new CompoundTag();

        root.putString(RACE_KEY, race.id());
        persistent.put(ROOT_KEY, root);

        RaceAttributeApplier.apply(player, race);
    }

    public static void clearRace(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();
        if (persistent.contains(ROOT_KEY)) {
            CompoundTag root = persistent.getCompound(ROOT_KEY);
            root.remove(RACE_KEY);

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
            race -> RaceAttributeApplier.apply(player, race),
            () -> RaceAttributeApplier.clear(player)
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
