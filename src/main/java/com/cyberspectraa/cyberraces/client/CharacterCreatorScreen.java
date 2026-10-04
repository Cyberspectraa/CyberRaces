package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.SubmitCharacterPacket;
import com.cyberspectraa.cyberraces.race.Race;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.List;

public final class CharacterCreatorScreen extends Screen {
    private static final ResourceLocation BACKGROUND =
        new ResourceLocation("minecraft", "textures/gui/options_background.png");

    private int raceIndex;
    private int featureStyle;

    private Button raceButton;
    private Button featureButton;

    public CharacterCreatorScreen() {
        super(Component.literal("Create Your Character"));
    }

    @Override
    protected void init() {
        Layout layout = layout();

        int selectorY = layout.top + layout.selectorY;
        int arrowWidth = layout.compact ? 20 : 24;
        int padding = layout.compact ? 8 : 14;
        int buttonHeight = layout.compact ? 18 : 20;

        this.addRenderableWidget(
            Button.builder(Component.literal("<"), button -> changeRace(-1))
                .bounds(layout.left + padding, selectorY, arrowWidth, buttonHeight)
                .build()
        );

        int raceButtonX = layout.left + padding + arrowWidth + 4;
        int raceButtonWidth = Math.max(
            54,
            layout.leftWidth - (padding * 2) - (arrowWidth * 2) - 8
        );

        this.raceButton = this.addRenderableWidget(
            Button.builder(Component.literal(currentRace().displayName()), button -> changeRace(1))
                .bounds(raceButtonX, selectorY, raceButtonWidth, buttonHeight)
                .build()
        );

        this.addRenderableWidget(
            Button.builder(Component.literal(">"), button -> changeRace(1))
                .bounds(raceButtonX + raceButtonWidth + 4, selectorY, arrowWidth, buttonHeight)
                .build()
        );

        int featurePadding = layout.compact ? 8 : 14;
        this.featureButton = this.addRenderableWidget(
            Button.builder(Component.empty(), button -> {
                featureStyle = (featureStyle + 1) % CharacterAppearance.FEATURE_STYLE_COUNT;
                refreshLabels();
            })
                .bounds(
                    layout.right + featurePadding,
                    layout.top + layout.featureButtonY,
                    Math.max(72, layout.rightWidth - featurePadding * 2),
                    buttonHeight
                )
                .build()
        );

        int confirmWidth = Math.min(160, Math.max(118, layout.width - 40));
        this.addRenderableWidget(
            Button.builder(Component.literal("Confirm Character"), button -> submit())
                .bounds(
                    layout.centerX - confirmWidth / 2,
                    layout.top + layout.height - layout.confirmBottomOffset,
                    confirmWidth,
                    buttonHeight
                )
                .build()
        );

        refreshLabels();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        tileBackground(graphics);

        Layout layout = layout();
        int bottom = layout.top + layout.height;

        graphics.fill(layout.left, layout.top, layout.left + layout.leftWidth, bottom, 0xC0101010);
        graphics.fill(layout.middle, layout.top, layout.middle + layout.middleWidth, bottom, 0xC0101010);
        graphics.fill(layout.right, layout.top, layout.right + layout.rightWidth, bottom, 0xC0101010);

        graphics.drawCenteredString(
            this.font,
            this.title,
            layout.centerX,
            layout.top + (layout.compact ? 7 : 12),
            0xFFFFFF
        );

        if (!layout.compact) {
            graphics.drawCenteredString(
                this.font,
                "Your Minecraft skin stays intact - CyberRaces adds the racial features.",
                layout.centerX,
                layout.top + 27,
                0xB8B8B8
            );
        }

        int sectionY = layout.top + layout.sectionTitleY;
        int sidePadding = layout.compact ? 8 : 14;

        graphics.drawString(this.font, "Race", layout.left + sidePadding, sectionY, 0xE6D39A, false);
        renderRaceInfo(graphics, layout);

        graphics.drawCenteredString(
            this.font,
            "Preview",
            layout.middle + layout.middleWidth / 2,
            sectionY,
            0xE6D39A
        );

        int previewTop = layout.top + layout.previewTop;
        int previewBottom = bottom - layout.previewBottom;
        if (previewBottom > previewTop + 20) {
            graphics.fill(
                layout.middle + (layout.compact ? 6 : 16),
                previewTop,
                layout.middle + layout.middleWidth - (layout.compact ? 6 : 16),
                previewBottom,
                0x70202020
            );
        }

        renderPlayerPreview(graphics, layout, mouseX, mouseY);

        graphics.drawString(
            this.font,
            layout.compact ? "Features" : "Race Features",
            layout.right + sidePadding,
            sectionY,
            0xE6D39A,
            false
        );

        int featureTextY = layout.top + layout.featureTextY;
        drawWrapped(
            graphics,
            featureDescription(),
            layout.right + sidePadding,
            featureTextY,
            Math.max(60, layout.rightWidth - sidePadding * 2),
            0xD0D0D0,
            layout.compact ? 3 : 4
        );

        if (!layout.compact) {
            drawWrapped(
                graphics,
                "Face, eyes, hair and clothing come from your normal skin.",
                layout.right + sidePadding,
                layout.top + 142,
                Math.max(60, layout.rightWidth - sidePadding * 2),
                0x9E9E9E,
                3
            );
        }

        if (!layout.compact) {
            graphics.drawCenteredString(
                this.font,
                "Confirming releases you into the world and allows CyberServer to play your arrival.",
                layout.centerX,
                bottom - 13,
                0xAFAFAF
            );
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
    }

    @Override
    public void removed() {
        ClientCharacterState.clearPreview();
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void renderRaceInfo(GuiGraphics graphics, Layout layout) {
        Race race = currentRace();
        int x = layout.left + (layout.compact ? 8 : 14);
        int y = layout.top + layout.statsY;
        int valueX = layout.left + layout.leftWidth - (layout.compact ? 42 : 52);
        int line = layout.compact ? 12 : 16;

        drawStat(graphics, layout.compact ? "HP" : "Health", String.valueOf((int) race.maxHealth()), x, valueX, y);
        drawStat(graphics, "Size", String.format("%.2fx", race.scale()), x, valueX, y + line);
        drawStat(graphics, "Speed", String.format("%.0f%%", race.movementMultiplier() * 100.0D), x, valueX, y + line * 2);
        drawStat(graphics, "Mana", String.format("%.0f%%", race.maxManaMultiplier() * 100.0D), x, valueX, y + line * 3);

        if (!layout.compact || layout.height >= 300) {
            drawStat(
                graphics,
                layout.compact ? "Regen" : "Mana regen",
                String.format("%.0f%%", race.manaRegenMultiplier() * 100.0D),
                x,
                valueX,
                y + line * 4
            );
        }

        int noteY = y + line * 5 + (layout.compact ? 6 : 14);
        int maxLines = layout.compact ? 2 : 4;
        int lineCount = 0;

        for (String summary : raceSummary(race)) {
            if (lineCount >= maxLines) {
                break;
            }

            List<FormattedCharSequence> wrapped = this.font.split(
                Component.literal(summary),
                Math.max(54, layout.leftWidth - (layout.compact ? 16 : 28))
            );

            for (FormattedCharSequence text : wrapped) {
                if (lineCount >= maxLines) {
                    break;
                }
                graphics.drawString(this.font, text, x, noteY + lineCount * 10, 0xD0D0D0, false);
                lineCount++;
            }
        }
    }

    private void drawStat(GuiGraphics graphics, String label, String value, int x, int valueX, int y) {
        graphics.drawString(this.font, label, x, y, 0x9E9E9E, false);
        graphics.drawString(this.font, value, valueX, y, 0xFFFFFF, false);
    }

    private void renderPlayerPreview(GuiGraphics graphics, Layout layout, int mouseX, int mouseY) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        ClientCharacterState.setPreview(currentRace(), featureStyle);

        boolean wasInvisible = this.minecraft.player.isInvisible();
        this.minecraft.player.setInvisible(false);

        int previewX = layout.middle + layout.middleWidth / 2;
        int previewY = layout.top + layout.height - layout.playerBottomOffset;

        int availableHeight = Math.max(80, layout.height - layout.previewTop - layout.previewBottom - 4);
        int sizeFromHeight = (int) (availableHeight * (layout.compact ? 0.42D : 0.48D));
        int sizeFromWidth = (int) (layout.middleWidth * (layout.compact ? 0.28D : 0.34D));

        int basePreviewScale = Math.max(34, Math.min(sizeFromHeight, sizeFromWidth));
        int previewScale = Mth.clamp(
            (int) (basePreviewScale * Math.sqrt(Math.max(0.5D, currentRace().scale()))),
            layout.compact ? 32 : 48,
            layout.compact ? 68 : 90
        );

        try {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics,
                previewX,
                previewY,
                previewScale,
                (float) (previewX - mouseX),
                (float) (layout.top + layout.height / 2 - mouseY),
                this.minecraft.player
            );
        } finally {
            this.minecraft.player.setInvisible(wasInvisible);
        }
    }

