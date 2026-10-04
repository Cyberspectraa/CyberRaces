package com.cyberspectraa.cyberraces;

import com.cyberspectraa.cyberraces.command.CyberRaceCommands;
import com.cyberspectraa.cyberraces.event.FairyWingEvents;
import com.cyberspectraa.cyberraces.event.RaceEvents;
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
        MinecraftForge.EVENT_BUS.register(RaceEvents.class);
        MinecraftForge.EVENT_BUS.register(FairyWingEvents.class);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

        LOGGER.info("CyberRaces loaded");
    }

    private void registerCommands(RegisterCommandsEvent event) {
        CyberRaceCommands.register(event.getDispatcher());
    }
}
