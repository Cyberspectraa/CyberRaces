package com.cyberspectraa.cyberraces.event;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Impact for a visible cobweb projectile fired with Arachnid's V ability. */
public final class ArachnidWebEvents {
    private ArachnidWebEvents() {}

    @SubscribeEvent
    public static void onProjectileHit(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof Snowball web)
                || web.level().isClientSide
                || !web.getPersistentData().getBoolean("CyberRacesWebShot")) return;

        HitResult hit = event.getRayTraceResult();
        if (hit instanceof EntityHitResult entityHit
                && entityHit.getEntity() instanceof LivingEntity victim
                && victim != web.getOwner()) {
            // Ensnare without leaving permanent cobweb blocks in players' builds.
            victim.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
        }

        if (web.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.CLOUD,
                    hit.getLocation().x, hit.getLocation().y, hit.getLocation().z,
                    15, 0.18D, 0.16D, 0.18D, 0.025D);
        }
    }
}
