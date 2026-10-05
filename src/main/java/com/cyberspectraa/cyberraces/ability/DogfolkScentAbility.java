package com.cyberspectraa.cyberraces.ability;

import com.cyberspectraa.cyberraces.character.CharacterManager;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public final class DogfolkScentAbility {
    public static final int ACTIVE_TICKS = 8 * 20;
    public static final int COOLDOWN_TICKS = 24 * 20;
    public static final double RADIUS = 24.0D;

    private static final String READY_TICK_KEY =
        "DogfolkScentReadyTick";

    private static final Map<UUID, Long> ACTIVE_UNTIL =
        new HashMap<>();

    private DogfolkScentAbility() {
    }

    public static void tryActivate(ServerPlayer player) {
        long now = player.level().getGameTime();
        long readyAt = getReadyTick(player);

        if (readyAt > now) {
            double seconds = (readyAt - now) / 20.0D;
            player.displayClientMessage(
                Component.literal(
                    String.format(
                        "Scent: %.1fs cooldown",
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
                "Scent active: items • animals • hostiles"
            ),
            true
        );

        scan(player, true);
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
                Component.literal("Scent faded"),
                true
            );
            return;
        }

        if (now % 10L == 0L) {
            scan(player, now % 20L == 0L);
        }
    }

    public static void cleanup(ServerPlayer player) {
        ACTIVE_UNTIL.remove(player.getUUID());
    }

    private static void scan(
        ServerPlayer player,
        boolean showDistances
    ) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        AABB area =
            player.getBoundingBox().inflate(RADIUS);

        ItemEntity item = nearest(
            level.getEntitiesOfClass(
                ItemEntity.class,
                area,
                Entity::isAlive
            ),
            player
        );

        Animal animal = nearest(
            level.getEntitiesOfClass(
                Animal.class,
                area,
                Entity::isAlive
            ),
            player
        );

        Monster hostile = nearest(
            level.getEntitiesOfClass(
                Monster.class,
                area,
                Entity::isAlive
            ),
            player
        );

        if (item != null) {
            mark(
                level,
                player,
                item,
                ParticleTypes.END_ROD
            );
        }

        if (animal != null) {
            mark(
                level,
                player,
                animal,
                ParticleTypes.HAPPY_VILLAGER
            );
        }

        if (hostile != null) {
            mark(
                level,
                player,
                hostile,
                ParticleTypes.SOUL_FIRE_FLAME
            );
        }

        if (showDistances) {
            player.displayClientMessage(
                Component.literal(
                    "Scent | "
                        + distanceLabel("Item", player, item)
                        + " | "
                        + distanceLabel("Animal", player, animal)
                        + " | "
                        + distanceLabel("Danger", player, hostile)
                ),
                true
            );
        }
    }

    private static <T extends Entity> T nearest(
        List<T> entities,
        ServerPlayer player
    ) {
        return entities.stream()
            .min(
                Comparator.comparingDouble(
                    player::distanceToSqr
                )
            )
            .orElse(null);
    }

    private static void mark(
        ServerLevel level,
        ServerPlayer player,
        Entity target,
        ParticleOptions particle
    ) {
        Vec3 from = player.getEyePosition();
        Vec3 to = target.position().add(
            0.0D,
            target.getBbHeight() * 0.55D,
            0.0D
        );

        Vec3 direction = to.subtract(from);
        double distance = direction.length();

        if (distance > 0.01D) {
            Vec3 normal = direction.scale(1.0D / distance);
            double trailLength = Math.min(4.5D, distance);

            for (int i = 1; i <= 6; i++) {
                Vec3 point = from.add(
                    normal.scale(
                        trailLength * i / 6.0D
                    )
                );

                level.sendParticles(
                    player,
                    particle,
                    true,
                    point.x,
                    point.y,
                    point.z,
                    1,
                    0.03D,
                    0.03D,
                    0.03D,
                    0.0D
                );
            }
        }

        level.sendParticles(
            player,
            particle,
            true,
            to.x,
            to.y,
            to.z,
            3,
            target.getBbWidth() * 0.20D,
            target.getBbHeight() * 0.18D,
            target.getBbWidth() * 0.20D,
            0.0D
        );
    }

    private static String distanceLabel(
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

        return label + " " + blocks + "m";
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
