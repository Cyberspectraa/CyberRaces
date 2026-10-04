package com.cyberspectraa.cyberraces.character;

import net.minecraft.nbt.CompoundTag;

public record CharacterAppearance(
    int featureStyle,
    int featureColor,
    int earHeight,
    int earSpread,
    int earTilt
) {
    public static final int FEATURE_STYLE_COUNT = 3;
    public static final int AUTO_COLOR = -1;
    public static final int EAR_MIN = -8;
    public static final int EAR_MAX = 8;

    public CharacterAppearance {
        featureStyle = clamp(featureStyle, 0, FEATURE_STYLE_COUNT - 1);
        featureColor = normalizeColor(featureColor);
        earHeight = clamp(earHeight, EAR_MIN, EAR_MAX);
        earSpread = clamp(earSpread, EAR_MIN, EAR_MAX);
        earTilt = clamp(earTilt, EAR_MIN, EAR_MAX);
    }

    public static CharacterAppearance defaults() {
        return new CharacterAppearance(0, AUTO_COLOR, 0, 0, 0);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("FeatureStyle", featureStyle);
        tag.putInt("FeatureRgb", featureColor);
        tag.putInt("EarHeight", earHeight);
        tag.putInt("EarSpread", earSpread);
        tag.putInt("EarTilt", earTilt);
        return tag;
    }

    public static CharacterAppearance load(CompoundTag tag) {
        // FeatureColor was the old preset-index system. Do not reinterpret it
        // as an RGB value; old characters migrate to automatic/natural colour.
        int color = tag.contains("FeatureRgb") ? tag.getInt("FeatureRgb") : AUTO_COLOR;

        return new CharacterAppearance(
            tag.getInt("FeatureStyle"),
            color,
            tag.getInt("EarHeight"),
            tag.getInt("EarSpread"),
            tag.getInt("EarTilt")
        );
    }

    private static int normalizeColor(int color) {
        if (color == AUTO_COLOR) {
            return AUTO_COLOR;
        }
        return color & 0xFFFFFF;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
