package com.cyberspectraa.cyberraces.event;

import com.cyberspectraa.cyberraces.character.CharacterManager;
import com.cyberspectraa.cyberraces.progression.ProgressionManager;
import net.minecraft.advancements.FrameType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ProgressionEvents {
    private ProgressionEvents() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ProgressionManager.ensure(player);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!(event.getOriginal() instanceof ServerPlayer oldPlayer)
                || !(event.getEntity() instanceof ServerPlayer newPlayer)) {
            return;
        }

        event.getOriginal().reviveCaps();
        ProgressionManager.copyData(oldPlayer, newPlayer);
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !CharacterManager.isCharacterCreated(player)) {
            return;
        }

        ProgressionManager.awardCombatKill(
            player,
            event.getEntity()
        );
    }

    @SubscribeEvent
    public static void onAdvancement(
        AdvancementEvent.AdvancementEarnEvent event
    ) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !CharacterManager.isCharacterCreated(player)) {
            return;
        }

        event.getAdvancement().getDisplay().ifPresent(display -> {
            long reward = switch (display.getFrame()) {
                case TASK -> 75L;
                case GOAL -> 150L;
                case CHALLENGE -> 300L;
            };

            ProgressionManager.addExperience(
                player,
                reward,
                "advancement"
            );
        });
    }
}
