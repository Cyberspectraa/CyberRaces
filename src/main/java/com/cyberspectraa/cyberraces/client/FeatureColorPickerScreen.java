package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import com.cyberspectraa.cyberraces.race.Race;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.awt.Color;

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
        float[] hsb = Color.RGBtoHSB(
            (startRgb >> 16) & 0xFF,
            (startRgb >> 8) & 0xFF,
            startRgb & 0xFF,
            null
        );
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(360, this.width - 20);
        int left = (this.width - panelWidth) / 2;
        int top = Math.max(16, this.height / 2 - 100);
        int controlWidth = panelWidth - 40;

        this.addRenderableWidget(new ColourSlider(
            left + 20, top + 42, controlWidth, 20, "Hue", hue,
            value -> {
                hue = (float) value;
                useCustom();
            }
        ));

        this.addRenderableWidget(new ColourSlider(
            left + 20, top + 68, controlWidth, 20, "Saturation", saturation,
            value -> {
                saturation = (float) value;
                useCustom();
            }
        ));

        this.addRenderableWidget(new ColourSlider(
            left + 20, top + 94, controlWidth, 20, "Brightness", brightness,
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
            })
                .bounds(left + 20, top + 122, controlWidth, 20)
                .build()
        );

        this.addRenderableWidget(
            Button.builder(Component.literal("Done"), button -> returnToParent())
                .bounds(this.width / 2 - 60, top + 154, 120, 20)
                .build()
        );

        updateAutomaticLabel();
        pushPreview();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int panelWidth = Math.min(360, this.width - 20);
        int left = (this.width - panelWidth) / 2;
        int top = Math.max(16, this.height / 2 - 100);

        graphics.fill(left, top, left + panelWidth, top + 184, 0xD0101010);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, top + 10, 0xFFFFFF);

        int rgb = resolvedRgb();
        int swatch = 0xFF000000 | rgb;
        graphics.fill(left + 20, top + 24, left + panelWidth - 20, top + 36, swatch);
        graphics.drawCenteredString(
            this.font,
            automatic ? automaticLabel() : String.format("#%06X", rgb),
            this.width / 2,
            top + 178,
            0xD0D0D0
        );

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        returnToParent();
    }

    private void useCustom() {
        automatic = false;
        parent.setFeatureColor(resolvedRgb());
        updateAutomaticLabel();
        pushPreview();
    }

    private int resolvedRgb() {
        return Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF;
    }

    private String automaticLabel() {
        return FeatureColourPalette.hasEars(race) ? "Use Player Skin" : "Use Natural Colour";
    }

    private void updateAutomaticLabel() {
        if (automaticButton != null) {
            automaticButton.setMessage(Component.literal(
                automatic ? automaticLabel() + " ✓" : automaticLabel()
            ));
        }
    }

    private void pushPreview() {
        parent.pushPreviewState();
    }

    private void returnToParent() {
        if (!automatic) {
            parent.setFeatureColor(resolvedRgb());
        }
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
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
            setMessage(Component.literal(label + ": " + Math.round(value * 100.0D) + "%"));
        }

        @Override
        protected void applyValue() {
            change.accept(value);
        }
    }
}
