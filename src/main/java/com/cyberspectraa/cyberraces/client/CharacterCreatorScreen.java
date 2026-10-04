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
import net.minecraft.util.Mth;

public final class CharacterCreatorScreen extends Screen {
    private static final ResourceLocation BACKGROUND =
        new ResourceLocation("minecraft", "textures/gui/options_background.png");

    private int raceIndex;
    private int featureStyle;

    private Button raceButton;
    private Button featureButton;

    public CharacterCreatorScreen() {
        super(Component.literal("Create Your Character"));
        this.raceIndex = 0;
    }

    @Override
    protected void init() {
        Layout layout = layout();

        int selectorY = layout.top + 54;
        this.addRenderableWidget(
            Button.builder(Component.literal("<"), button -> changeRace(-1))
                .bounds(layout.left + 14, selectorY, 24, 20)
                .build()
        );

        this.raceButton = this.addRenderableWidget(
            Button.builder(Component.literal(currentRace().displayName()), button -> changeRace(1))
                .bounds(layout.left + 42, selectorY, layout.leftWidth - 84, 20)
                .build()
        );

        this.addRenderableWidget(
            Button.builder(Component.literal(">"), button -> changeRace(1))
                .bounds(layout.left + layout.leftWidth - 38, selectorY, 24, 20)
                .build()
        );

        this.featureButton = this.addRenderableWidget(
            Button.builder(Component.empty(), button -> {
                featureStyle = (featureStyle + 1) % CharacterAppearance.FEATURE_STYLE_COUNT;
                refreshLabels();
            })
                .bounds(layout.right + 14, layout.top + 78, layout.rightWidth - 28, 20)
                .build()
        );

        this.addRenderableWidget(
            Button.builder(Component.literal("Confirm Character"), button -> submit())
                .bounds(layout.centerX - 80, layout.top + layout.height - 36, 160, 20)
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

        graphics.drawCenteredString(this.font, this.title, layout.centerX, layout.top + 12, 0xFFFFFF);
        graphics.drawCenteredString(
            this.font,
            "Your Minecraft skin stays intact - CyberRaces adds the racial features.",
            layout.centerX,
            layout.top + 27,
            0xB8B8B8
        );

        graphics.drawString(this.font, "Race", layout.left + 14, layout.top + 43, 0xE6D39A, false);
        renderRaceInfo(graphics, layout);

        graphics.drawCenteredString(this.font, "Preview", layout.middle + layout.middleWidth / 2, layout.top + 43, 0xE6D39A);
        graphics.fill(
            layout.middle + 16,
            layout.top + 58,
            layout.middle + layout.middleWidth - 16,
            bottom - 52,
            0x70202020
        );

        renderPlayerPreview(graphics, layout, mouseX, mouseY);

        graphics.drawString(this.font, "Race Features", layout.right + 14, layout.top + 43, 0xE6D39A, false);
        graphics.drawString(
            this.font,
            featureDescription(),
            layout.right + 14,
            layout.top + 110,
            0xD0D0D0,
            false
        );

        graphics.drawString(
            this.font,
            "Face, eyes, hair and clothing",
            layout.right + 14,
            layout.top + 142,
            0x9E9E9E,
            false
        );
        graphics.drawString(
            this.font,
            "come from your normal skin.",
            layout.right + 14,
            layout.top + 154,
            0x9E9E9E,
            false
        );

        graphics.drawCenteredString(
            this.font,
            "Confirming releases you into the world and allows CyberServer to play your arrival.",
            layout.centerX,
            bottom - 13,
            0xAFAFAF
        );

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
        int y = layout.top + 88;
        int x = layout.left + 14;

        graphics.drawString(this.font, "Health", x, y, 0x9E9E9E, false);
        graphics.drawString(this.font, String.valueOf((int) race.maxHealth()), x + 92, y, 0xFFFFFF, false);

        graphics.drawString(this.font, "Size", x, y + 16, 0x9E9E9E, false);
        graphics.drawString(this.font, String.format("%.2fx", race.scale()), x + 92, y + 16, 0xFFFFFF, false);

        graphics.drawString(this.font, "Speed", x, y + 32, 0x9E9E9E, false);
        graphics.drawString(this.font, String.format("%.0f%%", race.movementMultiplier() * 100.0D), x + 92, y + 32, 0xFFFFFF, false);

        graphics.drawString(this.font, "Mana", x, y + 48, 0x9E9E9E, false);
        graphics.drawString(this.font, String.format("%.0f%%", race.maxManaMultiplier() * 100.0D), x + 92, y + 48, 0xFFFFFF, false);

        graphics.drawString(this.font, "Mana regen", x, y + 64, 0x9E9E9E, false);
        graphics.drawString(this.font, String.format("%.0f%%", race.manaRegenMultiplier() * 100.0D), x + 92, y + 64, 0xFFFFFF, false);

        int noteY = y + 94;
        for (String line : raceSummary(race)) {
            graphics.drawString(this.font, line, x, noteY, 0xD0D0D0, false);
            noteY += 12;
        }
    }

    private void renderPlayerPreview(GuiGraphics graphics, Layout layout, int mouseX, int mouseY) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        ClientCharacterState.setPreview(currentRace(), featureStyle);

        boolean wasInvisible = this.minecraft.player.isInvisible();
        this.minecraft.player.setInvisible(false);

        int previewX = layout.middle + layout.middleWidth / 2;
        int previewY = layout.top + layout.height - 70;
        int previewScale = Mth.clamp(
            (int) (82.0D * Math.sqrt(Math.max(0.5D, currentRace().scale()))),
            58,
            90
        );

        try {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics,
                previewX,
                previewY,
                previewScale,
                (float) (previewX - mouseX),
                (float) (layout.top + 130 - mouseY),
                this.minecraft.player
            );
        } finally {
            this.minecraft.player.setInvisible(wasInvisible);
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
        int panelWidth = Math.min(760, Math.max(600, this.width - 80));
        int panelHeight = Math.min(430, Math.max(330, this.height - 70));
        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2;

        int gap = 6;
        int leftWidth = 205;
        int rightWidth = 205;
        int middleWidth = panelWidth - leftWidth - rightWidth - gap * 2;
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
            left + panelWidth / 2
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
        int centerX
    ) {
    }
}
