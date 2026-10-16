package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.SubmitRaceEvolutionPacket;
import com.cyberspectraa.cyberraces.race.RaceEvolution;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * Small journal-inspired two-choice menu. This source is compiled by Forge
 * with the official mappings, so init/render/onClose get remapped correctly.
 * The one-path evolutions never open a menu.
 */
public final class RaceEvolutionScreen extends Screen {
    private static final int PANEL_WIDTH = 280;
    private static final int PANEL_HEIGHT = 84;
    private static final int WOOD_DARK = 0xFF512D1B;
    private static final int WOOD = 0xFF8A552D;
    private static final int PARCHMENT = 0xFFE7D6B4;
    private static final int PARCHMENT_LIGHT = 0xFFF1E6CB;
    private static final int INK = 0xFF4A321F;
    private static final int INK_MUTED = 0xFF755C3D;
    private static final int WAX = 0xFF893A31;

    private final RaceEvolution[] choices;
    private boolean answered;

    public RaceEvolutionScreen(String firstEvolutionId) {
        super(Component.literal("Choose Your Bloodline"));
        RaceEvolution first = RaceEvolution.byId(firstEvolutionId).orElse(null);
        choices = first == null ? new RaceEvolution[0]
                : RaceEvolution.choicesFor(first.baseRace());
    }

    @Override
    protected void init() {
        // Only Elves have multiple paths. One-path races evolve automatically.
        if (choices.length != 2) {
            answered = true;
            minecraft.setScreen(null);
            return;
        }

        int widthPerButton = Math.max(72, Math.min(110, (width - 48) / 2));
        int gap = 12;
        int firstX = width / 2 - widthPerButton - gap / 2;
        int secondX = width / 2 + gap / 2;
        int buttonY = panelTop() + 40;

        addRenderableWidget(Button.builder(
                Component.literal(choices[0].displayName()),
                button -> accept(choices[0]))
                .bounds(firstX, buttonY, widthPerButton, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal(choices[1].displayName()),
                button -> accept(choices[1]))
                .bounds(secondX, buttonY, widthPerButton, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Deliberately no renderBackground(), leaving the world visible.
        int left = width / 2 - PANEL_WIDTH / 2;
        int top = panelTop();
        int right = left + PANEL_WIDTH;
        int bottom = top + PANEL_HEIGHT;

        // A shallow wood frame and parchment inset echo CyberQuest's journal.
        graphics.fill(left - 3, top - 3, right + 3, bottom + 3, 0xB0100D0A);
        graphics.fill(left, top, right, bottom, WOOD_DARK);
        graphics.fill(left + 2, top + 2, right - 2, bottom - 2, WOOD);
        graphics.fill(left + 5, top + 5, right - 5, bottom - 5, PARCHMENT);
        graphics.fill(left + 7, top + 7, right - 7, bottom - 7, PARCHMENT_LIGHT);

        // Small stitched wax marks and a double parchment rule.
        for (int y = top + 14; y <= bottom - 20; y += 11) {
            graphics.fill(left + 12, y, left + 15, y + 7, WAX);
            graphics.fill(right - 15, y, right - 12, y + 7, WAX);
        }
        graphics.fill(left + 24, top + 33, right - 24, top + 34, 0xFFB79D72);
        graphics.fill(left + 24, top + 35, right - 24, top + 36, 0xFFF9EFDA);

        // Journal ink is drawn without artificial drop shadows.
        Component title = Component.literal("Choose your bloodline");
        graphics.drawString(font, title,
                width / 2 - font.width(title) / 2, top + 12, INK, false);
        Component hint = Component.literal("High Elf or Dark Elf");
        graphics.drawString(font, hint,
                width / 2 - font.width(hint) / 2, top + 24, INK_MUTED, false);
        Component later = Component.literal("Esc to decide later");
        graphics.drawString(font, later,
                width / 2 - font.width(later) / 2, bottom - 15, INK_MUTED, false);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private int panelTop() {
        return height / 2 - PANEL_HEIGHT / 2;
    }

    private void accept(RaceEvolution choice) {
        if (answered) return;
        answered = true;
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.ENCHANTMENT_TABLE_USE, 1.05F));
        CyberRacesNetwork.sendToServer(new SubmitRaceEvolutionPacket(choice.id()));
        minecraft.setScreen(null);
    }

    @Override
    public void onClose() {
        if (!answered && choices.length == 2) {
            answered = true;
            CyberRacesNetwork.sendToServer(new SubmitRaceEvolutionPacket(""));
        }
        super.onClose();
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }

    @Override
    public boolean isPauseScreen() { return false; }
}
