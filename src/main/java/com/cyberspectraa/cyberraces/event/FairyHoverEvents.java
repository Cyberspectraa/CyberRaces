package com.cyberspectraa.cyberraces.event;

import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Server-side validation/safety half of the Fairy hover.
 *
 * The local client runs the same lightweight Y-only physics so movement feels
 * immediate. The server mirrors it without forcing a velocity packet every
 * tick, which avoids the old "rubber-band" hover feeling.
 */
public final class FairyHoverEvents {
    private FairyHoverEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
            || !(event.player instanceof ServerPlayer player)
            || RaceManager.getRace(player).orElse(null) != Race.FAIRY) {
            return;
        }

        FairyHoverPhysics.apply(player);
    }
}
