package com.cyberspectraa.cyberraces.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/** Places real, temporary cobweb blocks where the Arachnid web shot lands. */
public final class ArachnidWebEvents {
    private static final long WEB_LIFETIME_TICKS = 20L * 8L;
    private static final Map<ServerLevel, Map<BlockPos, Long>> TEMPORARY_WEBS =
            new WeakHashMap<>();

    private ArachnidWebEvents() {}

    @SubscribeEvent
    public static void onProjectileHit(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof Snowball web)
                || !(web.level() instanceof ServerLevel level)
                || !web.getPersistentData().getBoolean("CyberRacesWebShot"))
            return;

        HitResult hit = event.getRayTraceResult();
        if (hit == null || hit.getType() == HitResult.Type.MISS) return;

        BlockPos landing = null;
        if (hit instanceof EntityHitResult entityHit) {
            if (entityHit.getEntity() instanceof LivingEntity victim
                    && victim != web.getOwner()) {
                victim.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
                landing = victim.blockPosition();
            }
        } else if (hit instanceof BlockHitResult blockHit) {
            // Put the web in the empty space adjacent to the impacted face,
            // never overwrite the block the player was aiming at.
            landing = blockHit.getBlockPos().relative(blockHit.getDirection());
        }

        if (landing != null && level.hasChunkAt(landing)
                && level.isEmptyBlock(landing)
                && (web.getOwner() instanceof ServerPlayer shooter
                    ? level.mayInteract(shooter, landing) : true)) {
            if (level.setBlockAndUpdate(landing, Blocks.COBWEB.defaultBlockState())) {
                TEMPORARY_WEBS.computeIfAbsent(level, unused -> new HashMap<>())
                        .put(landing.immutable(), level.getGameTime() + WEB_LIFETIME_TICKS);
            }
        }

        level.sendParticles(ParticleTypes.CLOUD,
                hit.getLocation().x, hit.getLocation().y, hit.getLocation().z,
                12, 0.18D, 0.16D, 0.18D, 0.02D);
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.level instanceof ServerLevel level)) return;
        Map<BlockPos, Long> tracked = TEMPORARY_WEBS.get(level);
        if (tracked == null || tracked.isEmpty()) return;
        long tick = level.getGameTime();

        Iterator<Map.Entry<BlockPos, Long>> iterator = tracked.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Long> entry = iterator.next();
            if (tick < entry.getValue() || !level.hasChunkAt(entry.getKey()))
                continue;
            // Only remove our web when the placed block still is a cobweb.
            if (level.getBlockState(entry.getKey()).is(Blocks.COBWEB))
                level.setBlockAndUpdate(entry.getKey(), Blocks.AIR.defaultBlockState());
            iterator.remove();
        }
        if (tracked.isEmpty()) TEMPORARY_WEBS.remove(level);
    }
}
