package com.cyberspectraa.cyberraces.compat;

import com.cyberspectraa.cyberraces.race.Race;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Comparator;

/**
 * Race-owned AI behaviour for Wild CyberNpc entities. CyberNpc calls this
 * through an optional reflection bridge so the two mods remain independently
 * buildable. This class owns the racial effects; CyberNpc only decides when a
 * racial behaviour is allowed to take control of movement.
 */
public final class CyberNpcRacialAi {
    private static final String AI_ROOT = "CyberRacesWildNpcAi";

    private static final String DRAGON_READY = "DragonBreathReady";
    private static final String FAIRY_READY = "FairyEvasionReady";
    private static final String FOX_READY = "FoxQuickstepReady";
    private static final String ORC_READY = "OrcWarCryReady";
    private static final String NYMPH_READY = "NymphGraceReady";
    private static final String CAT_READY = "CatPounceReady";

    private static Class<?> cachedCollectClass;
    private static Method cachedCollectMethod;

    private CyberNpcRacialAi() {
    }

    /**
     * @return true when the race wants to own navigation for a short moment.
     */
    public static boolean tickCombat(LivingEntity entity) {
        if (!(entity instanceof PathfinderMob mob)
                || !(entity.level() instanceof ServerLevel level)
                || !entity.isAlive()
                || !CyberNpcRaceManager.isWildCyberNpc(entity)) {
            return false;
        }

        Race race = CyberNpcRaceManager.getRace(entity).orElse(null);
        LivingEntity target = mob.getTarget();

        if (race == null || target == null || !target.isAlive()) {
            return false;
        }

        double distanceSqr = entity.distanceToSqr(target);
        boolean lineOfSight = mob.getSensing().hasLineOfSight(target);

        // Dogfolk scent is deliberately particle-free for NPCs. They can keep
        // following a known target through brief line-of-sight breaks.
        if (race == Race.DOGFOLK
                && !lineOfSight
                && distanceSqr <= 24.0D * 24.0D) {
            mob.getNavigation().moveTo(target, 1.12D);
            return true;
        }

        switch (race) {
            case DRAGONBORN -> tryDragonBreath(level, mob, target, distanceSqr, lineOfSight);
            case FAIRY -> tryFairyEvasion(level, mob, target, distanceSqr);
            case FOXFOLK -> tryFoxQuickstep(level, mob, target, distanceSqr);
            case ORC -> tryOrcWarCry(level, mob, target, distanceSqr);
            case NYMPH -> tryNymphGrace(level, mob);
            case CATFOLK -> tryCatPounce(level, mob, target, distanceSqr, lineOfSight);
            default -> {
                // These races already receive their passive CyberRaces
                // attributes/effects. They do not need forced active AI here.
            }
        }

        return false;
    }

    /**
     * Low-priority non-combat racial behaviour.
     *
     * @return true while the race is deliberately moving toward a target.
     */
    public static boolean tickIdle(LivingEntity entity) {
        if (!(entity instanceof PathfinderMob mob)
                || !(entity.level() instanceof ServerLevel level)
                || !entity.isAlive()
                || !CyberNpcRaceManager.isWildCyberNpc(entity)
                || mob.getTarget() != null
                || CyberNpcRaceManager.getRace(entity).orElse(null)
                != Race.GOBLIN) {
            return false;
        }

        // Scavenger Sense is intentionally a low-frequency scan.
        if (entity.tickCount % 20 != Math.floorMod(entity.getId(), 20)) {
            return false;
        }

        ItemEntity loot = level.getEntitiesOfClass(
                        ItemEntity.class,
                        entity.getBoundingBox().inflate(12.0D, 5.0D, 12.0D),
                        item -> item.isAlive()
                                && !item.hasPickUpDelay()
                                && isUsefulScavengeLoot(item.getItem())
                ).stream()
                .min(Comparator.comparingDouble(entity::distanceToSqr))
                .orElse(null);

        if (loot == null) {
            return false;
        }

        if (entity.distanceToSqr(loot) <= 2.25D) {
            boolean collected = tryCollectScavengedItem(entity, loot);

            if (collected) {
                level.sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        loot.getX(),
                        loot.getY() + 0.20D,
                        loot.getZ(),
                        3,
                        0.08D,
                        0.05D,
                        0.08D,
                        0.0D
                );
            }

            return collected;
        }

