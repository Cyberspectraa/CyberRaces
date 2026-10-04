package com.cyberspectraa.cyberraces.character;

import net.minecraft.nbt.CompoundTag;

public record CharacterAppearance(int eyeStyle, int eyeColor, int featureStyle) {
    public static final int EYE_STYLE_COUNT = 4;
    public static final int EYE_COLOR_COUNT = 8;
    public static final int FEATURE_STYLE_COUNT = 3;

    public CharacterAppearance {
        eyeStyle = clamp(eyeStyle, 0, EYE_STYLE_COUNT - 1);
        eyeColor = clamp(eyeColor, 0, EYE_COLOR_COUNT - 1);
        featureStyle = clamp(featureStyle, 0, FEATURE_STYLE_COUNT - 1);
    }

    public static CharacterAppearance defaults() {
        return new CharacterAppearance(0, 0, 0);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("EyeStyle", eyeStyle);
        tag.putInt("EyeColor", eyeColor);
        tag.putInt("FeatureStyle", featureStyle);
        return tag;
    }

    public static CharacterAppearance load(CompoundTag tag) {
        return new CharacterAppearance(
            tag.getInt("EyeStyle"),
            tag.getInt("EyeColor"),
            tag.getInt("FeatureStyle")
        );
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