    private void drawWrapped(
        GuiGraphics graphics,
        String text,
        int x,
        int y,
        int maxWidth,
        int color,
        int maxLines
    ) {
        List<FormattedCharSequence> lines = this.font.split(Component.literal(text), maxWidth);
        int count = Math.min(lines.size(), maxLines);

        for (int i = 0; i < count; i++) {
            graphics.drawString(this.font, lines.get(i), x, y + i * 11, color, false);
        }
    }

    private void changeRace(int direction) {
        Race[] races = Race.values();
        raceIndex = Math.floorMod(raceIndex + direction, races.length);
        featureStyle = 0;
        refreshLabels();
    }

    private void refreshLabels() {
        Race race = currentRace();
        ClientCharacterState.setPreview(race, featureStyle);

        if (raceButton != null) {
            raceButton.setMessage(Component.literal(race.displayName()));
        }

        if (featureButton != null) {
            featureButton.setMessage(Component.literal(featureLabel()));
            featureButton.active = hasFeatureVariants(race);
        }
    }

    private boolean hasFeatureVariants(Race race) {
        return race != Race.HUMAN && race != Race.DWARF;
    }

    private String featureLabel() {
        String[] labels = switch (currentRace()) {
            case ELF -> new String[] {"Ears: Short", "Ears: Long", "Ears: High"};
            case HALFLING -> new String[] {"Ears: Round", "Ears: Soft", "Ears: Pointed"};
            case ORC -> new String[] {"Tusks: Small", "Tusks: Broad", "Tusks: Long"};
            case GOBLIN -> new String[] {"Ears: Wide", "Ears: Long", "Ears: Swept"};
            case TIEFLING -> new String[] {"Horns: Curved", "Horns: Swept", "Horns: Tall"};
            case DRAGONBORN -> new String[] {"Crest: Horned", "Crest: Crowned", "Crest: Swept"};
            case FAIRY -> new String[] {"Ears: Classic", "Ears: Sharp", "Ears: Soft"};
            case HUMAN -> new String[] {"No racial feature", "No racial feature", "No racial feature"};
            case DWARF -> new String[] {"Stout silhouette", "Stout silhouette", "Stout silhouette"};
        };
        return labels[featureStyle];
    }

