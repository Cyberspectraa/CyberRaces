package com.cyberspectraa.cyberraces.compat;

import net.minecraftforge.fml.ModList;

/**
 * Icarus provides the actual fall-flying mechanics for races with natural
 * flight. Fairy uses a permanent Zanza's Wings stack in the Curios back slot.
 */
public final class IcarusCompat {
    private static final String MOD_ID = "icarus";

    private IcarusCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }
}
