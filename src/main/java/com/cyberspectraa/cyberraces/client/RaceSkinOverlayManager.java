package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import com.cyberspectraa.cyberraces.race.Race;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RaceSkinOverlayManager {
    private static final Map<UUID, CacheEntry> CACHE = new HashMap<>();
    private static final Map<UUID, ResourceLocation> ORIGINAL_SKINS = new HashMap<>();

    private RaceSkinOverlayManager() {
    }

    public static ResourceLocation getReplacement(
        AbstractClientPlayer player,
        Race race,
        CharacterAppearance appearance,
        ResourceLocation originalSkin
    ) {
        rememberOriginal(player.getUUID(), originalSkin);

        if (!supports(race) || !appearance.hasBodyRecolour()) {
            invalidate(player.getUUID());
            return originalSkin;
        }

        CacheKey key = new CacheKey(
            originalSkin,
            appearance.bodySourceColor(),
            appearance.bodyTargetColor(),
            appearance.bodyTolerance()
        );

        CacheEntry existing = CACHE.get(player.getUUID());
        if (existing != null && existing.key().equals(key)) {
            return existing.texture();
        }

        invalidate(player.getUUID());

        ResourceLocation generated = generate(
            player,
            originalSkin,
            appearance
        );

        if (generated != null) {
            CACHE.put(
                player.getUUID(),
                new CacheEntry(key, generated)
            );
            return generated;
        }

        return originalSkin;
    }

    public static void rememberOriginal(
        UUID playerId,
        ResourceLocation originalSkin
    ) {
        ORIGINAL_SKINS.put(playerId, originalSkin);
    }

    public static ResourceLocation originalSkin(AbstractClientPlayer player) {
        return ORIGINAL_SKINS.get(player.getUUID());
    }

    public static void invalidate(UUID playerId) {
        CacheEntry entry = CACHE.remove(playerId);
        if (entry != null) {
            Minecraft.getInstance()
                .getTextureManager()
                .release(entry.texture());
        }
    }

    public static void clearAll() {
        Minecraft minecraft = Minecraft.getInstance();

        for (CacheEntry entry : CACHE.values()) {
            minecraft.getTextureManager().release(entry.texture());
        }

        CACHE.clear();
        ORIGINAL_SKINS.clear();
    }

    public static boolean supports(Race race) {
        return race == Race.GOBLIN || race == Race.TIEFLING;
    }

    public static int defaultTarget(Race race) {
        return switch (race) {
            case GOBLIN -> 0x6E9347;
            case TIEFLING -> 0xB84E5C;
            default -> CharacterAppearance.AUTO_COLOR;
        };
    }

    private static ResourceLocation generate(
        AbstractClientPlayer player,
        ResourceLocation originalSkin,
        CharacterAppearance appearance
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        try {
            AbstractTexture sourceTexture =
                minecraft.getTextureManager().getTexture(originalSkin);

            sourceTexture.bind();

            try (NativeImage source = new NativeImage(64, 64, false)) {
                source.downloadTexture(0, false);

                NativeImage recoloured = new NativeImage(64, 64, true);
                buildRecolouredSkin(source, recoloured, appearance);

                DynamicTexture dynamicTexture =
                    new DynamicTexture(recoloured);

                String name =
                    "cyberraces_skin_"
                        + player.getUUID().toString().replace("-", "")
                        + "_"
                        + Integer.toHexString(
                            appearance.bodySourceColor()
                                ^ appearance.bodyTargetColor()
                                ^ appearance.bodyTolerance()
                        );

                ResourceLocation location =
                    minecraft.getTextureManager().register(
                        name,
                        dynamicTexture
                    );

                dynamicTexture.upload();
                return location;
            }
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static void buildRecolouredSkin(
        NativeImage source,
        NativeImage result,
        CharacterAppearance appearance
    ) {
        int selected = appearance.bodySourceColor();
        int target = appearance.bodyTargetColor();

        int sourceR = (selected >> 16) & 0xFF;
        int sourceG = (selected >> 8) & 0xFF;
        int sourceB = selected & 0xFF;

        int targetR = (target >> 16) & 0xFF;
        int targetG = (target >> 8) & 0xFF;
        int targetB = target & 0xFF;

        double sourceLuma = Math.max(
            12.0D,
            luminance(sourceR, sourceG, sourceB)
        );

        double threshold =
            appearance.bodyTolerance() * 2.35D;
        double thresholdSquared = threshold * threshold;

        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                int originalPixel = source.getPixelRGBA(x, y);
                int alpha =
                    source.getLuminanceOrAlpha(x, y) & 0xFF;

                if (alpha < 16) {
                    result.setPixelRGBA(x, y, originalPixel);
                    continue;
                }

                int red =
                    source.getRedOrLuminance(x, y) & 0xFF;
                int green =
                    source.getGreenOrLuminance(x, y) & 0xFF;
                int blue =
                    source.getBlueOrLuminance(x, y) & 0xFF;

                int dr = red - sourceR;
                int dg = green - sourceG;
                int db = blue - sourceB;

                double distanceSquared =
                    dr * dr + dg * dg + db * db;

                if (distanceSquared > thresholdSquared) {
                    result.setPixelRGBA(x, y, originalPixel);
                    continue;
                }

                double shade =
                    luminance(red, green, blue) / sourceLuma;

                shade = Math.max(
                    0.34D,
                    Math.min(1.72D, shade)
                );

                int newR =
                    clamp((int) Math.round(targetR * shade));
                int newG =
                    clamp((int) Math.round(targetG * shade));
                int newB =
                    clamp((int) Math.round(targetB * shade));

                result.setPixelRGBA(
                    x,
                    y,
                    abgr(alpha, newR, newG, newB)
                );
            }
        }
    }

    private static double luminance(
        int red,
        int green,
        int blue
    ) {
        return red * 0.2126D
            + green * 0.7152D
            + blue * 0.0722D;
    }

    private static int abgr(
        int alpha,
        int red,
        int green,
        int blue
    ) {
        return ((alpha & 0xFF) << 24)
            | ((blue & 0xFF) << 16)
            | ((green & 0xFF) << 8)
            | (red & 0xFF);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private record CacheKey(
        ResourceLocation sourceTexture,
        int sourceColor,
        int targetColor,
        int tolerance
    ) {
    }

    private record CacheEntry(
        CacheKey key,
        ResourceLocation texture
    ) {
    }
}
