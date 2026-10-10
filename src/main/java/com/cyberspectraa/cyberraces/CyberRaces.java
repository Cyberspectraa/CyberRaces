package com.cyberspectraa.cyberraces;

import com.cyberspectraa.cyberraces.command.CyberProgressionCommands;
import com.cyberspectraa.cyberraces.command.CyberRaceCommands;
import com.cyberspectraa.cyberraces.command.CyberResetAllCommands;
import com.cyberspectraa.cyberraces.event.CharacterCreationEvents;
import com.cyberspectraa.cyberraces.event.CyberNpcRaceEvents;
import com.cyberspectraa.cyberraces.event.FairyHoverEvents;
import com.cyberspectraa.cyberraces.event.FairyWingEvents;
import com.cyberspectraa.cyberraces.event.ProgressionEvents;
import com.cyberspectraa.cyberraces.event.RaceEvents;
import com.cyberspectraa.cyberraces.event.RacialPassiveEvents;
import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CyberRaces.MOD_ID)
public final class CyberRaces {
    public static final String MOD_ID = "cyberraces";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CyberRaces() {
        CyberRacesNetwork.init();

        MinecraftForge.EVENT_BUS.register(RaceEvents.class);
        MinecraftForge.EVENT_BUS.register(RacialPassiveEvents.class);
        MinecraftForge.EVENT_BUS.register(FairyWingEvents.class);
        MinecraftForge.EVENT_BUS.register(FairyHoverEvents.class);
        MinecraftForge.EVENT_BUS.register(CharacterCreationEvents.class);
        MinecraftForge.EVENT_BUS.register(CyberNpcRaceEvents.class);
        MinecraftForge.EVENT_BUS.register(ProgressionEvents.class);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

        LOGGER.info("CyberRaces loaded");
    }

    private void registerCommands(RegisterCommandsEvent event) {
        CyberRaceCommands.register(event.getDispatcher());
        CyberProgressionCommands.register(event.getDispatcher());
        CyberResetAllCommands.register(event.getDispatcher());
    }
}
