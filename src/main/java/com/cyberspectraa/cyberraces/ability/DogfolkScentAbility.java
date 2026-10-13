package com.cyberspectraa.cyberraces.ability;

import com.cyberspectraa.cyberraces.character.CharacterManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;

import java.util.Comparator;
import java.util.List;

/**
 * V: Hunter's Nose. Applies the actual vanilla Glowing status effect,
 * rather than modifying client entity flags or spawning particle trails.
 *
 * The effect is synced by Minecraft, rendering the vanilla outlined
 * silhouette even through blocks. Like any Glowing potion effect it is
 * visible to nearby players, not exclusively to the Dogfolk who cast it.
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

        List<LivingEntity> targets = level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(RADIUS),
                entity -> entity != player && !(entity instanceof ArmorStand)
                        && entity.isAlive()
                        && entity.distanceToSqr(player) <= RADIUS * RADIUS
        ).stream()
                .sorted(Comparator.comparingDouble(player::distanceToSqr))
                .limit(16)
                .toList();

        for (LivingEntity target : targets) {
            // Preserve a pre-existing Glowing effect if it lasts longer.
            MobEffectInstance existing = target.getEffect(MobEffects.GLOWING);
            if (existing != null && existing.getDuration() >= ACTIVE_TICKS)
                continue;
            target.addEffect(new MobEffectInstance(
                    MobEffects.GLOWING,
                    ACTIVE_TICKS,
                    0,
                    false, // ambient
                    false, // no distracting potion particles
                    false  // no potion icon
            ));
        }

        root.putLong(READY_TICK_KEY, now + COOLDOWN_TICKS);
        player.getPersistentData().put(CharacterManager.ROOT_KEY, root);
        player.displayClientMessage(Component.literal(
                "Hunter's Nose: " + targets.size()
                        + " creatures glowing for 7s"), true);
    }

    // Lifecycle hooks retained for RacialPassiveEvents compatibility.
    public static void tick(ServerPlayer player) {}
    public static void cleanup(ServerPlayer player) {}
}
