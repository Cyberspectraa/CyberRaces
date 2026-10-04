package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.race.Race;

public final class FeatureColourPalette {
    public static final String[] NAMES = {
        "Natural",
        "Pale",
        "Tan",
        "Brown",
        "Dark",
        "Olive",
        "Green",
        "Red",
        "Purple",
        "Blue",
        "Ash"
    };

    private static final float[][] COLOURS = {
        {1.00F, 1.00F, 1.00F},
        {1.00F, 0.79F, 0.70F},
        {0.80F, 0.56F, 0.39F},
        {0.50F, 0.31F, 0.22F},
        {0.28F, 0.19F, 0.16F},
        {0.58F, 0.64F, 0.34F},
        {0.34F, 0.66F, 0.34F},
        {0.78F, 0.30F, 0.34F},
        {0.58F, 0.40F, 0.72F},
        {0.34F, 0.52F, 0.78F},
        {0.68F, 0.68F, 0.68F}
    };

    private FeatureColourPalette() {
    }

    public static int count() {
        return NAMES.length;
    }

    public static String name(int index) {
        return NAMES[clamp(index)];
    }

    public static float[] rgb(Race race, int index) {
        int safe = clamp(index);

        if (safe == 0) {
            return switch (race) {
                case TIEFLING -> new float[] {0.86F, 0.34F, 0.39F};
                case DRAGONBORN -> new float[] {0.46F, 0.68F, 0.62F};
                default -> COLOURS[0];
            };
        }

        return COLOURS[safe];
    }

    public static boolean usesPlayerSkin(Race race, int index) {
        if (index != 0) {
            return false;
        }

        return switch (race) {
            case ELF, HALFLING, ORC, GOBLIN, FAIRY -> true;
            default -> false;
        };
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(NAMES.length - 1, value));
    }
}
