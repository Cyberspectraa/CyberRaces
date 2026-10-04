package com.cyberspectraa.cyberraces.character;

import net.minecraft.nbt.CompoundTag;

public record CharacterAppearance(int featureStyle, int featureColor) {
    public static final int FEATURE_STYLE_COUNT = 3;
    public static final int FEATURE_COLOR_COUNT = 11;

    public CharacterAppearance {
        featureStyle = Math.max(0, Math.min(FEATURE_STYLE_COUNT - 1, featureStyle));
        featureColor = Math.max(0, Math.min(FEATURE_COLOR_COUNT - 1, featureColor));
    }

    public static CharacterAppearance defaults() {
        return new CharacterAppearance(0, 0);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("FeatureStyle", featureStyle);
        tag.putInt("FeatureColor", featureColor);
        return tag;
    }

    public static CharacterAppearance load(CompoundTag tag) {
        return new CharacterAppearance(
            tag.getInt("FeatureStyle"),
            tag.getInt("FeatureColor")
        );
    }
}
