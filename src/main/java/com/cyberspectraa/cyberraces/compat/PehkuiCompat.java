package com.cyberspectraa.cyberraces.compat;

import net.minecraft.world.entity.Entity;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public final class PehkuiCompat {
    private static final int TRANSITION_TICKS = 10;

    private PehkuiCompat() {
    }

    public static void applyScale(Entity entity, float scale) {
        if (entity == null) {
            return;
        }

        ScaleData data = ScaleTypes.BASE.getScaleData(entity);
        data.setScaleTickDelay(TRANSITION_TICKS);
        data.setTargetScale(scale);
    }

    public static void resetScale(Entity entity) {
        applyScale(entity, 1.0f);
    }
}
