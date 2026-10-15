package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.CyberRaces;
import com.cyberspectraa.cyberraces.race.Race;
import net.minecraft.resources.ResourceLocation;

public final class BeastVariantTextures {
    // Texture choices provided in the user's Figura CatEars.zip.
    // Existing saved race/feature IDs continue to load; old indices
    // outside this colour range use the nearest valid source variant.
    private static final String[] CAT_NAMES = {
        "Black", "Blue-ish", "Purple", "White"
    };

    private static final ResourceLocation[] CAT_TEXTURES = {
        cyber("textures/entity/beast/figura_cat_black.png"),
        cyber("textures/entity/beast/figura_cat_blue_ish.png"),
        cyber("textures/entity/beast/figura_cat_purple.png"),
        cyber("textures/entity/beast/figura_cat_white.png")
    };

    private static final String[] DOG_NAMES = {
        "Pale",
        "Woods",
        "Ashen",
        "Black",
        "Chestnut",
        "Rusty",
        "Spotted",
        "Striped",
        "Snowy"
    };

    private static final ResourceLocation[] DOG_TEXTURES = {
        minecraft("textures/entity/wolf/wolf.png"),
        cyber("textures/entity/beast/wolf_woods.png"),
        cyber("textures/entity/beast/wolf_ashen.png"),
        cyber("textures/entity/beast/wolf_black.png"),
        cyber("textures/entity/beast/wolf_chestnut.png"),
        cyber("textures/entity/beast/wolf_rusty.png"),
        cyber("textures/entity/beast/wolf_spotted.png"),
        cyber("textures/entity/beast/wolf_striped.png"),
        cyber("textures/entity/beast/wolf_snowy.png")
    };

    private static final String[] FOX_NAMES = {
        "Red",
        "Snow"
    };

    private static final ResourceLocation[] FOX_TEXTURES = {
        minecraft("textures/entity/fox/fox.png"),
        minecraft("textures/entity/fox/snow_fox.png")
    };

    private BeastVariantTextures() {
    }

    public static boolean isBeastfolk(Race race) {
        return race == Race.CATFOLK
            || race == Race.DOGFOLK
            || race == Race.FOXFOLK;
    }

    public static int count(Race race) {
        return switch (race) {
            case CATFOLK -> CAT_NAMES.length;
            case DOGFOLK -> DOG_NAMES.length;
            case FOXFOLK -> FOX_NAMES.length;
            default -> 3;
        };
    }

    public static String name(Race race, int index) {
        return switch (race) {
            case CATFOLK -> CAT_NAMES[clamp(index, CAT_NAMES.length)];
            case DOGFOLK -> DOG_NAMES[clamp(index, DOG_NAMES.length)];
            case FOXFOLK -> FOX_NAMES[clamp(index, FOX_NAMES.length)];
            default -> "";
        };
    }

    public static ResourceLocation texture(Race race, int index) {
        return switch (race) {
            case CATFOLK -> CAT_TEXTURES[clamp(index, CAT_TEXTURES.length)];
            case DOGFOLK -> DOG_TEXTURES[clamp(index, DOG_TEXTURES.length)];
            case FOXFOLK -> FOX_TEXTURES[clamp(index, FOX_TEXTURES.length)];
            default -> throw new IllegalArgumentException("Not a Beastfolk race: " + race);
        };
    }

    private static int clamp(int index, int length) {
        return Math.max(0, Math.min(length - 1, index));
    }

    private static ResourceLocation minecraft(String path) {
        return new ResourceLocation("minecraft", path);
    }

    private static ResourceLocation cyber(String path) {
        return new ResourceLocation(CyberRaces.MOD_ID, path);
    }
}
