package com.cyberspectraa.cyberraces.event;

import com.cyberspectraa.cyberraces.compat.FairyWingCompat;
import com.cyberspectraa.cyberraces.compat.NaturalWingCompat;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.curios.api.event.CurioUnequipEvent;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;

public final class FairyWingEvents {
    private FairyWingEvents() {
    }

    @SubscribeEvent
    public static void onUnequip(CurioUnequipEvent event) {
        if (FairyWingCompat.isRacialFairyWing(event.getStack())
                || NaturalWingCompat.isNaturalWing(event.getStack())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onDropRules(DropRulesEvent event) {
        event.addOverride(FairyWingCompat::isRacialFairyWing, ICurio.DropRule.ALWAYS_KEEP);
        event.addOverride(NaturalWingCompat::isNaturalWing, ICurio.DropRule.ALWAYS_KEEP);
    }

}
