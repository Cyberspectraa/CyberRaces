package com.cyberspectraa.cyberraces.event;

import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * The Undead condition borrows zombie traits without changing Player's
 * MobType (which would affect every modded effect and vanilla player rule).
 *
 * Applies to vanilla and modded undead that correctly report MobType.UNDEAD,
 * including CyberNpc zombies (subclasses of vanilla Zombie).
 */
public final class UndeadMobBehaviorEvents {
    private UndeadMobBehaviorEvents() {}

    private static boolean isUndeadPlayer(ServerPlayer player) {
        return RaceManager.getRace(player).orElse(null) == Race.UNDEAD;
    }

    private static boolean undeadMob(Mob mob) {
        return mob.getMobType() == MobType.UNDEAD;
    }

    // Prevent target selection before the zombie begins a chase. This also
    // intercepts CyberNpc's custom closest-target selector.
    @SubscribeEvent
    public static void onTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)
                || !undeadMob(mob)
                || !(event.getNewTarget() instanceof ServerPlayer player)
                || !isUndeadPlayer(player)) {
            return;
        }
        event.setNewTarget(null);
    }

    // A mob may have targeted the player before they transformed or before
    // data finished loading. Clear any such stale target promptly.
    @SubscribeEvent
    public static void onMobTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Mob mob)
                || mob.level().isClientSide
                || (mob.tickCount + mob.getId()) % 5 != 0
                || !undeadMob(mob)
                || !(mob.getTarget() instanceof ServerPlayer player)
                || !isUndeadPlayer(player)) {
            return;
        }
        mob.setTarget(null);
        mob.getNavigation().stop();
    }

    // Defensive protection for vanilla melee, ranged attacks and any mob
    // that inflicts damage without using a vanilla target goal.
    @SubscribeEvent
    public static void onUndeadAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && isUndeadPlayer(player)
                && event.getSource().getEntity() instanceof Mob attacker
                && undeadMob(attacker)) {
            event.setCanceled(true);
        }
    }

    /** Uncovered Undead players burn in daylight, like unhelmeted zombies. */
    @SubscribeEvent
    public static void onUndeadPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 20 != 0
                || !isUndeadPlayer(player)
                || player.isCreative() || player.isSpectator()
                || player.isInWaterRainOrBubble()
                || !player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
                || !player.level().isDay()
                || !player.level().canSeeSky(player.blockPosition())
                || player.level().getBrightness(
                        net.minecraft.world.level.LightLayer.SKY,
                        player.blockPosition()) < 12) {
            return;
        }

        if (!player.isOnFire()) player.setSecondsOnFire(6);
        if (player.level() instanceof ServerLevel server
                && player.tickCount % 40 == 0) {
            server.sendParticles(ParticleTypes.SMOKE,
                    player.getX(), player.getY() + player.getBbHeight() * 0.8D,
                    player.getZ(), 2, 0.2D, 0.15D, 0.2D, 0.01D);
        }
    }
}
