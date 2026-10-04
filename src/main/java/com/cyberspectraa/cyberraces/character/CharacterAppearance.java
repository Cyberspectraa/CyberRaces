package com.cyberspectraa.cyberraces.character;

import net.minecraft.nbt.CompoundTag;

public record CharacterAppearance(int featureStyle) {
    public static final int FEATURE_STYLE_COUNT = 3;

    public CharacterAppearance {
        featureStyle = Math.max(0, Math.min(FEATURE_STYLE_COUNT - 1, featureStyle));
    }

    public static CharacterAppearance defaults() {
        return new CharacterAppearance(0);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("FeatureStyle", featureStyle);
        return tag;
    }

    public static CharacterAppearance load(CompoundTag tag) {
        return new CharacterAppearance(tag.getInt("FeatureStyle"));
    }
}
