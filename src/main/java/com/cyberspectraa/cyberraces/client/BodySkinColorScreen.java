package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import com.cyberspectraa.cyberraces.race.Race;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.awt.Color;

public final class BodySkinColorScreen extends Screen {
    private final CharacterCreatorScreen parent;
    private final Race race;

    private int sourceColor;
    private int targetColor;
    private int tolerance;

    private float hue;
    private float saturation;
    private float brightness;

    private int sampledX = -1;
    private int sampledY = -1;

    public BodySkinColorScreen(
        CharacterCreatorScreen parent,
        Race race,
        int sourceColor,
        int targetColor,
        int tolerance
    ) {
        super(Component.literal("Race Skin Colour"));
        this.parent = parent;
        this.race = race;
        this.sourceColor = sourceColor;
        this.targetColor = targetColor == CharacterAppearance.AUTO_COLOR
            ? RaceSkinOverlayManager.defaultTarget(race)
            : targetColor;
        this.tolerance = tolerance;

        setHsbFromRgb(this.targetColor);
    }

    @Override
    protected void init() {
        Layout l = layout();

        this.addRenderableWidget(new ColorSlider(
            l.controlsX, l.top + 66, l.controlsWidth, 18, "Hue", hue,
            value -> {
                hue = (float) value;
                updateTarget();
            }
        ));

        this.addRenderableWidget(new ColorSlider(
            l.controlsX, l.top + 88, l.controlsWidth, 18, "Saturation", saturation,
            value -> {
                saturation = (float) value;
                updateTarget();
            }
        ));

        this.addRenderableWidget(new ColorSlider(
            l.controlsX, l.top + 110, l.controlsWidth, 18, "Brightness", brightness,
            value -> {
                brightness = (float) value;
                updateTarget();
            }
        ));

        this.addRenderableWidget(new ToleranceSlider(
            l.controlsX,
            l.top + 136,
            l.controlsWidth,
            18,
            tolerance,
            value -> {
                tolerance = value;
                apply();
            }
        ));

        int half = (l.controlsWidth - 6) / 2;

        this.addRenderableWidget(
            Button.builder(Component.literal("Disable"), button -> {
                sourceColor = CharacterAppearance.AUTO_COLOR;
                parent.setBodySkin(
                    CharacterAppearance.AUTO_COLOR,
                    CharacterAppearance.AUTO_COLOR,
                    tolerance
                );
                parent.pushPreviewState();
            })
                .bounds(l.controlsX, l.top + 162, half, 18)
                .build()
        );

        this.addRenderableWidget(
            Button.builder(Component.literal("Done"), button -> returnToParent())
                .bounds(l.controlsX + half + 6, l.top + 162, half, 18)
                .build()
        );

        apply();
    }

