package com.cyberspectraa.cyberraces.ability;

import com.cyberspectraa.cyberraces.character.CharacterManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GoblinScavengerSenseAbility {
    public static final int ACTIVE_TICKS = 8 * 20;
    public static final int COOLDOWN_TICKS = 24 * 20;

    private static final int HORIZONTAL_RADIUS = 16;
    private static final int VERTICAL_RADIUS = 6;

    private static final String READY_TICK_KEY =
        "GoblinScavengerReadyTick";

    private static final Map<UUID, Long> ACTIVE_UNTIL =
        new HashMap<>();

    private GoblinScavengerSenseAbility() {
    }

    public static void tryActivate(ServerPlayer player) {
        long now = player.level().getGameTime();
        long readyAt = getReadyTick(player);

        if (readyAt > now) {
            double seconds = (readyAt - now) / 20.0D;
            player.displayClientMessage(
                Component.literal(
                    String.format(
                        "Scavenger Sense: %.1fs cooldown",
                        seconds
                    )
                ),
                true
            );
            return;
        }

        ACTIVE_UNTIL.put(
            player.getUUID(),
            now + ACTIVE_TICKS
        );

        setReadyTick(
            player,
            now + COOLDOWN_TICKS
        );

        player.displayClientMessage(
            Component.literal(
                "Scavenger Sense active — sniffing out loot"
            ),
            true
        );

        scan(player);
    }

    public static void tick(ServerPlayer player) {
        Long until = ACTIVE_UNTIL.get(player.getUUID());
        if (until == null) {
            return;
        }

        long now = player.level().getGameTime();

        if (now >= until) {
            ACTIVE_UNTIL.remove(player.getUUID());
            player.displayClientMessage(
                Component.literal("Scavenger Sense faded"),
                true
            );
            return;
        }

        // Container scanning is intentionally only once per second.
        if (now % 20L == 0L) {
            scan(player);
        }
    }

    public static void cleanup(ServerPlayer player) {
        ACTIVE_UNTIL.remove(player.getUUID());
    }

    private static void scan(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        AABB itemArea =
            player.getBoundingBox().inflate(HORIZONTAL_RADIUS);

        ItemEntity drop =
            level.getEntitiesOfClass(
                ItemEntity.class,
                itemArea,
                Entity::isAlive
            ).stream()
                .min(
                    Comparator.comparingDouble(
                        player::distanceToSqr
                    )
                )
                .orElse(null);

        BlockPos cache =
            findNearestLootContainer(level, player);

        if (drop != null) {
            level.sendParticles(
                player,
                ParticleTypes.END_ROD,
                true,
                drop.getX(),
                drop.getY() + 0.20D,
                drop.getZ(),
                1,
                0.04D,
                0.03D,
                0.04D,
                0.0D
            );
        }

        if (cache != null) {
            level.sendParticles(
                player,
                ParticleTypes.ENCHANT,
                true,
                cache.getX() + 0.5D,
                cache.getY() + 0.8D,
                cache.getZ() + 0.5D,
                2,
                0.12D,
                0.10D,
                0.12D,
                0.0D
            );
        }

        player.displayClientMessage(
            Component.literal(
                "Scavenge | "
                    + entityLabel("Drop", player, drop)
                    + " | "
                    + blockLabel("Cache", player, cache)
            ),
            true
        );
    }

    private static BlockPos findNearestLootContainer(
        ServerLevel level,
        ServerPlayer player
    ) {
        BlockPos origin = player.blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        BlockPos min = origin.offset(
            -HORIZONTAL_RADIUS,
            -VERTICAL_RADIUS,
            -HORIZONTAL_RADIUS
        );

        BlockPos max = origin.offset(
            HORIZONTAL_RADIUS,
            VERTICAL_RADIUS,
            HORIZONTAL_RADIUS
        );

        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (!level.hasChunkAt(pos)) {
                continue;
            }

            BlockEntity blockEntity =
                level.getBlockEntity(pos);

            if (!(blockEntity instanceof Container container)
                || container.isEmpty()) {
                continue;
            }

            double distance =
                pos.distSqr(origin);

            if (distance < bestDistance) {
                bestDistance = distance;
                best = pos.immutable();
            }
        }

        return best;
    }

    private static String entityLabel(
        String label,
        ServerPlayer player,
        Entity entity
    ) {
        if (entity == null) {
            return label + " --";
        }

        int blocks = (int) Math.round(
            Math.sqrt(player.distanceToSqr(entity))
        );

        return label
            + " "
            + directionArrow(
                player,
                entity.getX(),
                entity.getZ()
            )
            + " "
            + blocks
            + "m";
    }

    private static String blockLabel(
        String label,
        ServerPlayer player,
        BlockPos pos
    ) {
        if (pos == null) {
            return label + " --";
        }

        double x = pos.getX() + 0.5D;
        double z = pos.getZ() + 0.5D;
        double dx = x - player.getX();
        double dy = pos.getY() + 0.5D - player.getY();
        double dz = z - player.getZ();
        int blocks = (int) Math.round(
            Math.sqrt(dx * dx + dy * dy + dz * dz)
        );

        return label
            + " "
            + directionArrow(player, x, z)
            + " "
            + blocks
            + "m";
    }

    private static String directionArrow(
        ServerPlayer player,
        double x,
        double z
    ) {
        double dx = x - player.getX();
        double dz = z - player.getZ();

        float targetYaw =
            (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;

        float relative =
            Mth.wrapDegrees(targetYaw - player.getYRot());

        if (relative >= -22.5F && relative < 22.5F) {
            return "↑";
        }
        if (relative >= 22.5F && relative < 67.5F) {
            return "↗";
        }
        if (relative >= 67.5F && relative < 112.5F) {
            return "→";
        }
        if (relative >= 112.5F && relative < 157.5F) {
            return "↘";
        }
        if (relative >= 157.5F || relative < -157.5F) {
            return "↓";
        }
        if (relative >= -157.5F && relative < -112.5F) {
            return "↙";
        }
        if (relative >= -112.5F && relative < -67.5F) {
            return "←";
        }

        return "↖";
    }

    private static long getReadyTick(ServerPlayer player) {
        CompoundTag root =
            player.getPersistentData().getCompound(
                CharacterManager.ROOT_KEY
            );

        return root.getLong(READY_TICK_KEY);
    }

    private static void setReadyTick(
        ServerPlayer player,
        long readyTick
    ) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag root =
            persistent.contains(CharacterManager.ROOT_KEY)
                ? persistent.getCompound(CharacterManager.ROOT_KEY)
                : new CompoundTag();

        root.putLong(READY_TICK_KEY, readyTick);
        persistent.put(CharacterManager.ROOT_KEY, root);
    }
}
