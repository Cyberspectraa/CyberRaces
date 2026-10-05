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
    private int featureColor = CharacterAppearance.AUTO_COLOR;
    private int earHeight;
    private int earSpread;
    private int earTilt;
    private int bodySourceColor = CharacterAppearance.AUTO_COLOR;
    private int bodyTargetColor = CharacterAppearance.AUTO_COLOR;
    private int bodyTolerance = CharacterAppearance.BODY_TOLERANCE_DEFAULT;

    private Button raceButton;
    private Button featureButton;
    private Button colourButton;
    private Button earFitButton;
    private Button bodyColourButton;

    public CharacterCreatorScreen() {
        super(Component.literal("Create Your Character"));
    }

    @Override
    protected void init() {
        Layout l = layout();
        int buttonHeight = l.small ? 18 : 20;
        int pad = l.small ? 6 : 12;
        int arrowWidth = l.small ? 18 : 24;

        int selectorY = l.top + (l.small ? 38 : 54);

        this.addRenderableWidget(
            Button.builder(Component.literal("<"), button -> changeRace(-1))
                .bounds(l.left + pad, selectorY, arrowWidth, buttonHeight)
                .build()
        );

        int raceX = l.left + pad + arrowWidth + 3;
        int raceWidth = Math.max(
            44,
            l.leftWidth - (pad * 2) - (arrowWidth * 2) - 6
        );

        this.raceButton = this.addRenderableWidget(
            Button.builder(Component.literal(currentRace().displayName()), button -> changeRace(1))
                .bounds(raceX, selectorY, raceWidth, buttonHeight)
                .build()
        );

        this.addRenderableWidget(
            Button.builder(Component.literal(">"), button -> changeRace(1))
                .bounds(raceX + raceWidth + 3, selectorY, arrowWidth, buttonHeight)
                .build()
        );

        int featureY = l.top + (l.small ? 42 : 78);
        this.featureButton = this.addRenderableWidget(
            Button.builder(Component.empty(), button -> {
                featureStyle = (featureStyle + 1) % featureVariantCount(currentRace());
                refreshLabels();
            })
                .bounds(l.right + pad, featureY, Math.max(60, l.rightWidth - pad * 2), buttonHeight)
                .build()
        );

        this.colourButton = this.addRenderableWidget(
            Button.builder(Component.empty(), button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(
                        new FeatureColorPickerScreen(this, currentRace(), featureColor)
                    );
                }
            })
                .bounds(
                    l.right + pad,
                    featureY + (l.small ? 22 : 24),
                    Math.max(60, l.rightWidth - pad * 2),
                    buttonHeight
                )
                .build()
        );

        this.earFitButton = this.addRenderableWidget(
            Button.builder(Component.literal("Ear Fit..."), button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(
                        new EarAdjustScreen(this, earHeight, earSpread, earTilt)
                    );
                }
            })
                .bounds(
                    l.right + pad,
                    featureY + (l.small ? 44 : 48),
                    Math.max(60, l.rightWidth - pad * 2),
                    buttonHeight
                )
                .build()
        );

        this.bodyColourButton = this.addRenderableWidget(
            Button.builder(Component.literal("Body Colour..."), button -> {
                if (this.minecraft != null) {
                    this.minecraft.setScreen(
                        new BodySkinColorScreen(
                            this,
                            currentRace(),
                            bodySourceColor,
                            bodyTargetColor,
                            bodyTolerance
                        )
                    );
                }
            })
                .bounds(
                    l.right + pad,
                    featureY + (l.small ? 66 : 72),
                    Math.max(60, l.rightWidth - pad * 2),
                    buttonHeight
                )
                .build()
        );

        int confirmWidth = l.small
            ? Math.min(126, l.middleWidth - 12)
            : Math.min(160, l.middleWidth - 20);

        confirmWidth = Math.max(92, confirmWidth);

        this.addRenderableWidget(
            Button.builder(Component.literal("Confirm"), button -> submit())
                .bounds(
                    l.centerX - confirmWidth / 2,
                    l.bottom - (l.small ? 22 : 30),
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

        Layout l = layout();

        graphics.fill(l.left, l.top, l.left + l.leftWidth, l.bottom, 0xC0101010);
        graphics.fill(l.middle, l.top, l.middle + l.middleWidth, l.bottom, 0xC0101010);
        graphics.fill(l.right, l.top, l.right + l.rightWidth, l.bottom, 0xC0101010);

        graphics.drawCenteredString(
            this.font,
            this.title,
            l.centerX,
            l.top + (l.small ? 6 : 10),
            0xFFFFFF
        );

        if (!l.small) {
            graphics.drawCenteredString(
                this.font,
                "Your normal Minecraft skin stays intact.",
                l.centerX,
                l.top + 24,
                0xB8B8B8
            );
        }

        int titleY = l.top + (l.small ? 24 : 42);
        int sidePad = l.small ? 6 : 12;

        graphics.drawString(this.font, "Race", l.left + sidePad, titleY, 0xE6D39A, false);
        graphics.drawCenteredString(this.font, "Preview", l.middle + l.middleWidth / 2, titleY, 0xE6D39A);
        graphics.drawString(
            this.font,
            l.small ? "Features" : "Race Features",
            l.right + sidePad,
            titleY,
            0xE6D39A,
            false
        );

        int previewTop = l.top + (l.small ? 38 : 58);
        int previewBottom = l.bottom - (l.small ? 28 : 42);

        if (previewBottom > previewTop + 10) {
            graphics.fill(
                l.middle + (l.small ? 4 : 12),
                previewTop,
                l.middle + l.middleWidth - (l.small ? 4 : 12),
                previewBottom,
                0x70202020
            );
        }

        renderRaceInfo(graphics, l);
        renderFeatureInfo(graphics, l);
        renderPlayerPreview(graphics, l, mouseX, mouseY);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderRaceInfo(GuiGraphics graphics, Layout l) {
        Race race = currentRace();

        int x = l.left + (l.small ? 6 : 12);
        int valueX = l.left + l.leftWidth - (l.small ? 38 : 48);
        int y = l.top + (l.small ? 64 : 88);
        int spacing = l.small ? 11 : 15;

        drawStat(graphics, l.small ? "HP" : "Health", String.valueOf((int) race.maxHealth()), x, valueX, y);
        drawStat(graphics, "Size", String.format("%.2fx", race.scale()), x, valueX, y + spacing);
        drawStat(graphics, "Speed", String.format("%.0f%%", race.movementMultiplier() * 100.0D), x, valueX, y + spacing * 2);
        drawStat(graphics, "Mana", String.format("%.0f%%", race.maxManaMultiplier() * 100.0D), x, valueX, y + spacing * 3);

        if (!l.small) {
            drawStat(
                graphics,
                "Regen",
                String.format("%.0f%%", race.manaRegenMultiplier() * 100.0D),
                x,
                valueX,
                y + spacing * 4
            );
        }

        int summaryY = y + spacing * (l.small ? 4 : 5) + (l.small ? 7 : 14);
        int maxWidth = Math.max(48, l.leftWidth - (l.small ? 12 : 24));
        int maxLines = l.small ? 3 : 5;

        int line = 0;
        for (String sentence : raceSummary(race)) {
            List<FormattedCharSequence> wrapped = this.font.split(Component.literal(sentence), maxWidth);
            for (FormattedCharSequence seq : wrapped) {
                if (line >= maxLines) {
                    return;
                }
                graphics.drawString(this.font, seq, x, summaryY + line * 10, 0xD0D0D0, false);
                line++;
            }
        }
    }

    private void renderFeatureInfo(GuiGraphics graphics, Layout l) {
        int x = l.right + (l.small ? 6 : 12);
        int maxWidth = Math.max(48, l.rightWidth - (l.small ? 12 : 24));
        int y = l.top + (l.small ? 136 : 184);

        drawWrapped(
            graphics,
            featureDescription(),
            x,
            y,
            maxWidth,
            0xD0D0D0,
            l.small ? 4 : 5
        );

        if (!l.small) {
            drawWrapped(
                graphics,
                BeastVariantTextures.isBeastfolk(currentRace())
                    ? "Ears and tail use the selected Minecraft animal coat texture."
                    : FeatureColourPalette.hasEars(currentRace())
                        ? "Use Skin for automatic texture matching, or pick any custom colour."
                        : "Pick any custom colour for this racial feature.",
                x,
                l.top + 202,
                maxWidth,
                0x9E9E9E,
                5
            );
        }
    }

    private void renderPlayerPreview(GuiGraphics graphics, Layout l, int mouseX, int mouseY) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        pushPreviewState();

        boolean wasInvisible = this.minecraft.player.isInvisible();
        this.minecraft.player.setInvisible(false);

        int previewX = l.middle + l.middleWidth / 2;
        int previewY = l.bottom - (l.small ? 34 : 52);

        int availableHeight = Math.max(60, l.height - (l.small ? 68 : 105));
        int widthLimited = (int) (l.middleWidth * (l.small ? 0.30D : 0.33D));
        int heightLimited = (int) (availableHeight * (l.small ? 0.34D : 0.42D));

        int baseScale = Math.max(28, Math.min(widthLimited, heightLimited));
        int previewScale = Mth.clamp(
            (int) (baseScale * Math.sqrt(Math.max(0.5D, currentRace().scale()))),
            l.small ? 28 : 42,
            l.small ? 58 : 86
        );

        try {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics,
                previewX,
                previewY,
                previewScale,
                (float) (previewX - mouseX),
                (float) (l.top + l.height / 2 - mouseY),
                this.minecraft.player
            );
        } finally {
            this.minecraft.player.setInvisible(wasInvisible);
        }
    }

    private void drawStat(
        GuiGraphics graphics,
        String label,
        String value,
        int x,
        int valueX,
        int y
    ) {
        graphics.drawString(this.font, label, x, y, 0x9E9E9E, false);
        graphics.drawString(this.font, value, valueX, y, 0xFFFFFF, false);
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
            graphics.drawString(this.font, lines.get(i), x, y + i * 10, color, false);
        }
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
        // Keep preview state while temporarily visiting the colour/ear screens.
        if (!(this.minecraft != null
            && (this.minecraft.screen instanceof FeatureColorPickerScreen
                || this.minecraft.screen instanceof EarAdjustScreen
                || this.minecraft.screen instanceof BodySkinColorScreen))) {
            ClientCharacterState.clearPreview();
        }
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void changeRace(int direction) {
        raceIndex = Math.floorMod(raceIndex + direction, Race.values().length);
        featureStyle = 0;
        featureColor = CharacterAppearance.AUTO_COLOR;
        earHeight = 0;
        earSpread = 0;
        earTilt = 0;
        bodySourceColor = CharacterAppearance.AUTO_COLOR;
        bodyTargetColor = CharacterAppearance.AUTO_COLOR;
        bodyTolerance = CharacterAppearance.BODY_TOLERANCE_DEFAULT;
        refreshLabels();
    }

    private void refreshLabels() {
        Race race = currentRace();
        pushPreviewState();

        if (raceButton != null) {
            raceButton.setMessage(Component.literal(race.displayName()));
        }

        if (featureButton != null) {
            featureButton.setMessage(Component.literal(featureLabel()));
            featureButton.active = hasFeatureVariants(race);
        }

        if (colourButton != null) {
            boolean featureColour = hasFeatureColour(race);
            colourButton.setMessage(Component.literal(
                "Colour: " + FeatureColourPalette.label(race, featureColor)
            ));
            colourButton.active = featureColour;
            colourButton.visible = featureColour;
        }

        if (earFitButton != null) {
            earFitButton.active = FeatureColourPalette.hasEars(race);
            earFitButton.visible = FeatureColourPalette.hasEars(race);
        }

        if (bodyColourButton != null) {
            boolean supportsBodyColour = RaceSkinOverlayManager.supports(race);
            bodyColourButton.active = supportsBodyColour;
            bodyColourButton.visible = supportsBodyColour;
            bodyColourButton.setMessage(Component.literal(
                bodySourceColor == CharacterAppearance.AUTO_COLOR
                    ? "Body Colour..."
                    : "Body: " + String.format("#%06X", bodyTargetColor & 0xFFFFFF)
            ));
        }
    }

    private boolean hasFeatureVariants(Race race) {
        return race != Race.HUMAN && race != Race.DWARF;
    }

    private boolean hasFeatureColour(Race race) {
        return race != Race.HUMAN
            && race != Race.DWARF
            && !BeastVariantTextures.isBeastfolk(race);
    }

    private int featureVariantCount(Race race) {
        return BeastVariantTextures.isBeastfolk(race)
            ? BeastVariantTextures.count(race)
            : CharacterAppearance.FEATURE_STYLE_COUNT >= 3 ? 3 : CharacterAppearance.FEATURE_STYLE_COUNT;
    }

    private String featureLabel() {
        String[] labels = switch (currentRace()) {
            case ELF -> new String[] {"Ears: Short", "Ears: Long", "Ears: High"};
            case HALFLING -> new String[] {"Ears: Round", "Ears: Soft", "Ears: Pointed"};
            case ORC -> new String[] {"Ears: Short", "Ears: Broad", "Ears: Swept"};
            case GOBLIN -> new String[] {"Ears: Wide", "Ears: Long", "Ears: Swept"};
            case TIEFLING -> new String[] {"Horns: Curved", "Horns: Swept", "Horns: Tall"};
            case DRAGONBORN -> new String[] {"Crest: Horned", "Crest: Crowned", "Crest: Swept"};
            case FAIRY -> new String[] {"Ears: Classic", "Ears: Sharp", "Ears: Soft"};
            case CATFOLK, DOGFOLK, FOXFOLK -> new String[] {
                "Variant: " + BeastVariantTextures.name(currentRace(), featureStyle)
            };
            case HUMAN -> new String[] {"No feature", "No feature", "No feature"};
            case DWARF -> new String[] {"Stout build", "Stout build", "Stout build"};
        };
        return BeastVariantTextures.isBeastfolk(currentRace())
            ? labels[0]
            : labels[Math.min(featureStyle, labels.length - 1)];
    }

    private String featureDescription() {
        return switch (currentRace()) {
            case HUMAN -> "No forced racial geometry.";
            case ELF -> "Pointed ears with adjustable placement.";
            case DWARF -> "Short, sturdy silhouette.";
            case HALFLING -> "Small adjustable ears.";
            case ORC -> "Strong pointed ears with adjustable placement.";
            case GOBLIN -> "Large adjustable outward ears.";
            case TIEFLING -> "Horns and an animated tail.";
            case DRAGONBORN -> "Horned crest and scaled tail.";
            case FAIRY -> "Adjustable fey ears and Zanza's Wings.";
            case CATFOLK -> "Vanilla Cat ears/tail with selectable Cat coat variants.";
            case DOGFOLK -> "Vanilla Wolf ears/tail with selectable Wolf coat variants.";
            case FOXFOLK -> "Vanilla Fox ears/tail with Red or Snow coat variants.";
        };
    }

    private String[] raceSummary(Race race) {
        return switch (race) {
            case HUMAN -> new String[] {"Balanced and adaptable."};
            case ELF -> new String[] {"Fast and magical.", "Lower health."};
            case DWARF -> new String[] {"Tough and resistant.", "Slightly slower."};
            case HALFLING -> new String[] {"Small and nimble."};
            case ORC -> new String[] {"High health.", "Lower natural mana."};
            case GOBLIN -> new String[] {"Small and quick.", "More fragile."};
            case TIEFLING -> new String[] {"Strong magic.", "Fire resistance."};
            case DRAGONBORN -> new String[] {"Armoured and resilient."};
            case FAIRY -> new String[] {"Tiny and magical.", "Fragile; can fly."};
            case CATFOLK -> new String[] {"Fast and agile.", "Balanced magic."};
            case DOGFOLK -> new String[] {"Sturdy and loyal.", "Slightly lower mana."};
            case FOXFOLK -> new String[] {"Quick and magical.", "Lower health."};
        };
    }

    Race currentRace() {
        return Race.values()[raceIndex];
    }

    CharacterAppearance currentAppearance() {
        return new CharacterAppearance(
            featureStyle,
            featureColor,
            earHeight,
            earSpread,
            earTilt,
            bodySourceColor,
            bodyTargetColor,
            bodyTolerance
        );
    }

    void setFeatureColor(int rgb) {
        featureColor = rgb == CharacterAppearance.AUTO_COLOR
            ? CharacterAppearance.AUTO_COLOR
            : rgb & 0xFFFFFF;
        refreshLabels();
    }

    void setEarAdjust(int height, int spread, int tilt) {
        earHeight = height;
        earSpread = spread;
        earTilt = tilt;
        pushPreviewState();
    }

    void setBodySkin(int sourceColor, int targetColor, int tolerance) {
        bodySourceColor = sourceColor == CharacterAppearance.AUTO_COLOR
            ? CharacterAppearance.AUTO_COLOR
            : sourceColor & 0xFFFFFF;

        bodyTargetColor = targetColor == CharacterAppearance.AUTO_COLOR
            ? CharacterAppearance.AUTO_COLOR
            : targetColor & 0xFFFFFF;

        bodyTolerance = Math.max(
            CharacterAppearance.BODY_TOLERANCE_MIN,
            Math.min(CharacterAppearance.BODY_TOLERANCE_MAX, tolerance)
        );

        refreshLabels();
    }

    void pushPreviewState() {
        ClientCharacterState.setPreview(currentRace(), currentAppearance());
    }

    private void submit() {
        CharacterAppearance appearance = currentAppearance();
        CyberRacesNetwork.sendToServer(
            new SubmitCharacterPacket(
                currentRace().id(),
                appearance.featureStyle(),
                appearance.featureColor(),
                appearance.earHeight(),
                appearance.earSpread(),
                appearance.earTilt(),
                appearance.bodySourceColor(),
                appearance.bodyTargetColor(),
                appearance.bodyTolerance()
            )
        );
    }

    private Layout layout() {
        boolean small = this.width <= 600 || this.height <= 340;

        int margin = small ? 4 : 18;
        int width = Math.max(300, Math.min(small ? this.width - margin * 2 : 860, this.width - margin * 2));
        int height = Math.max(220, Math.min(small ? this.height - margin * 2 : 460, this.height - margin * 2));

        width = Math.min(width, this.width);
        height = Math.min(height, this.height);

        int left = Math.max(0, (this.width - width) / 2);
        int top = Math.max(0, (this.height - height) / 2);
        int bottom = Math.min(this.height, top + height);

        int gap = small ? 3 : 6;

        int sideWidth;
        if (small) {
            sideWidth = Math.max(96, (int) (width * 0.28D));
            int maxSide = Math.max(96, (width - gap * 2 - 100) / 2);
            sideWidth = Math.min(sideWidth, maxSide);
        } else {
            sideWidth = Math.min(205, (int) (width * 0.27D));
        }

        int middleWidth = width - sideWidth * 2 - gap * 2;

        if (middleWidth < 100) {
            int needed = 100 - middleWidth;
            int cut = (needed + 1) / 2;
            sideWidth = Math.max(82, sideWidth - cut);
            middleWidth = width - sideWidth * 2 - gap * 2;
        }

        int middle = left + sideWidth + gap;
        int right = middle + middleWidth + gap;

        return new Layout(
            left,
            top,
            bottom,
            width,
            height,
            sideWidth,
            middle,
            middleWidth,
            right,
            sideWidth,
            left + width / 2,
            small
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
        int bottom,
        int width,
        int height,
        int leftWidth,
        int middle,
        int middleWidth,
        int right,
        int rightWidth,
        int centerX,
        boolean small
    ) {
    }
}
