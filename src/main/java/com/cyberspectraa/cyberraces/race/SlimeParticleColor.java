package com.cyberspectraa.cyberraces.race;

import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import net.minecraft.core.particles.DustParticleOptions;
import org.joml.Vector3f;

/**
 * Gel splashes match the saved slime variant, including mod-provided colours.
 * The CharacterAppearance feature RGB now stores the selected tint as well as
 * the stable variant slot; legacy characters use their original slot colour.
 */
public final class SlimeParticleColor {
    private static final int[] STANDARD = {
        0x73D36A, 0xF47C37, 0xA8C5CF, 0xBAED37,
        0x94E9F9, 0x79C9F2, 0x8360B1, 0xF4D16B,
        0x69C9F0, 0x74AC71, 0xA96CDD, 0xEDAD5F
    };

    private SlimeParticleColor() {}

    public static int colorForStyle(int slot) {
        return slot >= 0 && slot < STANDARD.length
                ? STANDARD[slot] : STANDARD[0];
    }

    public static DustParticleOptions forAppearance(CharacterAppearance appearance) {
        int rgb = appearance.featureColor();
        if (rgb == CharacterAppearance.AUTO_COLOR) {
            rgb = colorForStyle(appearance.featureStyle());
        }
        return new DustParticleOptions(new Vector3f(
                ((rgb >> 16) & 255) / 255.0F,
                ((rgb >> 8) & 255) / 255.0F,
                (rgb & 255) / 255.0F), 0.9F);
    }
}
