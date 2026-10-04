package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class EarAdjustScreen extends Screen {
    private final CharacterCreatorScreen parent;

    private int heightValue;
    private int spreadValue;
    private int tiltValue;

    private EarSlider heightSlider;
    private EarSlider spreadSlider;
    private EarSlider tiltSlider;

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
        Layout l = layout();

        this.heightSlider = this.addRenderableWidget(new EarSlider(
            l.controlsX, l.top + 52, l.controlsWidth, 20, "Height", heightValue,
            value -> {
                heightValue = value;
                apply();
            }
        ));

        this.spreadSlider = this.addRenderableWidget(new EarSlider(
            l.controlsX, l.top + 78, l.controlsWidth, 20, "Spread", spreadValue,
            value -> {
                spreadValue = value;
                apply();
            }
        ));

        this.tiltSlider = this.addRenderableWidget(new EarSlider(
            l.controlsX, l.top + 104, l.controlsWidth, 20, "Tilt", tiltValue,
            value -> {
                tiltValue = value;
                apply();
            }
        ));

        int half = (l.controlsWidth - 6) / 2;

        this.addRenderableWidget(
            Button.builder(Component.literal("Reset"), button -> reset())
                .bounds(l.controlsX, l.top + 136, half, 20)
                .build()
        );

        this.addRenderableWidget(
            Button.builder(Component.literal("Done"), button -> returnToParent())
                .bounds(l.controlsX + half + 6, l.top + 136, half, 20)
                .build()
        );

        apply();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        Layout l = layout();

        graphics.fill(l.left, l.top, l.previewRight, l.bottom, 0xD0101010);
        graphics.fill(l.rightX, l.top, l.rightX + l.rightWidth, l.bottom, 0xD0101010);

        graphics.drawCenteredString(
            this.font,
            "Live Ear Preview",
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

        graphics.drawCenteredString(
            this.font,
            "Drag the sliders and watch the ears move.",
            l.rightX + l.rightWidth / 2,
            l.top + 26,
            0xB8B8B8
        );

        renderPlayerPreview(graphics, l, mouseX, mouseY);

        super.render(graphics, mouseX, mouseY, partialTick);
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
            Math.min((int) (availableWidth * 0.39F), (int) (availableHeight * 0.38F)),
            42,
            92
        );

        try {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics,
                centerX,
                l.bottom - 22,
                scale,
                (float) (centerX - mouseX),
                (float) (l.top + availableHeight / 2 - mouseY),
                player
            );
        } finally {
            player.setInvisible(wasInvisible);
        }
    }

    private void apply() {
        parent.setEarAdjust(heightValue, spreadValue, tiltValue);
        parent.pushPreviewState();
    }

    private void reset() {
        heightValue = 0;
        spreadValue = 0;
        tiltValue = 0;

        if (heightSlider != null) {
            heightSlider.setCurrent(0);
        }
        if (spreadSlider != null) {
            spreadSlider.setCurrent(0);
        }
        if (tiltSlider != null) {
            tiltSlider.setCurrent(0);
        }

        apply();
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
        int panelWidth = Math.min(650, this.width - margin * 2);
        int panelHeight = Math.min(340, this.height - margin * 2);

        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2;
        int bottom = top + panelHeight;

        int previewWidth = Math.max(145, (int) (panelWidth * 0.50F));
        int previewRight = left + previewWidth;
        int rightX = previewRight + 4;
        int rightWidth = left + panelWidth - rightX;

        int controlsX = rightX + 12;
        int controlsWidth = Math.max(90, rightWidth - 24);

        return new Layout(
            left,
            top,
            bottom,
            previewRight,
            rightX,
            rightWidth,
            controlsX,
            controlsWidth
        );
    }

    private static int clamp(int value) {
        return Math.max(
            CharacterAppearance.EAR_MIN,
            Math.min(CharacterAppearance.EAR_MAX, value)
        );
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

    private record Layout(
        int left,
        int top,
        int bottom,
        int previewRight,
        int rightX,
        int rightWidth,
        int controlsX,
        int controlsWidth
    ) {
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

        private void setCurrent(int current) {
            this.value = Mth.clamp(toSlider(current), 0.0D, 1.0D);
            updateMessage();
        }
    }
}
