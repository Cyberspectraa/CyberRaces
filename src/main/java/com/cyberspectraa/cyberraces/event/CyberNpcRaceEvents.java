package com.cyberspectraa.cyberraces.event;

import com.cyberspectraa.cyberraces.compat.CyberNpcRaceManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
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
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.level().isClientSide
            || !CyberNpcRaceManager.isCyberNpc(entity)) {
            return;
        }

        // Fallback for unusual spawn flows where the NPC becomes Wild after
        // EntityJoinLevelEvent. Once assigned, this branch becomes a cheap
        // persistent-data presence check.
        if (CyberNpcRaceManager.isWildCyberNpc(entity)) {
            if (!CyberNpcRaceManager.hasRace(entity)) {
                CyberNpcRaceManager.ensureAssigned(entity);
                CyberNpcRaceManager.broadcast(entity);
            }
        } else if (CyberNpcRaceManager.hasRace(entity)) {
            CyberNpcRaceManager.clearIfNotWild(entity);
        }
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

        CyberNpcRaceManager.getRace(entity).ifPresent(race -> {
            if (race.fireDamageMultiplier() != 1.0) {
                event.setAmount(
                    (float) (
                        event.getAmount()
                            * race.fireDamageMultiplier()
                    )
                );
            }
        });
    }
}
