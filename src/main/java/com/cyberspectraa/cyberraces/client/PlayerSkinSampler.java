package com.cyberspectraa.cyberraces.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

public final class PlayerSkinSampler {
    private PlayerSkinSampler() {
    }

    public static ResourceLocation originalTexture(
        AbstractClientPlayer player
    ) {
        ResourceLocation remembered =
            RaceSkinOverlayManager.originalSkin(player);

        if (remembered != null) {
            return remembered;
        }

        // This call also lets the CyberRaces skin mixin remember the
        // vanilla skin location before returning any racial replacement.
        ResourceLocation rendered = player.getSkinTextureLocation();
        remembered = RaceSkinOverlayManager.originalSkin(player);

        return remembered != null ? remembered : rendered;
    }

    public static int sample(
        AbstractClientPlayer player,
        int skinX,
        int skinY
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || player == null) {
            return -1;
        }

        try {
            AbstractTexture texture =
                minecraft.getTextureManager().getTexture(
                    originalTexture(player)
                );

            texture.bind();

            try (NativeImage image =
                     new NativeImage(64, 64, false)) {
                image.downloadTexture(0, false);

                int x = Math.max(0, Math.min(63, skinX));
                int y = Math.max(0, Math.min(63, skinY));

                int red =
                    image.getRedOrLuminance(x, y) & 0xFF;
                int green =
                    image.getGreenOrLuminance(x, y) & 0xFF;
                int blue =
                    image.getBlueOrLuminance(x, y) & 0xFF;
                int alpha =
                    image.getLuminanceOrAlpha(x, y) & 0xFF;

                if (alpha < 16) {
                    return -1;
                }

                return (red << 16)
                    | (green << 8)
                    | blue;
            }
        } catch (RuntimeException exception) {
            return -1;
        }
    }
}
