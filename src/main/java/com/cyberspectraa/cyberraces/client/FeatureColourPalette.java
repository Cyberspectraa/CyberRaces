package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import com.cyberspectraa.cyberraces.race.Race;

public final class FeatureColourPalette {
    private FeatureColourPalette() {
    }

    public static int defaultRgb(Race race) {
        return switch (race) {
            case TIEFLING -> 0xC24F5B;
            case DRAGONBORN -> 0x76AFA1;
            default -> 0xD8A38D;
        };
    }

    public static float[] rgb(Race race, int packedRgb) {
        int rgb = packedRgb == CharacterAppearance.AUTO_COLOR
            ? defaultRgb(race)
            : packedRgb & 0xFFFFFF;

        return new float[] {
            ((rgb >> 16) & 0xFF) / 255.0F,
            ((rgb >> 8) & 0xFF) / 255.0F,
            (rgb & 0xFF) / 255.0F
        };
    }

    public static boolean usesPlayerSkin(Race race, int packedRgb) {
        if (packedRgb != CharacterAppearance.AUTO_COLOR) {
            return false;
        }

        return switch (race) {
            case ELF, HALFLING, ORC, GOBLIN, FAIRY -> true;
            default -> false;
        };
    }

    public static String label(Race race, int packedRgb) {
        if (packedRgb == CharacterAppearance.AUTO_COLOR) {
            return switch (race) {
                case ELF, HALFLING, ORC, GOBLIN, FAIRY -> "Skin";
                default -> "Natural";
            };
        }

        return String.format("#%06X", packedRgb & 0xFFFFFF);
    }

    public static boolean hasEars(Race race) {
        return switch (race) {
            case ELF, HALFLING, ORC, GOBLIN, FAIRY -> true;
            default -> false;
        };
    }
}
