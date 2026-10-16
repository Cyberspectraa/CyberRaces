package com.cyberspectraa.cyberraces.compat;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

/**
 * Dragon-style short continuous lightning breath, not Lightning Lance.
 * Uses Ice and Fire CE's lightning dragon sound and damage type when loaded.
 * Uses only vanilla particles so there is no dependency on its custom
 * entity-bound particle serializers and NEVER modifies terrain.
 */
public final class LightningBreathCompat {
    private static final int DURATION_TICKS = 16;
    private static final double MAX_RANGE = 10.0D;
    private static final Map<UUID, Long> END_AT = new HashMap<>();

    private LightningBreathCompat() {}

    public static boolean start(ServerPlayer player) {
        if (END_AT.containsKey(player.getUUID()) || player.isSpectator()) return false;
        END_AT.put(player.getUUID(), player.serverLevel().getGameTime() + DURATION_TICKS);
        ServerLevel level = player.serverLevel();
        SoundEvent sound = SoundEvents.LIGHTNING_BOLT_THUNDER;
        if (ModList.get().isLoaded("iceandfire")) {
            SoundEvent iceAndFire = ForgeRegistries.SOUND_EVENTS.getValue(
                    new ResourceLocation("iceandfire", "lightningdragon_breath"));
            if (iceAndFire != null) sound = iceAndFire;
        }
        level.playSound(null, player.blockPosition(), sound,
                SoundSource.PLAYERS, 0.8F, 1.2F);
        return true;
    }

    public static void tick(ServerPlayer player) {
        Long end = END_AT.get(player.getUUID());
        if (end == null) return;
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        if (now >= end || !player.isAlive()) {
            END_AT.remove(player.getUUID());
            return;
        }

        Vec3 from = player.getEyePosition().add(player.getLookAngle().scale(0.45));
        Vec3 far = from.add(player.getLookAngle().scale(MAX_RANGE));
        Vec3 to = level.clip(new ClipContext(from, far,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player))
                .getLocation();
        Vec3 delta = to.subtract(from);
        double length = delta.length();
        if (length < 0.01D) return;

        // Winding blue-white electrical sparks form a sustained breath stream
        // with a clear endpoint and natural wobble, not a single projectile.
        for (int step = 0; step <= 19; step++) {
            double t = (step + level.random.nextDouble() * 0.4D) / 20.0D;
            Vec3 p = from.add(delta.scale(t));
            double wobble = 0.13D + t * 0.28D;
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    p.x, p.y, p.z, 2,
                    wobble, wobble, wobble, 0.025D);
            if (step % 4 == 0) level.sendParticles(ParticleTypes.END_ROD,
                    p.x, p.y, p.z, 1,
                    wobble * 0.3D, wobble * 0.3D, wobble * 0.3D, 0.012D);
        }

        if (now % 4L != 0L) return;
        AABB hitBox = new AABB(from, to).inflate(1.2D);
        HashSet<UUID> hit = new HashSet<>();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, hitBox,
                entity -> entity.isAlive() && entity != player)) {
            if (!hit.add(target.getUUID()) || !player.hasLineOfSight(target)) continue;
            Vec3 contact = target.getBoundingBox().getCenter();
            double along = contact.subtract(from).dot(delta) / delta.lengthSqr();
            if (along < 0.0D || along > 1.0D) continue;
            if (contact.distanceToSqr(from.add(delta.scale(along))) > 1.5D * 1.5D) continue;
            target.hurt(damageSource(level, player), 2.5F);
        }
    }

    private static DamageSource damageSource(ServerLevel level, ServerPlayer player) {
        if (ModList.get().isLoaded("iceandfire")) {
            ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE,
                    new ResourceLocation("iceandfire", "dragon_lightning"));
            var registry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
            var holder = registry.getHolder(key);
            if (holder.isPresent()) {
                return new DamageSource(holder.get(), player);
            }
        }
        return level.damageSources().playerAttack(player);
    }

    public static void cleanup(ServerPlayer player) {
        if (player != null) END_AT.remove(player.getUUID());
    }
}