    @Override
    public void render(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float partialTick
    ) {
        this.renderBackground(graphics);
        Layout l = layout();

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

        renderPlayerPreview(graphics, l, mouseX, mouseY);
        renderSkinAtlas(graphics, l);

        graphics.drawString(
            this.font,
            "1. Click your original skin tone",
            l.controlsX,
            l.top + 26,
            0xE6D39A,
            false
        );

        graphics.drawString(
            this.font,
            sourceColor == CharacterAppearance.AUTO_COLOR
                ? "Source: not selected"
                : String.format("Source: #%06X", sourceColor & 0xFFFFFF),
            l.controlsX,
            l.top + 38,
            sourceColor == CharacterAppearance.AUTO_COLOR ? 0xFF8E8E : 0xD0D0D0,
            false
        );

        graphics.drawString(
            this.font,
            "2. Pick the new racial colour",
            l.controlsX,
            l.top + 50,
            0xE6D39A,
            false
        );

        graphics.fill(
            l.controlsX + l.controlsWidth - 34,
            l.top + 37,
            l.controlsX + l.controlsWidth,
            l.top + 53,
            0xFF000000 | (targetColor & 0xFFFFFF)
        );

        if (sourceColor == CharacterAppearance.AUTO_COLOR) {
            graphics.drawCenteredString(
                this.font,
                "Recolour begins after you sample a skin pixel.",
                l.rightX + l.rightWidth / 2,
                l.bottom - 14,
                0xAAAAAA
            );
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Layout l = layout();

        if (button == 0
            && mouseX >= l.atlasX
            && mouseX < l.atlasX + l.atlasSize
            && mouseY >= l.atlasY
            && mouseY < l.atlasY + l.atlasSize
            && this.minecraft != null
            && this.minecraft.player != null) {

            int skinX = Mth.clamp(
                (int) ((mouseX - l.atlasX) * 64.0D / l.atlasSize),
                0,
                63
            );
            int skinY = Mth.clamp(
                (int) ((mouseY - l.atlasY) * 64.0D / l.atlasSize),
                0,
                63
            );

            int sampled = PlayerSkinSampler.sample(
                this.minecraft.player,
                skinX,
                skinY
            );

            if (sampled >= 0) {
                sourceColor = sampled;
                sampledX = skinX;
                sampledY = skinY;
                apply();
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
        Layout l,
        int mouseX,
        int mouseY
    ) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        apply();

        LocalPlayer player = this.minecraft.player;
        boolean wasInvisible = player.isInvisible();
        player.setInvisible(false);

        int centerX = l.left + (l.previewRight - l.left) / 2;
        int availableWidth = l.previewRight - l.left;
        int availableHeight = l.bottom - l.top;

        int scale = Mth.clamp(
            Math.min(
                (int) (availableWidth * 0.39F),
                (int) (availableHeight * 0.36F)
            ),
            38,
            88
        );

        try {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics,
                centerX,
                l.bottom - 20,
                scale,
                (float) (centerX - mouseX),
                (float) (l.top + availableHeight / 2 - mouseY),
                player
            );
        } finally {
            player.setInvisible(wasInvisible);
        }
    }

    private void renderSkinAtlas(GuiGraphics graphics, Layout l) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        graphics.drawCenteredString(
            this.font,
            "Click skin pixel",
            l.atlasX + l.atlasSize / 2,
            l.atlasY - 10,
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
            PlayerSkinSampler.originalTexture(this.minecraft.player),
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

        if (sampledX >= 0 && sampledY >= 0) {
            int x1 = l.atlasX + (int) Math.floor(sampledX * scale);
            int y1 = l.atlasY + (int) Math.floor(sampledY * scale);
            int x2 = l.atlasX + (int) Math.ceil((sampledX + 1) * scale);
            int y2 = l.atlasY + (int) Math.ceil((sampledY + 1) * scale);

            graphics.fill(x1 - 1, y1 - 1, x2 + 1, y1, 0xFFFFFFFF);
            graphics.fill(x1 - 1, y2, x2 + 1, y2 + 1, 0xFFFFFFFF);
            graphics.fill(x1 - 1, y1, x1, y2, 0xFFFFFFFF);
            graphics.fill(x2, y1, x2 + 1, y2, 0xFFFFFFFF);
        }
    }

    private void updateTarget() {
        targetColor = Color.HSBtoRGB(
            hue,
            saturation,
            brightness
        ) & 0xFFFFFF;
        apply();
    }

    private void setHsbFromRgb(int rgb) {
        float[] hsb = Color.RGBtoHSB(
            (rgb >> 16) & 0xFF,
            (rgb >> 8) & 0xFF,
            rgb & 0xFF,
            null
        );

        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
    }

    private void apply() {
        parent.setBodySkin(sourceColor, targetColor, tolerance);
        parent.pushPreviewState();
    }

    private void returnToParent() {
        apply();
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private Layout layout() {
        boolean small = this.width < 600 || this.height < 340;
        int margin = small ? 4 : 12;
        int panelWidth = Math.min(760, this.width - margin * 2);
        int panelHeight = Math.min(390, this.height - margin * 2);

        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2;
        int bottom = top + panelHeight;

        int previewWidth = Math.max(150, (int) (panelWidth * 0.44F));
        int previewRight = left + previewWidth;
        int rightX = previewRight + 4;
        int rightWidth = left + panelWidth - rightX;

        int atlasSize = Math.min(
            small ? 76 : 108,
            Math.max(52, rightWidth / 3)
        );
        int atlasX = rightX + rightWidth - atlasSize - 8;
        int atlasY = top + 66;

        int controlsX = rightX + 8;
        int controlsRight = atlasX - 8;
        int controlsWidth = Math.max(96, controlsRight - controlsX);

        return new Layout(
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

    private record Layout(
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

    private static final class ColorSlider extends AbstractSliderButton {
        private final String label;
        private final java.util.function.DoubleConsumer change;

        private ColorSlider(
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

    private static final class ToleranceSlider extends AbstractSliderButton {
        private final java.util.function.IntConsumer change;

        private ToleranceSlider(
            int x,
            int y,
            int width,
            int height,
            int current,
            java.util.function.IntConsumer change
        ) {
            super(
                x,
                y,
                width,
                height,
                Component.empty(),
                toSlider(current)
            );
            this.change = change;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(
                "Match range: " + fromSlider(value)
            ));
        }

        @Override
        protected void applyValue() {
            change.accept(fromSlider(value));
        }

        private static double toSlider(int value) {
            return (value - CharacterAppearance.BODY_TOLERANCE_MIN)
                / (double) (
                    CharacterAppearance.BODY_TOLERANCE_MAX
                        - CharacterAppearance.BODY_TOLERANCE_MIN
                );
        }

        private static int fromSlider(double value) {
            return Mth.clamp(
                (int) Math.round(
                    CharacterAppearance.BODY_TOLERANCE_MIN
                        + value * (
                            CharacterAppearance.BODY_TOLERANCE_MAX
                                - CharacterAppearance.BODY_TOLERANCE_MIN
                        )
                ),
                CharacterAppearance.BODY_TOLERANCE_MIN,
                CharacterAppearance.BODY_TOLERANCE_MAX
            );
        }
    }
}
