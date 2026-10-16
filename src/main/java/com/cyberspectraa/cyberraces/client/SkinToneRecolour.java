package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.ArrayDeque;

/**
 * Recolour exposed skin, never the outer clothing/hat layers.
 *
 * Earlier versions changed every pixel with a similar RGB value across
 * the whole atlas. A shirt or trousers sharing a skin shade could turn
 * into racial skin. Instead, accept connected colour regions which start
 * from expected exposed skin (face, wrists, collar, ankles). The original
 * skin's shading is preserved even for darker/lighter neighbouring pixels.
 */
public final class SkinToneRecolour {
    private record Island(int x0, int y0, int x1, int y1,
                          int seedX0, int seedY0, int seedX1, int seedY1,
                          int minSeeds) {}

    private static final Island[] BASE_SKIN = {
        // Head: front lower face. Top/hair is never a seed.
        new Island(0, 0, 32, 16, 10, 12, 15, 16, 2),
        // Torso: neckline. Skip shirt entirely when the neckline isn't skin.
        new Island(16, 16, 40, 32, 21, 20, 27, 23, 4),
        // Right arm: exposed wrist/hand, from outer arm base.
        new Island(40, 16, 56, 32, 44, 27, 48, 32, 3),
        // Right leg: ankle, if not covered by boots/pants.
        new Island(0, 16, 16, 32, 4, 28, 8, 32, 3),
        // Left leg: ankle.
        new Island(16, 48, 32, 64, 20, 60, 24, 64, 3),
        // Left arm: exposed wrist/hand.
        new Island(32, 48, 48, 64, 36, 59, 40, 64, 3)
    };

    private SkinToneRecolour() {}

    public static void apply(NativeImage source, NativeImage result,
            CharacterAppearance appearance, boolean overlay) {
        int sampled = appearance.bodySourceColor();
        int target = appearance.bodyTargetColor();
        boolean[][] accepted = new boolean[64][64];
        boolean[][] candidates = new boolean[64][64];
        int tolerance = appearance.bodyTolerance();

        for (Island island : BASE_SKIN) {
            for (int y = island.y0; y < island.y1; y++) {
                for (int x = island.x0; x < island.x1; x++) {
                    candidates[x][y] = isSkinShade(source, x, y, sampled, tolerance);
                }
            }
            ArrayDeque<Integer> work = new ArrayDeque<>();
            int seeds = 0;
            for (int y = island.seedY0; y < island.seedY1; y++) {
                for (int x = island.seedX0; x < island.seedX1; x++) {
                    if (candidates[x][y]) {
                        work.add(y * 64 + x);
                        seeds++;
                    }
                }
            }
            if (seeds < island.minSeeds) continue;

            while (!work.isEmpty()) {
                int pos = work.removeFirst();
                int x = pos % 64, y = pos / 64;
                if (x < island.x0 || x >= island.x1
                        || y < island.y0 || y >= island.y1
                        || !candidates[x][y] || accepted[x][y]) continue;
                accepted[x][y] = true;
                if (x > island.x0) work.add(pos - 1);
                if (x + 1 < island.x1) work.add(pos + 1);
                if (y > island.y0) work.add(pos - 64);
                if (y + 1 < island.y1) work.add(pos + 64);
            }
        }

        double sourceBrightness = Math.max(12.0, brightness(
                (sampled >> 16) & 255, (sampled >> 8) & 255, sampled & 255));
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                int original = source.getPixelRGBA(x, y);
                if (!accepted[x][y]) {
                    result.setPixelRGBA(x, y, overlay ? 0 : original);
                    continue;
                }
                int alpha = source.getLuminanceOrAlpha(x, y) & 255;
                int r = source.getRedOrLuminance(x, y) & 255;
                int g = source.getGreenOrLuminance(x, y) & 255;
                int b = source.getBlueOrLuminance(x, y) & 255;
                double shade = Math.max(0.35, Math.min(1.7,
                        brightness(r, g, b) / sourceBrightness));
                int rr = clamp((int) Math.round(((target >> 16) & 255) * shade));
                int gg = clamp((int) Math.round(((target >> 8) & 255) * shade));
                int bb = clamp((int) Math.round((target & 255) * shade));
                result.setPixelRGBA(x, y, (alpha << 24)
                        | (bb << 16) | (gg << 8) | rr);
            }
        }
    }

    private static boolean isSkinShade(NativeImage source, int x, int y,
            int reference, int tolerance) {
        if ((source.getLuminanceOrAlpha(x, y) & 255) < 200) return false;

        double r = source.getRedOrLuminance(x, y) & 255;
        double g = source.getGreenOrLuminance(x, y) & 255;
        double b = source.getBlueOrLuminance(x, y) & 255;
        double rr = (reference >> 16) & 255;
        double gg = (reference >> 8) & 255;
        double bb = reference & 255;
        double light = brightness((int) r, (int) g, (int) b);
        double base = brightness((int) rr, (int) gg, (int) bb);
        if (light < 18 || base < 18 || light / base < 0.42
                || light / base > 1.67) return false;

        // Compare colour character independently from light/dark shading:
        // this includes shaded skin without grabbing similarly bright cloth.
        double normal = 140.0 / light;
        double sourceNormal = 140.0 / base;
        double dr = r * normal - rr * sourceNormal;
        double dg = g * normal - gg * sourceNormal;
        double db = b * normal - bb * sourceNormal;
        double colourDifference = Math.sqrt(dr * dr + dg * dg + db * db);
        double limit = 16.0 + tolerance * 1.15;
        return colourDifference <= limit;
    }

    private static double brightness(int r, int g, int b) {
        return r * 0.2126 + g * 0.7152 + b * 0.0722;
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
}
