package com.cyberspectraa.cyberraces.compat;

import net.minecraft.server.level.ServerPlayer;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public final class PehkuiCompat {
    private static final int TRANSITION_TICKS = 10;

    private PehkuiCompat() {
    }

    public static void applyScale(ServerPlayer player, float scale) {
        ScaleData data = ScaleTypes.BASE.getScaleData(player);
        data.setScaleTickDelay(TRANSITION_TICKS);
        data.setTargetScale(scale);
    }

    public static void resetScale(ServerPlayer player) {
        applyScale(player, 1.0f);
    }
}
