package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.SubmitCharacterPacket;
import com.cyberspectraa.cyberraces.network.packet.SaveAppearanceMirrorPacket;
import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.DragonVariant;
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
    private static final Race[] STARTING_RACES = Race.startingChoices();

    private static final String[] BIRD_WING_NAMES = {
        "White", "Orange", "Magenta", "Light Blue",
        "Yellow", "Lime", "Pink", "Gray",
        "Light Gray", "Cyan", "Purple", "Blue",
        "Brown", "Green", "Red", "Black"
    };

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
    private Button tuskButton;
    private final boolean mirrorMode;
    private final String mirrorEvolutionId;

    public CharacterCreatorScreen() {
        super(Component.literal("Create Your Character"));
        mirrorMode = false;
        mirrorEvolutionId = "";
    }

    public CharacterCreatorScreen(Race race, String evolutionId,
            CharacterAppearance saved) {
        super(Component.literal("Appearance Mirror"));
        mirrorMode = true;
        mirrorEvolutionId = evolutionId == null ? "" : evolutionId;
        for (int i = 0; i < STARTING_RACES.length; i++) {
            if (STARTING_RACES[i] == race) {
                raceIndex = i;
                break;
            }
        }
        featureStyle = saved.featureStyle();
        featureColor = saved.featureColor();
        earHeight = saved.earHeight();
        earSpread = saved.earSpread();
        earTilt = saved.earTilt();
        bodySourceColor = saved.bodySourceColor();
        bodyTargetColor = saved.bodyTargetColor();
        bodyTolerance = saved.bodyTolerance();
    }

    @Override
    protected void init() {
        Layout l = layout();
        int buttonHeight = l.small ? 18 : 20;
        int pad = l.small ? 6 : 12;
        int arrowWidth = l.small ? 18 : 24;

        int selectorY = l.top + (l.small ? 38 : 54);

        Button previousRace = this.addRenderableWidget(
            Button.builder(Component.literal("<"), button -> changeRace(-1))
                .bounds(l.left + pad, selectorY, arrowWidth, buttonHeight)
                .build()
        );
        previousRace.active = !mirrorMode;

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

        Button nextRace = this.addRenderableWidget(
            Button.builder(Component.literal(">"), button -> changeRace(1))
                .bounds(raceX + raceWidth + 3, selectorY, arrowWidth, buttonHeight)
                .build()
        );
        nextRace.active = !mirrorMode;
        raceButton.active = !mirrorMode;

        int featureY = l.top + (l.small ? 42 : 78);
        this.featureButton = this.addRenderableWidget(
            Button.builder(Component.empty(), button -> {
                if (currentRace() == Race.SLIME) {
                    featureStyle = SlimeVariantPalette.nextAvailable(featureStyle);
                    // Store the selected RGB as well: the server uses it for
                    // coloured jump/land particles, including resource-pack slimes.
                    featureColor = SlimeVariantPalette.color(featureStyle);
                } else if (currentRace() == Race.DRAGONBORN) {
                    featureStyle = DragonVariant.style(
                            DragonVariant.type(featureStyle),
                            DragonVariant.horns(featureStyle) + 1);
                } else if (currentRace() == Race.GOBLIN) {
                    featureStyle = (featureStyle / 3) * 3
                            + Math.floorMod(featureStyle + 1, 3);
                } else {
                    featureStyle = (featureStyle + 1)
                            % featureVariantCount(currentRace());
                }
                refreshLabels();
            })
                .bounds(l.right + pad, featureY, Math.max(60, l.rightWidth - pad * 2), buttonHeight)
                .build()
        );

        this.colourButton = this.addRenderableWidget(
            Button.builder(Component.empty(), button -> {
                if (currentRace() == Race.DRAGONBORN) {
                    featureColor = DragonVariant.nextColor(featureColor);
                    refreshLabels();
                } else if (this.minecraft != null) {
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
                if (currentRace() == Race.DRAGONBORN) {
                    featureStyle = DragonVariant.style(
                            DragonVariant.type(featureStyle) + 1,
                            DragonVariant.horns(featureStyle));
                    refreshLabels();
                } else if (this.minecraft != null) {
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

        this.tuskButton = this.addRenderableWidget(
            Button.builder(Component.empty(), button -> {
                featureStyle = Math.floorMod(featureStyle + 3, 6);
                refreshLabels();
            })
            .bounds(l.right + pad, featureY + (l.small ? 88 : 96),
                    Math.max(60, l.rightWidth - pad * 2), buttonHeight)
            .build());

        int confirmWidth = l.small
            ? Math.min(126, l.middleWidth - 12)
            : Math.min(160, l.middleWidth - 20);

        confirmWidth = Math.max(92, confirmWidth);

        this.addRenderableWidget(
            Button.builder(Component.literal(mirrorMode ? "Save Look" : "Confirm"),
                    button -> submit())
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
                "Keep your skin. Choose the traits of your race.",
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
        boolean mirrorTusks = mirrorMode && currentRace() == Race.GOBLIN
                && "hobgoblin".equals(mirrorEvolutionId);
        int y = l.top + (mirrorTusks ? (l.small ? 157 : 224)
                : (l.small ? 136 : 184));

        drawWrapped(
            graphics,
            featureDescription(),
            x,
            y,
            maxWidth,
            0xD0D0D0,
            l.small ? 4 : 5
        );

        if (!l.small && !mirrorTusks) {
            drawWrapped(
                graphics,
                BeastVariantTextures.isBeastfolk(currentRace())
                    ? (currentRace() == Race.CATFOLK
                        ? "Figura-style cat ears and tail. Pick a coat above."
                        : "Ears and tail follow the selected animal coat.")
                    : currentRace() == Race.SLIME
                        ? "Appearance only: slime abilities do not change."
                    : currentRace() == Race.BIRDFOLK
                        ? "Choose your wing colour above."
                        : !hasFeatureColour(currentRace())
                            ? "This feature has no colour setting."
                            : FeatureColourPalette.hasEars(currentRace())
                                ? "Use Skin to match your skin, or choose a colour."
                                : "Choose a colour for this feature.",
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
        return mirrorMode;
    }

    @Override
    public void onClose() {
        if (mirrorMode) super.onClose();
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
        if (mirrorMode) return;
        raceIndex = Math.floorMod(raceIndex + direction, STARTING_RACES.length);
        featureStyle = 0;
        featureColor = currentRace() == Race.DRAGONBORN
                ? DragonVariant.DEFAULT_COLOR
                : currentRace() == Race.SLIME
                    ? SlimeVariantPalette.color(0)
                    : CharacterAppearance.AUTO_COLOR;
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
                race == Race.DRAGONBORN
                    ? "Horns/Tail/Wings: " + DragonVariant.dyeName(featureColor)
                    : "Colour: " + FeatureColourPalette.label(race, featureColor)
            ));
            colourButton.active = featureColour;
            colourButton.visible = featureColour;
        }

        if (earFitButton != null) {
            earFitButton.active = race == Race.DRAGONBORN
                    || FeatureColourPalette.hasEars(race);
            earFitButton.visible = earFitButton.active;
            earFitButton.setMessage(Component.literal(
                    race == Race.DRAGONBORN
                        ? "Breath: " + DragonVariant.name(featureStyle)
                        : "Ear Fit..."));
        }

        if (tuskButton != null) {
            boolean tusks = mirrorMode && race == Race.GOBLIN
                    && "hobgoblin".equals(mirrorEvolutionId);
            tuskButton.visible = tusks;
            tuskButton.active = tusks;
            tuskButton.setMessage(Component.literal(
                    "Tusks: " + (featureStyle < 3 ? "Show" : "Hide")));
        }

        if (bodyColourButton != null) {
            // Slimes already have the correct automatic gel tint. Only
            // races with optional skin recolouring show this picker.
            boolean supportsBodyColour =
                race != Race.SLIME && RaceSkinOverlayManager.supports(race);
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
        return race != Race.HUMAN
            && race != Race.DWARF
            && race != Race.NYMPH
            && race != Race.AASIMAR
            && race != Race.ARACHNID
            && race != Race.UNDEAD;
    }

    private boolean hasFeatureColour(Race race) {
        return race != Race.HUMAN
            && race != Race.DWARF
            && race != Race.NYMPH
            && race != Race.AASIMAR
            && race != Race.BIRDFOLK
            && race != Race.SLIME
            && !BeastVariantTextures.isBeastfolk(race);
    }

    private int featureVariantCount(Race race) {
        if (race == Race.BIRDFOLK) {
            return BIRD_WING_NAMES.length;
        }

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
            case DRAGONBORN -> new String[] {
                "Horns: " + switch (DragonVariant.horns(featureStyle)) {
                    case 1 -> "Crowned";
                    case 2 -> "Swept";
                    default -> "Horned";
                }
            };
            case FAIRY -> new String[] {"Ears: Classic", "Ears: Sharp", "Ears: Soft"};
            case NYMPH -> new String[] {"Nature spirit", "Nature spirit", "Nature spirit"};
            case CATFOLK, DOGFOLK, FOXFOLK -> new String[] {
                "Variant: " + BeastVariantTextures.name(currentRace(), featureStyle)
            };
            case BIRDFOLK -> new String[] {
                "Wings: " + BIRD_WING_NAMES[Math.floorMod(featureStyle, BIRD_WING_NAMES.length)]
            };
            case AASIMAR -> new String[] {"Celestial", "Celestial", "Celestial"};
            case HUMAN -> new String[] {"No feature", "No feature", "No feature"};
            case DWARF -> new String[] {"Stout build", "Stout build", "Stout build"};
            case SLIME -> new String[] {
                "Variant: " + SlimeVariantPalette.name(featureStyle)
            };
            case ARACHNID -> new String[] {"Silk-blooded", "Silk-blooded", "Silk-blooded"};
            case UNDEAD -> new String[] {"Undying form", "Undying form", "Undying form"};
        };
        return BeastVariantTextures.isBeastfolk(currentRace())
                || currentRace() == Race.DRAGONBORN
                || currentRace() == Race.SLIME
            ? labels[0]
            : labels[Math.min(Math.floorMod(featureStyle, 3), labels.length - 1)];
    }

    private String featureDescription() {
        return switch (currentRace()) {
            case HUMAN -> "No extra ears, horns or tails.";
            case ELF -> "Pointed ears with adjustable height and angle.";
            case DWARF -> "Short build; no added ears or horns.";
            case HALFLING -> "Small ears and a short build.";
            case ORC -> "Broad pointed ears with adjustable placement.";
            case GOBLIN -> "Long, outward-pointing ears.";
            case TIEFLING -> "Curved horns and a moving tail.";
            case DRAGONBORN -> "Choose your breath, horn shape and wing-matched colour.";
            case FAIRY -> "Pointed ears and delicate wings.";
            case NYMPH -> "Nature-tinted skin without added limbs.";
            case CATFOLK -> "Cat ears and tail, with selectable coat patterns.";
            case DOGFOLK -> "Wolf ears and tail, with selectable coat patterns.";
            case FOXFOLK -> "Fox ears and tail; red and snow coats.";
            case AASIMAR -> "Celestial features. Not a starting race.";
            case BIRDFOLK -> "Feathered wings in your chosen colour.";
            case SLIME -> "Choose your gel colour and type. Abilities stay the same.";
            case ARACHNID -> "Spider-like movement without extra limbs.";
            case UNDEAD -> "Undead form gained through infection.";
        };
    }

    private String[] raceSummary(Race race) {
        return switch (race) {
            case HUMAN -> new String[] {
                "No racial bonuses to health, speed or mana.",
                "Gains 5% more experience."
            };
            case ELF -> new String[] {
                "18 health; quicker, with better mana.",
                "Bows charge 15% faster."
            };
            case DWARF -> new String[] {
                "22 health. Sturdy, but slower on foot.",
                "Mines faster underground; extra resource drops."
            };
            case HALFLING -> new String[] {
                "18 health; small, quick and lucky.",
                "+1 Luck; takes 35% less fall damage."
            };
            case ORC -> new String[] {
                "24 health; hits harder when badly hurt.",
                "V: War Cry grants Strength and knockback resistance."
            };
            case GOBLIN -> new String[] {
                "18 health, high speed and +0.5 Luck.",
                "V: Treasure Instinct marks ores and stocked containers."
            };
            case TIEFLING -> new String[] {
                "Takes half damage from fire.",
                "V: Infernal Surge boosts melee and sets foes alight."
            };
            case DRAGONBORN -> new String[] {
                "22 health, tough scales and natural armour.",
                "V: " + (DragonVariant.type(featureStyle) == 2
                    ? "Lightning breath stream; no mana cost."
                    : DragonVariant.name(featureStyle)
                        + " breath through Iron's Spells.")
            };
            case FAIRY -> new String[] {
                "14 health; tiny, quick and good with mana.",
                "V: Toggle Hover. Natural wings with Icarus."
            };
            case NYMPH -> new String[] {
                "18 health; good mana, but less sturdy.",
                "Heals in rain or water. V: Nature's Grace."
            };
            case CATFOLK -> new String[] {
                "Quicker movement and higher jumps.",
                "Takes 60% less fall damage."
            };
            case DOGFOLK -> new String[] {
                "22 health; uses less hunger while sprinting.",
                "V: Hunter's Nose outlines nearby creatures."
            };
            case FOXFOLK -> new String[] {
                "18 health, quick and light on their feet.",
                "Sneaks faster. V: Quickstep dashes forward."
            };
            case AASIMAR -> new String[] {
                "Resists spells and takes less fall damage.",
                "Legacy race; cannot be chosen at creation."
            };
            case BIRDFOLK -> new String[] {
                "18 health, with feathered wings.",
                "V: Wing Burst. Takes 65% less fall damage."
            };
            case SLIME -> new String[] {
                "Bounces after falls and takes no fall damage.",
                "Higher jumps; sneak to land without bouncing."
            };
            case ARACHNID -> new String[] {
                "Climbs walls; sneak to hold your place.",
                "V: Web Shot leaves a temporary cobweb."
            };
            case UNDEAD -> new String[] {
                "Acquired through Zombification, not at birth.",
                "Undead won't attack; beware daylight and fire."
            };
        };
    }

    Race currentRace() {
        return STARTING_RACES[raceIndex];
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
        ClientCharacterState.setPreview(currentRace(), currentAppearance(),
                mirrorMode ? mirrorEvolutionId : "");
    }

    private void submit() {
        CharacterAppearance appearance = currentAppearance();
        if (mirrorMode) {
            CyberRacesNetwork.sendToServer(new SaveAppearanceMirrorPacket(appearance));
            return;
        }
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