    private String featureDescription() {
        return switch (currentRace()) {
            case HUMAN -> "Your normal skin and proportions define your appearance.";
            case ELF -> "Pointed ears follow the animated head.";
            case DWARF -> "Your shorter, sturdy silhouette is the main racial look.";
            case HALFLING -> "Subtle ears keep Halflings distinct from Dwarves.";
            case ORC -> "Pointed ears and visible lower tusks.";
            case GOBLIN -> "Large ears create the Goblin silhouette.";
            case TIEFLING -> "Horns plus a body-attached animated tail.";
            case DRAGONBORN -> "Dragon snout, crest and a heavy tail.";
            case FAIRY -> "Fey ears plus permanent Zanza's Wings.";
        };
    }

    private String[] raceSummary(Race race) {
        return switch (race) {
            case HUMAN -> new String[] {"Balanced and adaptable.", "No forced racial geometry."};
            case ELF -> new String[] {"Fast and naturally magical.", "Lower base health."};
            case DWARF -> new String[] {"Tough and resistant.", "Shorter and slightly slower."};
            case HALFLING -> new String[] {"Small and nimble.", "Low profile, lighter frame."};
            case ORC -> new String[] {"High health and strength.", "Lower natural mana."};
            case GOBLIN -> new String[] {"Small and very quick.", "Fragile compared with Orcs."};
            case TIEFLING -> new String[] {"Strong magical potential.", "Natural fire resistance."};
            case DRAGONBORN -> new String[] {"Armoured and resilient.", "Large physical presence."};
            case FAIRY -> new String[] {"Tiny, fast and magical.", "Very fragile; natural flight."};
        };
    }

