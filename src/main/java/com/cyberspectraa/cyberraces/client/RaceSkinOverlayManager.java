package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import com.cyberspectraa.cyberraces.race.Race;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class RaceSkinOverlayManager {
    private static final Map<UUID, CacheEntry> CACHE = new HashMap<>();
    private static final Map<UUID, CacheEntry> NPC_OVERLAY_CACHE = new HashMap<>();
    // Slime NPCs on a compatible CyberNpc renderer use one tinted base texture,
    // avoiding a second translucent copy of the animated slim arms.
    private static final Map<UUID, CacheEntry> NPC_SLIME_BASE_CACHE = new HashMap<>();
    private static final Map<UUID, ResourceLocation> ORIGINAL_SKINS = new HashMap<>();

    private RaceSkinOverlayManager() {
    }

    public static ResourceLocation getReplacement(
        AbstractClientPlayer player,
        Race race,
        CharacterAppearance appearance,
        String evolutionId,
        boolean undead,
        ResourceLocation originalSkin
    ) {
        rememberOriginal(player.getUUID(), originalSkin);

        // No automatic tint for Orc, Dragonborn, or Nymph. Players must
        // select a source pixel and target colour as with Goblin.
        if (!undead && (!supports(race)
                || (race != Race.SLIME && !appearance.hasBodyRecolour()))) {
            invalidate(player.getUUID());
            return originalSkin;
        }

        CacheKey key = new CacheKey(
            originalSkin,
            appearance.bodySourceColor(),
            appearance.bodyTargetColor(),
            appearance.bodyTolerance(),
            undead ? "acquired_undead"
                : (race == Race.SLIME ? evolutionId + "#slime:" + appearance.featureStyle() : "")
        );

        CacheEntry existing = CACHE.get(player.getUUID());
        if (existing != null && existing.key().equals(key)) {
            return existing.texture();
        }

        invalidate(player.getUUID());

        ResourceLocation generated = undead
            ? generateBodyTone(player.getUUID(), originalSkin, appearance,
                    0x668B52, "undead")
            : race == Race.SLIME
                ? generateSlime(player.getUUID(), originalSkin, evolutionId, appearance.featureStyle(), false)
                : generate(player, originalSkin, appearance);

        if (generated != null) {
            CACHE.put(
                player.getUUID(),
                new CacheEntry(key, generated)
            );
            return generated;
        }

        return originalSkin;
    }

    /**
     * Optional client-only CyberNpc renderer hook. Composite the Slime colour
     * into the *existing* 64x64 NPC skin instead of duplicating the animated
     * humanoid model with a transparent overlay. This keeps slim female arms
     * and bow draw poses in precisely the same geometry/depth pass.
     *
     * Old CyberNpc versions do not call this method and retain the legacy
     * overlay, so neither mod gains a mandatory dependency on the other.
     */
    public static ResourceLocation getNpcSlimeBaseTexture(
        LivingEntity entity,
        ResourceLocation sourceTexture
    ) {
        if (entity == null || sourceTexture == null) {
            return sourceTexture;
        }

        ClientCharacterState.VisualCharacter visual =
            ClientCharacterState.resolve(entity.getUUID()).orElse(null);
        if (visual == null || visual.race() != Race.SLIME) {
            invalidateNpcSlimeBase(entity.getUUID());
            return sourceTexture;
        }

        String lineage = visual.evolutionId();
        int variation = visual.appearance().featureStyle();
        CacheKey key = new CacheKey(sourceTexture, 0, 0, 0,
                lineage + "#slime:" + variation);
        CacheEntry cached = NPC_SLIME_BASE_CACHE.get(entity.getUUID());
        if (cached != null && cached.key().equals(key)) {
            return cached.texture();
        }

        invalidateNpcSlimeBase(entity.getUUID());

        ResourceLocation composed = generateSlime(
            entity.getUUID(), sourceTexture, lineage, variation, false
        );
        if (composed == null) {
            return sourceTexture;
        }

        NPC_SLIME_BASE_CACHE.put(entity.getUUID(), new CacheEntry(key, composed));
        return composed;
    }

    /**
     * A true result means the renderer has actually selected the composite
     * texture this frame. If an old renderer or a failed texture generation
     * leaves the base unchanged, the old translucent layer remains available.
     */
    public static boolean isNpcSlimeBaseApplied(
        LivingEntity entity,
        ResourceLocation currentlyRenderedTexture
    ) {
        CacheEntry entry = entity == null ? null :
            NPC_SLIME_BASE_CACHE.get(entity.getUUID());
        return entry != null && entry.texture().equals(currentlyRenderedTexture);
    }

    public static ResourceLocation getNpcOverlay(
        LivingEntity entity,
        Race race,
        CharacterAppearance appearance,
        String evolutionId,
        ResourceLocation sourceTexture
    ) {
        if (entity == null
            || sourceTexture == null
            || !supports(race)
            || (race != Race.SLIME && !appearance.hasBodyRecolour())) {
            if (entity != null) {
                invalidateNpc(entity.getUUID());
            }
            return null;
        }

        CacheKey key = new CacheKey(
            sourceTexture,
            appearance.bodySourceColor(),
            appearance.bodyTargetColor(),
            appearance.bodyTolerance(),
            race == Race.SLIME ? evolutionId + "#slime:" + appearance.featureStyle() : ""
        );

        CacheEntry existing = NPC_OVERLAY_CACHE.get(entity.getUUID());
        if (existing != null && existing.key().equals(key)) {
            return existing.texture();
        }

        invalidateNpc(entity.getUUID());

        ResourceLocation generated = race == Race.SLIME
            ? generateSlime(entity.getUUID(), sourceTexture, evolutionId, appearance.featureStyle(), true)
            : generateNpcOverlay(entity, sourceTexture, appearance);

        if (generated != null) {
            NPC_OVERLAY_CACHE.put(
                entity.getUUID(),
                new CacheEntry(key, generated)
            );
        }

        return generated;
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

        invalidateNpc(playerId);
        invalidateNpcSlimeBase(playerId);
    }

    private static void invalidateNpcSlimeBase(UUID entityId) {
        CacheEntry entry = NPC_SLIME_BASE_CACHE.remove(entityId);
        if (entry != null) {
            Minecraft.getInstance().getTextureManager().release(entry.texture());
        }
    }

    private static void invalidateNpc(UUID entityId) {
        CacheEntry entry = NPC_OVERLAY_CACHE.remove(entityId);
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

        for (CacheEntry entry : NPC_OVERLAY_CACHE.values()) {
            minecraft.getTextureManager().release(entry.texture());
        }
        for (CacheEntry entry : NPC_SLIME_BASE_CACHE.values()) {
            minecraft.getTextureManager().release(entry.texture());
        }

        CACHE.clear();
        NPC_OVERLAY_CACHE.clear();
        NPC_SLIME_BASE_CACHE.clear();
        ORIGINAL_SKINS.clear();
    }

    public static boolean supports(Race race) {
        return race == Race.GOBLIN
            || race == Race.ORC
            || race == Race.DRAGONBORN
            || race == Race.NYMPH
            || race == Race.TIEFLING
            || race == Race.CATFOLK
            || race == Race.DOGFOLK
            || race == Race.FOXFOLK
            || race == Race.SLIME;
    }

    public static int defaultTarget(Race race) {
        return switch (race) {
            case GOBLIN -> 0x6E9347;
            case ORC -> 0x6B9552;
            case DRAGONBORN -> FeatureColourPalette.defaultRgb(Race.DRAGONBORN);
            case NYMPH -> 0x83B898;
            case TIEFLING -> 0xB84E5C;
            case CATFOLK -> 0xC49A6C;
            case DOGFOLK -> 0xA97852;
            case FOXFOLK -> 0xC96B35;
            case SLIME -> 0x72C96A;
            default -> CharacterAppearance.AUTO_COLOR;
        };
    }

    /**
     * A full-skin translucent-gel LOOK, not a replacement character model:
     * head, torso, arms, legs and second skin layer retain their UVs and
     * player animations. The source skin's pixels remain visible below a
     * semi-transparent jelly tint with stable tiny glints/bubbles.
     *
     * Players receive a derived composite texture; CyberNpc entities
     * receive only the translucent overlay to draw over their own skin.
     */
    private static ResourceLocation generateSlime(
        UUID entityId, ResourceLocation base, String evolutionId,
        int variantStyle, boolean npcOverlay
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        try {
            AbstractTexture sourceTexture = minecraft.getTextureManager().getTexture(base);
            sourceTexture.bind();
            try (NativeImage source = new NativeImage(64, 64, false)) {
                source.downloadTexture(0, false);
                NativeImage result = new NativeImage(64, 64, true);
                int target = slimeTint(evolutionId, variantStyle);
                int tr = (target >> 16) & 255;
                int tg = (target >> 8) & 255;
                int tb = target & 255;
                for (int y = 0; y < 64; y++) {
                    for (int x = 0; x < 64; x++) {
                        int a = source.getLuminanceOrAlpha(x, y) & 255;
                        if (a < 8) { result.setPixelRGBA(x, y, 0); continue; }
                        int r = source.getRedOrLuminance(x,y) & 255;
                        int g = source.getGreenOrLuminance(x,y) & 255;
                        int b = source.getBlueOrLuminance(x,y) & 255;
                        // An irregular glossy surface, visible even on plain
                        // Steve skins. Small bright spots resemble bubbles.
                        int noise = (x * 17 + y * 29 + x * y * 3) & 31;
                        int gloss = noise < 3 ? 49 : (noise < 7 ? 19 : -4);
                        if ((x * 5 + y * 11) % 37 == 0) gloss += 27;
                        int red = npcOverlay ? clamp(tr + gloss)
                                : clamp((int)(r * 0.43 + tr * 0.57) + gloss);
                        int green = npcOverlay ? clamp(tg + gloss)
                                : clamp((int)(g * 0.43 + tg * 0.57) + gloss);
                        int blue = npcOverlay ? clamp(tb + gloss)
                                : clamp((int)(b * 0.43 + tb * 0.57) + gloss);
                        int alpha = npcOverlay ? Math.min(178, a * 178 / 255) : a;
                        result.setPixelRGBA(x, y, abgr(alpha, red, green, blue));
                    }
                }
                DynamicTexture texture = new DynamicTexture(result);
                ResourceLocation location = minecraft.getTextureManager().register(
                    "cyberraces_slime_" + entityId.toString().replace("-", "")
                        + "_" + Integer.toHexString(target)
                        + (npcOverlay ? "_npc" : "_player"), texture);
                texture.upload();
                return location;
            }
        } catch (RuntimeException exception) {
            return null;
        }
    }

    /**
     * Zombie-coloured skin without tinting the entire outfit. The most common
     * lower-face shade is used to select matching exposed-skin pixels across
     * the existing 64x64 skin; clothing, hair and eyes remain distinct.
     */
    private static ResourceLocation generateBodyTone(
        UUID entityId, ResourceLocation original,
        CharacterAppearance appearance, int tone, String label
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        try {
            AbstractTexture texture = minecraft.getTextureManager().getTexture(original);
            texture.bind();
            try (NativeImage skin = new NativeImage(64, 64, false);
                 NativeImage base = new NativeImage(64, 64, true)) {
                skin.downloadTexture(0, false);
                if (appearance.hasBodyRecolour()) {
                    buildRecolouredSkin(skin, base, appearance);
                } else {
                    for (int y = 0; y < 64; y++) {
                        for (int x = 0; x < 64; x++) {
                            base.setPixelRGBA(x, y, skin.getPixelRGBA(x, y));
                        }
                    }
                }
                int face = lowerFaceSkinColor(base);
                int toneR = (tone >> 16) & 255;
                int toneG = (tone >> 8) & 255;
                int toneB = tone & 255;
                int sr = (face >> 16) & 255;
                int sg = (face >> 8) & 255;
                int sb = face & 255;
                double sourceLuma = Math.max(28.0D, luminance(sr, sg, sb));

                NativeImage output = new NativeImage(64, 64, true);
                for (int y = 0; y < 64; y++) {
                    for (int x = 0; x < 64; x++) {
                        int pixel = base.getPixelRGBA(x, y);
                        int alpha = base.getLuminanceOrAlpha(x, y) & 255;
                        if (alpha < 16) {
                            output.setPixelRGBA(x, y, pixel);
                            continue;
                        }
                        int r = base.getRedOrLuminance(x, y) & 255;
                        int g = base.getGreenOrLuminance(x, y) & 255;
                        int b = base.getBlueOrLuminance(x, y) & 255;
                        int dr = r - sr, dg = g - sg, db = b - sb;
                        int distanceSquared = dr * dr + dg * dg + db * db;

                        if (distanceSquared > 88 * 88) {
                            output.setPixelRGBA(x, y, pixel);
                            continue;
                        }

                        double shade = Math.max(0.32D, Math.min(1.58D,
                                luminance(r, g, b) / sourceLuma));
                        double strength = 0.82D * Math.max(
                                0.16D, 1.0D - Math.sqrt(distanceSquared) / 110D);
                        int targetR = clamp((int) Math.round(toneR * shade));
                        int targetG = clamp((int) Math.round(toneG * shade));
                        int targetB = clamp((int) Math.round(toneB * shade));
                        int rr = clamp((int) Math.round(r * (1D-strength) + targetR * strength));
                        int gg = clamp((int) Math.round(g * (1D-strength) + targetG * strength));
                        int bb = clamp((int) Math.round(b * (1D-strength) + targetB * strength));
                        output.setPixelRGBA(x, y, abgr(alpha, rr, gg, bb));
                    }
                }

                DynamicTexture dynamic = new DynamicTexture(output);
                ResourceLocation key = minecraft.getTextureManager().register(
                        "cyberraces_skin_tone_" + label + "_"
                                + entityId.toString().replace("-", ""),
                        dynamic);
                dynamic.upload();
                return key;
            }
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static int lowerFaceSkinColor(NativeImage image) {
        java.util.Map<Integer, Integer> counts = new java.util.HashMap<>();
        java.util.Map<Integer, Integer> samples = new java.util.HashMap<>();
        for (int y = 12; y <= 15; y++) {
            for (int x = 10; x <= 13; x++) {
                if ((image.getLuminanceOrAlpha(x, y) & 255) < 230) continue;
                int r = image.getRedOrLuminance(x, y) & 255;
                int g = image.getGreenOrLuminance(x, y) & 255;
                int b = image.getBlueOrLuminance(x, y) & 255;
                if (luminance(r, g, b) < 35.0D) continue;
                int bucket = ((r >> 4) << 8) | ((g >> 4) << 4) | (b >> 4);
                counts.merge(bucket, 1, Integer::sum);
                samples.put(bucket, (r << 16) | (g << 8) | b);
            }
        }
        int bestKey = -1, bestCount = -1;
        for (var entry : counts.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestKey = entry.getKey();
                bestCount = entry.getValue();
            }
        }
        return bestKey < 0 ? 0xAD9478 : samples.get(bestKey);
    }

    private static int slimeTint(String evolutionId, int variantStyle) {
        // Slime variations are cosmetic, never evolutions.
        return SlimeVariantPalette.color(variantStyle);
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

    private static ResourceLocation generateNpcOverlay(
        LivingEntity entity,
        ResourceLocation sourceTextureLocation,
        CharacterAppearance appearance
    ) {
        Minecraft minecraft = Minecraft.getInstance();

        try {
            AbstractTexture sourceTexture =
                minecraft.getTextureManager().getTexture(
                    sourceTextureLocation
                );

            sourceTexture.bind();

            try (NativeImage source = new NativeImage(64, 64, false)) {
                source.downloadTexture(0, false);

                NativeImage overlay = new NativeImage(64, 64, true);
                buildRecolouredOverlay(source, overlay, appearance);

                DynamicTexture dynamicTexture =
                    new DynamicTexture(overlay);

                String name =
                    "cyberraces_npc_overlay_"
                        + entity.getUUID().toString().replace("-", "")
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

    private static void buildRecolouredOverlay(
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
                int alpha =
                    source.getLuminanceOrAlpha(x, y) & 0xFF;

                if (alpha < 16) {
                    result.setPixelRGBA(x, y, 0x00000000);
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
                    result.setPixelRGBA(x, y, 0x00000000);
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
        int tolerance,
        String lineage
    ) {
    }

    private record CacheEntry(
        CacheKey key,
        ResourceLocation texture
    ) {
    }
}
