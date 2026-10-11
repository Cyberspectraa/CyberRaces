package com.cyberspectraa.cyberraces.event;

import com.cyberspectraa.cyberraces.character.CharacterManager;
import com.cyberspectraa.cyberraces.race.RaceEvolutionJourney;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/** Loaded only when Iron's Spellbooks is installed. */
public final class RaceEvolutionSpellEvents {
    private RaceEvolutionSpellEvents() {}
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCast(SpellPreCastEvent event) {
        if (event.isCanceled()) return;
        if (event.getEntity() instanceof ServerPlayer player
                && CharacterManager.isCharacterCreated(player)) {
            RaceEvolutionJourney.magic(player);
        }
    }
}