        mob.getNavigation().moveTo(loot, 1.02D);
        mob.getLookControl().setLookAt(loot, 25.0F, 20.0F);
        return true;
    }

    private static void tryDragonBreath(
            ServerLevel level,
            PathfinderMob mob,
            LivingEntity target,
            double distanceSqr,
            boolean lineOfSight
    ) {
        if (!lineOfSight
                || distanceSqr > 8.0D * 8.0D
                || !isReady(mob, DRAGON_READY)) {
            return;
        }

        Vec3 from = mob.getEyePosition();
        Vec3 to = target.getEyePosition();
        Vec3 ray = to.subtract(from);

        for (int i = 1; i <= 8; i++) {
            Vec3 point = from.add(ray.scale(i / 8.0D));
            level.sendParticles(
                    ParticleTypes.FLAME,
                    point.x,
                    point.y,
                    point.z,
                    2,
                    0.08D,
                    0.08D,
                    0.08D,
                    0.01D
            );
        }

        level.playSound(
                null,
                mob.blockPosition(),
                SoundEvents.BLAZE_SHOOT,
                SoundSource.NEUTRAL,
                0.85F,
                0.78F
        );

        target.hurt(
                level.damageSources().mobAttack(mob),
                5.0F
        );
        target.setSecondsOnFire(4);

        setCooldown(mob, DRAGON_READY, 18 * 20);
    }

    private static void tryFairyEvasion(
            ServerLevel level,
            PathfinderMob mob,
            LivingEntity target,
            double distanceSqr
    ) {
        if (distanceSqr > 5.5D * 5.5D
                || !mob.onGround()
                || mob.isPassenger()
                || mob.isInWaterOrBubble()
                || !isReady(mob, FAIRY_READY)) {
            return;
        }

        Vec3 away = mob.position().subtract(target.position());
        away = horizontalNormal(away);

        Vec3 current = mob.getDeltaMovement();
        mob.setDeltaMovement(
                current.x + away.x * 0.32D,
                Math.max(current.y, 0.42D),
                current.z + away.z * 0.32D
        );
        mob.hurtMarked = true;

        mob.addEffect(
                new MobEffectInstance(
                        MobEffects.SLOW_FALLING,
                        5 * 20,
                        0,
                        false,
                        false,
                        true
                )
        );

        level.sendParticles(
                ParticleTypes.END_ROD,
                mob.getX(),
                mob.getY() + mob.getBbHeight() * 0.55D,
                mob.getZ(),
                5,
                0.18D,
                0.18D,
                0.18D,
                0.01D
        );

        setCooldown(mob, FAIRY_READY, 12 * 20);
    }

    private static void tryFoxQuickstep(
            ServerLevel level,
            PathfinderMob mob,
            LivingEntity target,
            double distanceSqr
    ) {
        if (distanceSqr > 5.0D * 5.0D
                || !mob.onGround()
                || mob.isPassenger()
                || mob.isInWaterOrBubble()
                || !isReady(mob, FOX_READY)) {
            return;
        }

        Vec3 away = horizontalNormal(
                mob.position().subtract(target.position())
        );

        double sideSign = mob.getRandom().nextBoolean()
                ? 1.0D
                : -1.0D;

        Vec3 side = new Vec3(
                -away.z * sideSign,
                0.0D,
                away.x * sideSign
        );

        Vec3 dash = away.scale(0.78D)
                .add(side.scale(0.64D))
                .normalize()
                .scale(1.25D);

        mob.setDeltaMovement(
                dash.x,
                Math.max(mob.getDeltaMovement().y, 0.12D),
                dash.z
        );
        mob.hurtMarked = true;

        level.sendParticles(
                ParticleTypes.POOF,
                mob.getX(),
                mob.getY() + 0.25D,
                mob.getZ(),
                7,
                0.18D,
                0.08D,
                0.18D,
                0.01D
        );

        setCooldown(mob, FOX_READY, 6 * 20);
    }

    private static void tryOrcWarCry(
            ServerLevel level,
            PathfinderMob mob,
            LivingEntity target,
            double distanceSqr
    ) {
        double healthFraction = mob.getMaxHealth() <= 0.0F
                ? 1.0D
                : mob.getHealth() / mob.getMaxHealth();

        if ((healthFraction > 0.68D && distanceSqr > 4.5D * 4.5D)
                || !isReady(mob, ORC_READY)) {
            return;
        }

        mob.addEffect(
                new MobEffectInstance(
                        MobEffects.DAMAGE_BOOST,
                        8 * 20,
                        0,
                        false,
                        true,
                        true
                )
        );

        mob.addEffect(
                new MobEffectInstance(
                        MobEffects.DAMAGE_RESISTANCE,
                        8 * 20,
                        0,
                        false,
                        true,
                        true
                )
        );

        level.playSound(
                null,
                mob.blockPosition(),
                SoundEvents.RAVAGER_ROAR,
                SoundSource.NEUTRAL,
                0.58F,
                0.92F
        );

        level.sendParticles(
                ParticleTypes.CRIT,
                mob.getX(),
                mob.getY() + mob.getBbHeight() * 0.55D,
                mob.getZ(),
                10,
                mob.getBbWidth() * 0.35D,
                mob.getBbHeight() * 0.22D,
                mob.getBbWidth() * 0.35D,
                0.03D
        );

        setCooldown(mob, ORC_READY, 30 * 20);
    }

    private static void tryNymphGrace(
            ServerLevel level,
            PathfinderMob mob
    ) {
        double healthFraction = mob.getMaxHealth() <= 0.0F
                ? 1.0D
                : mob.getHealth() / mob.getMaxHealth();

        if (healthFraction > 0.62D
                || !isReady(mob, NYMPH_READY)) {
            return;
        }

        mob.addEffect(
                new MobEffectInstance(
                        MobEffects.REGENERATION,
                        8 * 20,
                        0,
                        false,
                        true,
                        true
                )
        );

        mob.addEffect(
                new MobEffectInstance(
                        MobEffects.MOVEMENT_SPEED,
                        8 * 20,
                        0,
                        false,
                        true,
                        true
                )
        );

        level.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                mob.getX(),
                mob.getY() + mob.getBbHeight() * 0.55D,
                mob.getZ(),
                7,
                mob.getBbWidth() * 0.30D,
                mob.getBbHeight() * 0.20D,
                mob.getBbWidth() * 0.30D,
                0.01D
        );

        level.playSound(
                null,
                mob.blockPosition(),
                SoundEvents.ALLAY_AMBIENT_WITH_ITEM,
                SoundSource.NEUTRAL,
                0.28F,
                1.20F
        );

        setCooldown(mob, NYMPH_READY, 30 * 20);
    }

    private static void tryCatPounce(
            ServerLevel level,
            PathfinderMob mob,
            LivingEntity target,
            double distanceSqr,
            boolean lineOfSight
    ) {
        if (!lineOfSight
                || distanceSqr < 3.0D * 3.0D
                || distanceSqr > 7.0D * 7.0D
                || !mob.onGround()
                || mob.isPassenger()
                || !isReady(mob, CAT_READY)) {
            return;
        }

        Vec3 toward = horizontalNormal(
                target.position().subtract(mob.position())
        );

        mob.setDeltaMovement(
                toward.x * 0.88D,
                0.36D,
                toward.z * 0.88D
        );
        mob.hurtMarked = true;

        level.sendParticles(
                ParticleTypes.POOF,
                mob.getX(),
                mob.getY() + 0.10D,
                mob.getZ(),
                4,
                0.12D,
                0.05D,
                0.12D,
                0.01D
        );

        setCooldown(mob, CAT_READY, 8 * 20);
    }

    private static Vec3 horizontalNormal(Vec3 vector) {
        Vec3 horizontal = new Vec3(
                vector.x,
                0.0D,
                vector.z
        );

        if (horizontal.lengthSqr() < 0.0001D) {
            return new Vec3(1.0D, 0.0D, 0.0D);
        }

        return horizontal.normalize();
    }

    private static boolean isUsefulScavengeLoot(ItemStack stack) {
        if (stack == null
                || stack.isEmpty()
                || stack.isEdible()) {
            return false;
        }

        return stack.isDamageableItem()
                || stack.isEnchanted()
                || stack.getRarity() != Rarity.COMMON
                || stack.is(Items.DIAMOND)
                || stack.is(Items.EMERALD)
                || stack.is(Items.GOLD_INGOT)
                || stack.is(Items.IRON_INGOT)
                || stack.is(Items.ENDER_PEARL)
                || stack.is(Items.ARROW);
    }

    private static boolean tryCollectScavengedItem(
            LivingEntity entity,
            ItemEntity item
    ) {
        Method method = getCollectMethod(entity.getClass());
        if (method == null) {
            return false;
        }

        try {
            Object result = method.invoke(entity, item);
            return result instanceof Boolean value && value;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return false;
        }
    }

    @Nullable
    private static Method getCollectMethod(Class<?> entityClass) {
        if (cachedCollectClass == entityClass) {
            return cachedCollectMethod;
        }

        cachedCollectClass = entityClass;
        cachedCollectMethod = null;

        try {
            cachedCollectMethod = entityClass.getMethod(
                    "collectRacialScavengeItem",
                    ItemEntity.class
            );
        } catch (NoSuchMethodException ignored) {
        }

        return cachedCollectMethod;
    }

    private static boolean isReady(
            LivingEntity entity,
            String key
    ) {
        return readyAt(entity, key)
                <= entity.level().getGameTime();
    }

    private static long readyAt(
            LivingEntity entity,
            String key
    ) {
        CompoundTag persistent = entity.getPersistentData();
        if (!persistent.contains(AI_ROOT)) {
            return 0L;
        }

        return persistent.getCompound(AI_ROOT)
                .getLong(key);
    }

    private static void setCooldown(
            LivingEntity entity,
            String key,
            int ticks
    ) {
        CompoundTag persistent = entity.getPersistentData();
        CompoundTag root = persistent.contains(AI_ROOT)
                ? persistent.getCompound(AI_ROOT)
                : new CompoundTag();

        root.putLong(
                key,
                entity.level().getGameTime() + ticks
        );

        persistent.put(AI_ROOT, root);
    }
}
