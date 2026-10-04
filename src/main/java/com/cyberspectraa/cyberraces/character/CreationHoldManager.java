package com.cyberspectraa.cyberraces.character;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class CreationHoldManager {
    private static final Map<UUID, HoldPoint> HOLDS = new HashMap<>();

    private CreationHoldManager() {
    }

    public static void enter(ServerPlayer player) {
        HOLDS.computeIfAbsent(
            player.getUUID(),
            ignored -> new HoldPoint(player.serverLevel().dimension(), player.position(), player.getYRot(), player.getXRot())
        );

        player.setInvisible(true);
        player.setInvulnerable(true);
        player.setNoGravity(true);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
    }

    public static void tick(ServerPlayer player) {
        HoldPoint hold = HOLDS.get(player.getUUID());
        if (hold == null) {
            enter(player);
            hold = HOLDS.get(player.getUUID());
        }

        player.setInvisible(true);
        player.setInvulnerable(true);
        player.setNoGravity(true);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;

        if (hold != null
            && player.serverLevel().dimension() == hold.dimension
            && player.position().distanceToSqr(hold.position) > 0.0025D) {
            player.teleportTo(
                player.serverLevel(),
                hold.position.x,
                hold.position.y,
                hold.position.z,
                hold.yaw,
                hold.pitch
            );
        }
    }

    public static void release(ServerPlayer player) {
        HOLDS.remove(player.getUUID());
        player.setInvisible(false);
        player.setInvulnerable(false);
        player.setNoGravity(false);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
    }

    public static void forget(ServerPlayer player) {
        HOLDS.remove(player.getUUID());
    }

    private record HoldPoint(
        net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,
        Vec3 position,
        float yaw,
        float pitch
    ) {
    }
}
