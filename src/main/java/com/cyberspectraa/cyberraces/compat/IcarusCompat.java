package com.cyberspectraa.cyberraces.compat;

import net.minecraftforge.fml.ModList;

/**
 * Icarus is optional. The first CyberRaces alpha records which races should
 * receive Icarus-backed natural flight without pretending vanilla creative
 * flight is the finished implementation.
 *
 * The actual wearable/visual wing grant is the next integration milestone.
 */
public final class IcarusCompat {
    private static final String MOD_ID = "icarus";

    private IcarusCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }
}
