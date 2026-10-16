package com.cyberspectraa.cyberraces.race;

/**
 * Dragonborn element and horn shape share the existing appearance style integer.
 * Slots 0-2 are the original horn shapes, so old characters remain Fire dragons.
 * Every colour is an actual Icarus dragon-wing dye colour.
 */
public final class DragonVariant {
    private static final String[] TYPES = {"Fire", "Frost", "Lightning", "Poison", "Ender"};
    private static final String[] WING_NAMES = {
        "White", "Orange", "Magenta", "Light Blue", "Yellow", "Lime",
        "Pink", "Gray", "Light Gray", "Cyan", "Purple", "Blue",
        "Brown", "Green", "Red", "Black"
    };
    private static final int[] WING_RGB = {
        0xF9FFFE, 0xF9801D, 0xC74EBD, 0x3AB3DA,
        0xFED83D, 0x80C71F, 0xF38BAA, 0x474F52,
        0x9D9D97, 0x169C9C, 0x8932B8, 0x3C44AA,
        0x835432, 0x5E7C16, 0xB02E26, 0x1D1D21
    };

    public static final int TYPE_COUNT = 5;
    public static final int HORN_COUNT = 3;
    public static final int DEFAULT_COLOR = 0x169C9C; // Cyan dragon wings

    private DragonVariant() {}

    public static int type(int savedStyle) {
        return Math.min(TYPE_COUNT - 1, Math.max(0, savedStyle) / HORN_COUNT);
    }

    public static int horns(int savedStyle) {
        return Math.floorMod(savedStyle, HORN_COUNT);
    }

    public static int style(int type, int horns) {
        return Math.floorMod(type, TYPE_COUNT) * HORN_COUNT
                + Math.floorMod(horns, HORN_COUNT);
    }

    public static String name(int style) {
        return TYPES[type(style)];
    }

    public static int nearestDye(int rgb) {
        if (rgb < 0) rgb = DEFAULT_COLOR;
        int index = 0;
        long shortest = Long.MAX_VALUE;
        for (int i = 0; i < WING_RGB.length; i++) {
            long dr = ((rgb >> 16) & 255) - ((WING_RGB[i] >> 16) & 255);
            long dg = ((rgb >> 8) & 255) - ((WING_RGB[i] >> 8) & 255);
            long db = (rgb & 255) - (WING_RGB[i] & 255);
            long distance = dr * dr + dg * dg + db * db;
            if (distance < shortest) {
                shortest = distance;
                index = i;
            }
        }
        return index;
    }

    public static int dyeRgb(int index) {
        return WING_RGB[Math.floorMod(index, WING_RGB.length)];
    }

    public static int normalizedColor(int rgb) {
        return dyeRgb(nearestDye(rgb));
    }

    public static String dyeName(int rgb) {
        return WING_NAMES[nearestDye(rgb)];
    }

    public static int nextColor(int rgb) {
        return dyeRgb(nearestDye(rgb) + 1);
    }
}
