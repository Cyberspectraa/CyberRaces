package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import com.cyberspectraa.cyberraces.race.Race;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.nio.ByteBuffer;

public final class FeatureColorPickerScreen extends Screen {
    private final CharacterCreatorScreen parent;
    private final Race race;

    private float hue;
    private float saturation;
    private float brightness;
    private boolean automatic;

    private Button automaticButton;

    public FeatureColorPickerScreen(CharacterCreatorScreen parent, Race race, int currentRgb) {
        super(Component.literal("Feature Colour"));
        this.parent = parent;
        this.race = race;
        this.automatic = currentRgb == CharacterAppearance.AUTO_COLOR;

        int startRgb = automatic ? FeatureColourPalette.defaultRgb(race) : currentRgb;
        setHsbFromRgb(startRgb);
    }

    @Override
    protected void init() {
        PickerLayout l = layout();

        this.addRenderableWidget(new ColourSlider(
            l.controlsX, l.top + 46, l.controlsWidth, 18, "Hue", hue,
            value -> {
                hue = (float) value;
                useCustom();
            }
        ));

        this.addRenderableWidget(new ColourSlider(
            l.controlsX, l.top + 68, l.controlsWidth, 18, "Saturation", saturation,
            value -> {
                saturation = (float) value;
                useCustom();
            }
        ));

        this.addRenderableWidget(new ColourSlider(
            l.controlsX, l.top + 90, l.controlsWidth, 18, "Brightness", brightness,
            value -> {
                brightness = (float) value;
                useCustom();
            }
        ));

        this.automaticButton = this.addRenderableWidget(
            Button.builder(Component.empty(), button -> {
                automatic = true;
                parent.setFeatureColor(CharacterAppearance.AUTO_COLOR);
                updateAutomaticLabel();
                parent.pushPreviewState();
            })
                .bounds(l.controlsX, l.top + 114, l.controlsWidth, 18)
                .build()
        );

        this.addRenderableWidget(
            Button.builder(Component.literal("Done"), button -> returnToParent())
                .bounds(l.rightX + l.rightWidth / 2 - 50, l.bottom - 24, 100, 18)
                .build()
        );

        updateAutomaticLabel();
        parent.pushPreviewState();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        PickerLayout l = layout();

        graphics.fill(l.left, l.top, l.previewRight, l.bottom, 0xD0101010);
        graphics.fill(l.rightX, l.top, l.rightX + l.rightWidth, l.bottom, 0xD0101010);

        graphics.drawCenteredString(
            this.font,
            "Live Preview",
            l.left + (l.previewRight - l.left) / 2,
            l.top + 10,
            0xE6D39A
        );

        graphics.drawCenteredString(
            this.font,
            this.title,
            l.rightX + l.rightWidth / 2,
            l.top + 10,
            0xFFFFFF
        );

        int rgb = automatic
            ? FeatureColourPalette.defaultRgb(race)
            : resolvedRgb();

        graphics.fill(
            l.controlsX,
            l.top + 28,
            l.controlsX + l.controlsWidth,
            l.top + 40,
            0xFF000000 | rgb
        );

        renderPlayerPreview(graphics, l, mouseX, mouseY);

        if (FeatureColourPalette.hasEars(race)) {
            renderSkinSampler(graphics, l);
        }

        graphics.drawCenteredString(
            this.font,
            automatic ? automaticLabel() : String.format("#%06X", rgb),
            l.rightX + l.rightWidth / 2,
            l.bottom - 36,
            0xD0D0D0
        );

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        PickerLayout l = layout();

        if (button == 0
            && FeatureColourPalette.hasEars(race)
            && mouseX >= l.atlasX
            && mouseX < l.atlasX + l.atlasSize
            && mouseY >= l.atlasY
            && mouseY < l.atlasY + l.atlasSize) {
            int sampled = sampleFramebufferPixel(mouseX, mouseY);
            if (sampled >= 0) {
                automatic = false;
                setHsbFromRgb(sampled);
                parent.setFeatureColor(sampled);
                parent.pushPreviewState();

                // Re-open this screen so all three sliders jump to the sampled colour.
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new FeatureColorPickerScreen(parent, race, sampled));
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        returnToParent();
    }

    private void renderPlayerPreview(
        GuiGraphics graphics,
        PickerLayout l,
        int mouseX,
        int mouseY
    ) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        parent.pushPreviewState();

        LocalPlayer player = this.minecraft.player;
        boolean wasInvisible = player.isInvisible();
        player.setInvisible(false);

        int previewCenterX = l.left + (l.previewRight - l.left) / 2;
        int previewBottom = l.bottom - 24;
        int availableWidth = l.previewRight - l.left;
        int availableHeight = l.bottom - l.top;

        int scale = Mth.clamp(
            Math.min((int) (availableWidth * 0.34F), (int) (availableHeight * 0.33F)),
            34,
            78
        );

        try {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics,
                previewCenterX,
                previewBottom,
                scale,
                (float) (previewCenterX - mouseX),
                (float) (l.top + availableHeight / 2 - mouseY),
                player
            );
        } finally {
            player.setInvisible(wasInvisible);
        }
    }

    private void renderSkinSampler(GuiGraphics graphics, PickerLayout l) {
        if (this.minecraft == null || this.minecraft.player == null || l.atlasSize <= 0) {
            return;
        }

        graphics.drawCenteredString(
            this.font,
            "Click your skin colour",
            l.atlasX + l.atlasSize / 2,
            l.atlasY - 11,
            0xE6D39A
        );

        graphics.fill(
            l.atlasX - 2,
            l.atlasY - 2,
            l.atlasX + l.atlasSize + 2,
            l.atlasY + l.atlasSize + 2,
            0xFF6A563E
        );

        float scale = l.atlasSize / 64.0F;
        graphics.pose().pushPose();
        graphics.pose().translate(l.atlasX, l.atlasY, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.blit(
            this.minecraft.player.getSkinTextureLocation(),
            0,
            0,
            0.0F,
            0.0F,
            64,
            64,
            64,
            64
        );
        graphics.pose().popPose();
    }

    private int sampleFramebufferPixel(double guiX, double guiY) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return -1;
        }

        double scale = minecraft.getWindow().getGuiScale();
        int framebufferX = (int) Math.floor(guiX * scale);
        int framebufferY = minecraft.getWindow().getHeight()
            - 1
            - (int) Math.floor(guiY * scale);

        ByteBuffer pixel = BufferUtils.createByteBuffer(4);
        GL11.glReadPixels(
            framebufferX,
            framebufferY,
            1,
            1,
            GL11.GL_RGBA,
            GL11.GL_UNSIGNED_BYTE,
            pixel
        );

        int red = pixel.get(0) & 0xFF;
        int green = pixel.get(1) & 0xFF;
        int blue = pixel.get(2) & 0xFF;
        int alpha = pixel.get(3) & 0xFF;

        if (alpha < 16) {
            return -1;
        }

        return (red << 16) | (green << 8) | blue;
    }

    private void useCustom() {
        automatic = false;
        parent.setFeatureColor(resolvedRgb());
        updateAutomaticLabel();
        parent.pushPreviewState();
    }

    private int resolvedRgb() {
        return Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF;
    }

    private void setHsbFromRgb(int rgb) {
        float[] hsb = Color.RGBtoHSB(
            (rgb >> 16) & 0xFF,
            (rgb >> 8) & 0xFF,
            rgb & 0xFF,
            null
        );

        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
    }

    private String automaticLabel() {
        return FeatureColourPalette.hasEars(race)
            ? "Use Player Skin"
            : "Use Natural Colour";
    }

    private void updateAutomaticLabel() {
        if (automaticButton != null) {
            automaticButton.setMessage(Component.literal(
                automatic ? automaticLabel() + " ✓" : automaticLabel()
            ));
        }
    }

    private void returnToParent() {
        if (!automatic) {
            parent.setFeatureColor(resolvedRgb());
        }

        parent.pushPreviewState();

        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private PickerLayout layout() {
        boolean small = this.width < 600 || this.height < 340;
        int margin = small ? 4 : 12;
        int panelWidth = Math.min(720, this.width - margin * 2);
        int panelHeight = Math.min(390, this.height - margin * 2);

        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2;
        int bottom = top + panelHeight;

        int previewWidth = Math.max(135, (int) (panelWidth * (small ? 0.42F : 0.44F)));
        int previewRight = left + previewWidth;
        int rightX = previewRight + 4;
        int rightWidth = left + panelWidth - rightX;

        int atlasSize = FeatureColourPalette.hasEars(race)
            ? Math.min(small ? 80 : 112, Math.max(48, rightWidth / 3))
            : 0;

        int atlasX = rightX + rightWidth - atlasSize - 8;
        int atlasY = top + 58;

        int controlsX = rightX + 8;
        int controlsRight = FeatureColourPalette.hasEars(race)
            ? atlasX - 8
            : rightX + rightWidth - 8;
        int controlsWidth = Math.max(86, controlsRight - controlsX);

        return new PickerLayout(
            left,
            top,
            bottom,
            previewRight,
            rightX,
            rightWidth,
            controlsX,
            controlsWidth,
            atlasX,
            atlasY,
            atlasSize
        );
    }

    private record PickerLayout(
        int left,
        int top,
        int bottom,
        int previewRight,
        int rightX,
        int rightWidth,
        int controlsX,
        int controlsWidth,
        int atlasX,
        int atlasY,
        int atlasSize
    ) {
    }

    private static final class ColourSlider extends AbstractSliderButton {
        private final String label;
        private final java.util.function.DoubleConsumer change;

        private ColourSlider(
            int x,
            int y,
            int width,
            int height,
            String label,
            double value,
            java.util.function.DoubleConsumer change
        ) {
            super(x, y, width, height, Component.empty(), Mth.clamp(value, 0.0D, 1.0D));
            this.label = label;
            this.change = change;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(
                label + ": " + Math.round(value * 100.0D) + "%"
            ));
        }

        @Override
        protected void applyValue() {
            change.accept(value);
        }
    }
}
