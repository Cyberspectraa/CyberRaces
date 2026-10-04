package com.cyberspectraa.cyberraces.character;

import net.minecraft.nbt.CompoundTag;

public record CharacterAppearance(
    int featureStyle,
    int featureColor,
    int earHeight,
    int earSpread,
    int earTilt,
    int bodySourceColor,
    int bodyTargetColor,
    int bodyTolerance
) {
    public static final int FEATURE_STYLE_COUNT = 3;
    public static final int AUTO_COLOR = -1;
    public static final int EAR_MIN = -8;
    public static final int EAR_MAX = 8;
    public static final int BODY_TOLERANCE_MIN = 4;
    public static final int BODY_TOLERANCE_MAX = 60;
    public static final int BODY_TOLERANCE_DEFAULT = 22;

    public CharacterAppearance {
        featureStyle = clamp(featureStyle, 0, FEATURE_STYLE_COUNT - 1);
        featureColor = normalizeColor(featureColor);
        earHeight = clamp(earHeight, EAR_MIN, EAR_MAX);
        earSpread = clamp(earSpread, EAR_MIN, EAR_MAX);
        earTilt = clamp(earTilt, EAR_MIN, EAR_MAX);
        bodySourceColor = normalizeColor(bodySourceColor);
        bodyTargetColor = normalizeColor(bodyTargetColor);
        bodyTolerance = clamp(bodyTolerance, BODY_TOLERANCE_MIN, BODY_TOLERANCE_MAX);
    }

    public static CharacterAppearance defaults() {
        return new CharacterAppearance(
            0,
            AUTO_COLOR,
            0,
            0,
            0,
            AUTO_COLOR,
            AUTO_COLOR,
            BODY_TOLERANCE_DEFAULT
        );
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("FeatureStyle", featureStyle);
        tag.putInt("FeatureRgb", featureColor);
        tag.putInt("EarHeight", earHeight);
        tag.putInt("EarSpread", earSpread);
        tag.putInt("EarTilt", earTilt);
        tag.putInt("BodySourceRgb", bodySourceColor);
        tag.putInt("BodyTargetRgb", bodyTargetColor);
        tag.putInt("BodyTolerance", bodyTolerance);
        return tag;
    }

    public static CharacterAppearance load(CompoundTag tag) {
        int featureRgb = tag.contains("FeatureRgb")
            ? tag.getInt("FeatureRgb")
            : AUTO_COLOR;

        int bodySourceRgb = tag.contains("BodySourceRgb")
            ? tag.getInt("BodySourceRgb")
            : AUTO_COLOR;

        int bodyTargetRgb = tag.contains("BodyTargetRgb")
            ? tag.getInt("BodyTargetRgb")
            : AUTO_COLOR;

        int bodyTolerance = tag.contains("BodyTolerance")
            ? tag.getInt("BodyTolerance")
            : BODY_TOLERANCE_DEFAULT;

        return new CharacterAppearance(
            tag.getInt("FeatureStyle"),
            featureRgb,
            tag.getInt("EarHeight"),
            tag.getInt("EarSpread"),
            tag.getInt("EarTilt"),
            bodySourceRgb,
            bodyTargetRgb,
            bodyTolerance
        );
    }

    public boolean hasBodyRecolour() {
        return bodySourceColor != AUTO_COLOR && bodyTargetColor != AUTO_COLOR;
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
