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
 * Only used when a race has two evolutions (currently High Elf / Dark Elf).
 * Two vanilla buttons, no giant book, inventory overlay or extra menu.
 * ESC postpones the choice until the next time the player sleeps.
 */
public final class RaceEvolutionScreen extends Screen {
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
        if (choices.length != 2) {
            // This screen must never appear for a single evolution.
            answered = true;
            minecraft.setScreen(null);
            return;
        }
        int gap = 8;
        int buttonWidth = Math.max(82, Math.min(120, (width - 38) / 2));
        int left = width / 2 - buttonWidth - gap / 2;
        int y = height / 2 + 1;

        for (int i = 0; i < choices.length; i++) {
            final RaceEvolution choice = choices[i];
            addRenderableWidget(Button.builder(
                    Component.literal(choice.displayName()),
                    b -> accept(choice))
                    .bounds(left + i * (buttonWidth + gap), y, buttonWidth, 20)
                    .build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Keep the world visible, with a small Minecraft-style choice box.
        // Do not call renderBackground: that creates the full-screen overlay.
        int left = width / 2 - 128;
        int top = height / 2 - 34;
        graphics.fill(left - 2, top - 2, left + 258, top + 76, 0xFF171513);
        graphics.fill(left, top, left + 256, top + 72, 0xFF6B6253);
        graphics.fill(left + 2, top + 2, left + 254, top + 70, 0xE72D2A24);
        graphics.drawCenteredString(font, "Choose your bloodline",
                width / 2, top + 9, 0xFFE3D4AD);
        graphics.drawCenteredString(font, "Esc to decide later",
                width / 2, top + 60, 0xFFAFA58E);
        super.render(graphics, mouseX, mouseY, partialTick);
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
