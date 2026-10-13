package com.cyberspectraa.cyberraces.ability;

import com.cyberspectraa.cyberraces.character.CharacterManager;
import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.RaceSensePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;

import java.util.Comparator;
import java.util.List;

/**
 * V: Hunter's Nose. Reveals nearby living creatures through walls for
 * this Dogfolk client only; no potions, tracking trails or global glow.
 */
public final class DogfolkScentAbility {
    public static final int ACTIVE_TICKS = 7 * 20;
    public static final int COOLDOWN_TICKS = 28 * 20;
    public static final double RADIUS = 22.0D;
    private static final String READY_TICK_KEY = "DogfolkScentReadyTick";

    private DogfolkScentAbility() {}

    public static void tryActivate(ServerPlayer player) {
        long now = player.level().getGameTime();
        CompoundTag root = player.getPersistentData()
                .getCompound(CharacterManager.ROOT_KEY);
        long ready = root.getLong(READY_TICK_KEY);
        if (ready > now) {
            player.displayClientMessage(Component.literal(
                    "Hunter's Nose: " + ((ready - now + 19L) / 20L)
                            + "s cooldown"), true);
            return;
        }
        if (!(player.level() instanceof ServerLevel level)) return;

        List<Integer> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(RADIUS),
                entity -> entity != player && !(entity instanceof ArmorStand)
                        && entity.isAlive() && entity.distanceToSqr(player)
                        <= RADIUS * RADIUS
        ).stream()
                .sorted(Comparator.comparingDouble(player::distanceToSqr))
                .limit(16)
                .map(LivingEntity::getId)
                .toList();

        CyberRacesNetwork.sendToPlayer(player, RaceSensePacket.creatures(targets));
        root.putLong(READY_TICK_KEY, now + COOLDOWN_TICKS);
        player.getPersistentData().put(CharacterManager.ROOT_KEY, root);

        player.displayClientMessage(Component.literal(
                "Hunter's Nose: " + targets.size()
                        + " nearby creatures revealed (7s)"), true);
    }

    // Legacy lifecycle hooks retained for RacialPassiveEvents compatibility.
    public static void tick(ServerPlayer player) {}
    public static void cleanup(ServerPlayer player) {}
}
