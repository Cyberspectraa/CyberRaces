package com.cyberspectraa.cyberraces.event;

import com.cyberspectraa.cyberraces.compat.CyberNpcRaceManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class CyberNpcRaceEvents {
    private CyberNpcRaceEvents() {
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)
            || !(event.getLevel() instanceof ServerLevel level)
            || !CyberNpcRaceManager.isCyberNpc(entity)) {
            return;
        }

        // Spawn eggs/finalizeSpawn can set the CyberNpc type very late in the
        // spawn sequence. Running once on the server queue observes the final
        // Wild/Main/Quest type without adding a permanent polling task.
        level.getServer().execute(() -> {
            if (!entity.isAlive()) {
                return;
            }

            if (CyberNpcRaceManager.isWildCyberNpc(entity)) {
                CyberNpcRaceManager.ensureAssigned(entity);
                CyberNpcRaceManager.broadcast(entity);
            } else {
                CyberNpcRaceManager.clearIfNotWild(entity);
            }
        });
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer
            && event.getTarget() instanceof LivingEntity target
            && CyberNpcRaceManager.isWildCyberNpc(target)) {
            CyberNpcRaceManager.syncTo(target, viewer);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();

        if (!CyberNpcRaceManager.isWildCyberNpc(entity)
            || !event.getSource().is(DamageTypeTags.IS_FIRE)) {
            return;
        }

        double multiplier =
            CyberNpcRaceManager.effectiveFireDamageMultiplier(entity);

        if (multiplier != 1.0D) {
            event.setAmount(
                (float) (event.getAmount() * multiplier)
            );
        }
    }
}
