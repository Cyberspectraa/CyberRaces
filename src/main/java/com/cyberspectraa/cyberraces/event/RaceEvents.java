package com.cyberspectraa.cyberraces.event;

import com.cyberspectraa.cyberraces.CyberRaces;
import com.cyberspectraa.cyberraces.compat.IcarusCompat;
import com.cyberspectraa.cyberraces.race.FlightType;
import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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

        RaceManager.reapply(player);

        if (!RaceManager.hasRace(player)) {
            player.sendSystemMessage(
                Component.literal("Choose your race with ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal("/cyberraces race choose <race>").withStyle(ChatFormatting.YELLOW))
            );
            player.sendSystemMessage(
                Component.literal("Use /cyberraces race list to see the available races.")
                    .withStyle(ChatFormatting.GRAY)
            );
        } else {
            Race race = RaceManager.getRace(player).orElseThrow();
            if (race.flightType() == FlightType.ICARUS_NATURAL && !IcarusCompat.isLoaded()) {
                player.sendSystemMessage(
                    Component.literal("CyberRaces: this race expects Icarus for natural flight, but Icarus is not loaded.")
                        .withStyle(ChatFormatting.RED)
                );
            }
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        if (!(event.getOriginal() instanceof ServerPlayer oldPlayer)
            || !(event.getEntity() instanceof ServerPlayer newPlayer)) {
            return;
        }

        RaceManager.copyRaceData(oldPlayer, newPlayer);
        RaceManager.reapply(newPlayer);
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
