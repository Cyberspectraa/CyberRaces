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

public final class CharacterCreatorScreen extends Screen {
    private static final ResourceLocation BACKGROUND =
        new ResourceLocation("minecraft", "textures/gui/options_background.png");

    private static final String[] EYE_STYLES = {
        "Classic", "Sharp", "Soft", "Narrow"
    };

    private static final String[] EYE_COLORS = {
        "Brown", "Blue", "Green", "Hazel", "Grey", "Amber", "Red", "Violet"
    };

    private int raceIndex;
    private int eyeStyle;
    private int eyeColor;
    private int featureStyle;

    private Button raceButton;
    private Button eyeStyleButton;
    private Button eyeColorButton;
    private Button featureButton;

    public CharacterCreatorScreen() {
        super(Component.literal("Create Your Character"));
        this.raceIndex = 0;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 190;
        int right = this.width / 2 + 70;
        int top = Math.max(35, this.height / 2 - 105);

        this.addRenderableWidget(
            Button.builder(Component.literal("< Race"), button -> changeRace(-1))
                .bounds(left, top + 25, 82, 20)
                .build()
        );

        this.raceButton = this.addRenderableWidget(
            Button.builder(Component.literal(currentRace().displayName()), button -> changeRace(1))
                .bounds(left + 86, top + 25, 118, 20)
                .build()
        );

        this.addRenderableWidget(
            Button.builder(Component.literal("Race >"), button -> changeRace(1))
                .bounds(left + 208, top + 25, 82, 20)
                .build()
        );

        this.eyeStyleButton = this.addRenderableWidget(
            Button.builder(Component.empty(), button -> {
                eyeStyle = (eyeStyle + 1) % CharacterAppearance.EYE_STYLE_COUNT;
                refreshLabels();
            })
                .bounds(right, top + 25, 120, 20)
                .build()
        );

        this.eyeColorButton = this.addRenderableWidget(
            Button.builder(Component.empty(), button -> {
                eyeColor = (eyeColor + 1) % CharacterAppearance.EYE_COLOR_COUNT;
                refreshLabels();
            })
                .bounds(right, top + 55, 120, 20)
                .build()
        );

        this.featureButton = this.addRenderableWidget(
            Button.builder(Component.empty(), button -> {
                featureStyle = (featureStyle + 1) % CharacterAppearance.FEATURE_STYLE_COUNT;
                refreshLabels();
            })
                .bounds(right, top + 85, 120, 20)
                .build()
        );

        this.addRenderableWidget(
            Button.builder(Component.literal("Confirm Character"), button -> submit())
                .bounds(this.width / 2 - 75, top + 185, 150, 20)
                .build()
        );

        refreshLabels();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        tileBackground(graphics);

        int left = this.width / 2 - 200;
        int right = this.width / 2 + 55;
        int top = Math.max(25, this.height / 2 - 115);

        graphics.fill(left, top, this.width / 2 + 45, top + 220, 0xB0101010);
        graphics.fill(right, top, right + 145, top + 145, 0xB0101010);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, top + 8, 0xFFFFFF);
        graphics.drawString(this.font, "Race", left + 12, top + 28, 0xE6D39A, false);
        graphics.drawString(this.font, "Appearance", right + 10, top + 8, 0xE6D39A, false);

        Race race = currentRace();
        int infoY = top + 58;
        graphics.drawString(this.font, "Health: " + (int) race.maxHealth(), left + 12, infoY, 0xFFFFFF, false);
        graphics.drawString(this.font, String.format("Scale: %.2fx", race.scale()), left + 12, infoY + 12, 0xFFFFFF, false);
        graphics.drawString(this.font, String.format("Mana: %.0f%%", race.maxManaMultiplier() * 100.0D), left + 12, infoY + 24, 0xFFFFFF, false);
        graphics.drawString(this.font, String.format("Mana Regen: %.0f%%", race.manaRegenMultiplier() * 100.0D), left + 12, infoY + 36, 0xFFFFFF, false);

        if (this.minecraft != null && this.minecraft.player != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(
                graphics,
                this.width / 2 + 3,
                top + 178,
                62,
                (float) (this.width / 2 + 3 - mouseX),
                (float) (top + 110 - mouseY),
                this.minecraft.player
            );
        }

        graphics.drawCenteredString(
            this.font,
            "Your character enters the world only after you confirm.",
            this.width / 2,
            top + 212,
            0xB8B8B8
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
    public boolean isPauseScreen() {
        return false;
    }

    private void changeRace(int direction) {
        Race[] races = Race.values();
        raceIndex = Math.floorMod(raceIndex + direction, races.length);
        featureStyle = 0;
        refreshLabels();
    }

    private void refreshLabels() {
        if (raceButton != null) {
            raceButton.setMessage(Component.literal(currentRace().displayName()));
        }
        if (eyeStyleButton != null) {
            eyeStyleButton.setMessage(Component.literal("Eyes: " + EYE_STYLES[eyeStyle]));
        }
        if (eyeColorButton != null) {
            eyeColorButton.setMessage(Component.literal("Colour: " + EYE_COLORS[eyeColor]));
        }
        if (featureButton != null) {
            featureButton.setMessage(Component.literal(featureLabel()));
        }
    }

    private String featureLabel() {
        String[] labels = switch (currentRace()) {
            case ELF -> new String[] {"Ears: Short", "Ears: Long", "Ears: High"};
            case DWARF -> new String[] {"Build: Stout", "Build: Broad", "Build: Compact"};
            case HALFLING -> new String[] {"Ears: Round", "Ears: Soft", "Ears: Pointed"};
            case ORC -> new String[] {"Tusks: Small", "Tusks: Broad", "Tusks: Long"};
            case GOBLIN -> new String[] {"Ears: Wide", "Ears: Long", "Ears: Swept"};
            case TIEFLING -> new String[] {"Horns: Curved", "Horns: Swept", "Horns: Tall"};
            case DRAGONBORN -> new String[] {"Crest: Horned", "Crest: Crowned", "Crest: Swept"};
            case FAIRY -> new String[] {"Fey: Classic", "Fey: Sharp", "Fey: Soft"};
            case HUMAN -> new String[] {"Style: Classic", "Style: Rugged", "Style: Refined"};
        };
        return labels[featureStyle];
    }

    private Race currentRace() {
        return Race.values()[raceIndex];
    }

    private void submit() {
        CyberRacesNetwork.sendToServer(
            new SubmitCharacterPacket(
                currentRace().id(),
                eyeStyle,
                eyeColor,
                featureStyle
            )
        );
    }

    private void tileBackground(GuiGraphics graphics) {
        for (int y = 0; y < this.height; y += 32) {
            for (int x = 0; x < this.width; x += 32) {
                graphics.blit(BACKGROUND, x, y, 0, 0, 32, 32, 32, 32);
            }
        }
    }
}
