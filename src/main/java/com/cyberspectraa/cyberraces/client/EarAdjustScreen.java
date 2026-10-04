package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class EarAdjustScreen extends Screen {
    private final CharacterCreatorScreen parent;

    private int heightValue;
    private int spreadValue;
    private int tiltValue;

    public EarAdjustScreen(
        CharacterCreatorScreen parent,
        int heightValue,
        int spreadValue,
        int tiltValue
    ) {
        super(Component.literal("Ear Fit"));
        this.parent = parent;
        this.heightValue = clamp(heightValue);
        this.spreadValue = clamp(spreadValue);
        this.tiltValue = clamp(tiltValue);
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(360, this.width - 20);
        int left = (this.width - panelWidth) / 2;
        int top = Math.max(16, this.height / 2 - 94);
        int controlWidth = panelWidth - 40;

        this.addRenderableWidget(new EarSlider(
            left + 20, top + 38, controlWidth, 20, "Height", heightValue,
            value -> {
                heightValue = value;
                apply();
            }
        ));

        this.addRenderableWidget(new EarSlider(
            left + 20, top + 64, controlWidth, 20, "Spread", spreadValue,
            value -> {
                spreadValue = value;
                apply();
            }
        ));

        this.addRenderableWidget(new EarSlider(
            left + 20, top + 90, controlWidth, 20, "Tilt", tiltValue,
            value -> {
                tiltValue = value;
                apply();
            }
        ));

        this.addRenderableWidget(
            Button.builder(Component.literal("Reset"), button -> {
                heightValue = 0;
                spreadValue = 0;
                tiltValue = 0;
                parent.setEarAdjust(0, 0, 0);
                if (this.minecraft != null) {
                    this.minecraft.setScreen(new EarAdjustScreen(parent, 0, 0, 0));
                }
            })
                .bounds(left + 20, top + 122, (controlWidth - 6) / 2, 20)
                .build()
        );

        this.addRenderableWidget(
            Button.builder(Component.literal("Done"), button -> returnToParent())
                .bounds(left + 26 + (controlWidth - 6) / 2, top + 122, (controlWidth - 6) / 2, 20)
                .build()
        );

        apply();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int panelWidth = Math.min(360, this.width - 20);
        int left = (this.width - panelWidth) / 2;
        int top = Math.max(16, this.height / 2 - 94);

        graphics.fill(left, top, left + panelWidth, top + 154, 0xD0101010);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, top + 10, 0xFFFFFF);
        graphics.drawCenteredString(
            this.font,
            "Move the ears until they fit your skin.",
            this.width / 2,
            top + 22,
            0xB8B8B8
        );

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        returnToParent();
    }

    private void apply() {
        parent.setEarAdjust(heightValue, spreadValue, tiltValue);
        parent.pushPreviewState();
    }

    private void returnToParent() {
        apply();
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private static int clamp(int value) {
        return Math.max(CharacterAppearance.EAR_MIN, Math.min(CharacterAppearance.EAR_MAX, value));
    }

    private static double toSlider(int value) {
        return (value - CharacterAppearance.EAR_MIN)
            / (double) (CharacterAppearance.EAR_MAX - CharacterAppearance.EAR_MIN);
    }

    private static int fromSlider(double value) {
        return clamp((int) Math.round(
            CharacterAppearance.EAR_MIN
                + value * (CharacterAppearance.EAR_MAX - CharacterAppearance.EAR_MIN)
        ));
    }

    private static final class EarSlider extends AbstractSliderButton {
        private final String label;
        private final java.util.function.IntConsumer change;

        private EarSlider(
            int x,
            int y,
            int width,
            int height,
            String label,
            int current,
            java.util.function.IntConsumer change
        ) {
            super(x, y, width, height, Component.empty(), Mth.clamp(toSlider(current), 0.0D, 1.0D));
            this.label = label;
            this.change = change;
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            int current = fromSlider(value);
            String prefix = current > 0 ? "+" : "";
            setMessage(Component.literal(label + ": " + prefix + current));
        }

        @Override
        protected void applyValue() {
            change.accept(fromSlider(value));
        }
    }
}