    private Race currentRace() {
        return Race.values()[raceIndex];
    }

    private void submit() {
        CyberRacesNetwork.sendToServer(
            new SubmitCharacterPacket(currentRace().id(), featureStyle)
        );
    }

    private Layout layout() {
        int outerMargin = this.width < 640 || this.height < 360 ? 6 : 18;

        int panelWidth = Math.max(1, Math.min(860, this.width - outerMargin * 2));
        int panelHeight = Math.max(1, Math.min(460, this.height - outerMargin * 2));

        int left = Math.max(0, (this.width - panelWidth) / 2);
        int top = Math.max(0, (this.height - panelHeight) / 2);

        boolean compact = panelWidth < 700 || panelHeight < 380;

        int gap = compact ? 4 : 6;

        int leftWidth;
        int rightWidth;

        if (compact) {
            leftWidth = Math.max(118, (int) (panelWidth * 0.285D));
            rightWidth = Math.max(118, (int) (panelWidth * 0.285D));

            int maxSideWidth = Math.max(118, (panelWidth - gap * 2 - 120) / 2);
            leftWidth = Math.min(leftWidth, maxSideWidth);
            rightWidth = Math.min(rightWidth, maxSideWidth);
        } else {
            leftWidth = Math.min(205, (int) (panelWidth * 0.28D));
            rightWidth = Math.min(205, (int) (panelWidth * 0.28D));
        }

        int middleWidth = Math.max(120, panelWidth - leftWidth - rightWidth - gap * 2);

        if (leftWidth + rightWidth + middleWidth + gap * 2 > panelWidth) {
            int overflow = leftWidth + rightWidth + middleWidth + gap * 2 - panelWidth;
            int sideCut = (overflow + 1) / 2;
            leftWidth = Math.max(96, leftWidth - sideCut);
            rightWidth = Math.max(96, rightWidth - (overflow - sideCut));
            middleWidth = Math.max(100, panelWidth - leftWidth - rightWidth - gap * 2);
        }

        int middle = left + leftWidth + gap;
        int right = middle + middleWidth + gap;

        return new Layout(
            left,
            top,
            panelWidth,
            panelHeight,
            leftWidth,
            middle,
            middleWidth,
            right,
            rightWidth,
            left + panelWidth / 2,
            compact,
            compact ? 31 : 54,
            compact ? 21 : 43,
            compact ? 53 : 88,
            compact ? 30 : 58,
            compact ? 36 : 52,
            compact ? 48 : 78,
            compact ? 74 : 110,
            compact ? 26 : 36,
            compact ? 38 : 70
        );
    }

    private void tileBackground(GuiGraphics graphics) {
        for (int y = 0; y < this.height; y += 32) {
            for (int x = 0; x < this.width; x += 32) {
                graphics.blit(BACKGROUND, x, y, 0, 0, 32, 32, 32, 32);
            }
        }
    }

    private record Layout(
        int left,
        int top,
        int width,
        int height,
        int leftWidth,
        int middle,
        int middleWidth,
        int right,
        int rightWidth,
        int centerX,
        boolean compact,
        int selectorY,
        int sectionTitleY,
        int statsY,
        int previewTop,
        int previewBottom,
        int featureButtonY,
        int featureTextY,
        int confirmBottomOffset,
        int playerBottomOffset
    ) {
    }
}
