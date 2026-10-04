package com.cyberspectraa.cyberraces.event;

import com.cyberspectraa.cyberraces.character.CharacterManager;
import com.cyberspectraa.cyberraces.character.CharacterSyncService;
import com.cyberspectraa.cyberraces.race.RaceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class RaceEvents {
    private RaceEvents() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        CharacterManager.handleLogin(player);

        if (CharacterManager.isCharacterCreated(player)) {
            RaceManager.reapply(player);
        }

        CharacterSyncService.syncAllTo(player);
        if (CharacterManager.isCharacterCreated(player)) {
            CharacterSyncService.broadcast(player);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer
            && event.getTarget() instanceof ServerPlayer target) {
            CharacterSyncService.syncTo(target, viewer);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!(event.getOriginal() instanceof ServerPlayer oldPlayer)
            || !(event.getEntity() instanceof ServerPlayer newPlayer)) {
            return;
        }

        RaceManager.copyRaceData(oldPlayer, newPlayer);

        if (CharacterManager.isCharacterCreated(newPlayer)) {
            RaceManager.reapply(newPlayer);
            CharacterSyncService.broadcast(newPlayer);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!event.getSource().is(DamageTypeTags.IS_FIRE)) {
            return;
        }

        RaceManager.getRace(player).ifPresent(race -> {
            if (race.fireDamageMultiplier() != 1.0) {
                event.setAmount((float) (event.getAmount() * race.fireDamageMultiplier()));
            }
        });
    }
}
