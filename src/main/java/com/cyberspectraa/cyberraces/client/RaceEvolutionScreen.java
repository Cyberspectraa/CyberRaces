package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.SubmitRaceEvolutionPacket;
import com.cyberspectraa.cyberraces.race.RaceEvolution;
import com.cyberspectraa.cyberraces.race.RaceEvolutionStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/** Shows all allowed evolution choices for the player's race. */
public final class RaceEvolutionScreen extends Screen {
    private final RaceEvolution[] choices;
    private int index;
    private Button choiceButton;
    private boolean answered;

    public RaceEvolutionScreen(String firstEvolutionId) {
        super(Component.literal("Choose Evolution"));
        RaceEvolution first = RaceEvolution.byId(firstEvolutionId).orElse(null);
        this.choices = first == null
                ? new RaceEvolution[0]
                : RaceEvolution.choicesFor(first.baseRace());
    }

    @Override
    protected void init() {
        if (choices.length == 0) {
            onClose();
            return;
        }
        int x = width / 2;
        int y = Math.max(16, height / 2 - 122);

        if (choices.length > 1) {
            addRenderableWidget(Button.builder(Component.literal("<"),
                    button -> change(-1))
                    .bounds(x - 150, y + 42, 26, 20).build());
            addRenderableWidget(Button.builder(Component.literal(">"),
                    button -> change(1))
                    .bounds(x + 124, y + 42, 26, 20).build());
        }

        choiceButton = addRenderableWidget(Button.builder(
                Component.literal(current().displayName()),
                button -> change(1))
                .bounds(x - 120, y + 42, 240, 20).build());
        choiceButton.active = choices.length > 1;

        addRenderableWidget(Button.builder(Component.literal("Evolve"),
                button -> submit(true))
                .bounds(x - 149, y + 207, 140, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Not Yet"),
                button -> submit(false))
                .bounds(x + 9, y + 207, 140, 22).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        if (choices.length == 0) {
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }
        int x = width / 2;
        int y = Math.max(16, height / 2 - 122);
        RaceEvolution selected = current();
        graphics.drawCenteredString(font, title, x, y, 0xF7DBA1);
        graphics.drawCenteredString(font,
                Component.literal("Cyber Level 30 — " + selected.baseRace().displayName()),
                x, y + 23, 0xA7E3D3);

        List<FormattedCharSequence> description =
                font.split(Component.literal(selected.description()), 286);
        int textY = y + 78;
        for (FormattedCharSequence line : description) {
            graphics.drawString(font, line,
                    x - font.width(line) / 2, textY, 0xE5E5E5, false);
            textY += 11;
        }

        RaceEvolutionStats stats = selected.stats();
        graphics.drawCenteredString(font, Component.literal(
                "Health +" + Math.round(stats.healthBonus())
                        + "  Armour +" + stats.armorBonus()),
                x, y + 133, 0xA7C8E3);
        graphics.drawCenteredString(font, Component.literal(
                "Speed " + percent(stats.movementMultiplier())
                        + "  Mana " + percent(stats.maxManaMultiplier())),
                x, y + 151, 0xC6A7E3);

        graphics.drawCenteredString(font, Component.literal(
                choices.length > 1 ? "Choose your path, or decide later."
                        : "Evolve now, or decide later."),
                x, y + 182, 0xBDBDBD);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private RaceEvolution current() {
        return choices[index];
    }

    private void change(int direction) {
        if (choices.length < 2) return;
        index = Math.floorMod(index + direction, choices.length);
        if (choiceButton != null) {
            choiceButton.setMessage(Component.literal(current().displayName()));
        }
    }

    private static String percent(double multiplier) {
        long value = Math.round((multiplier - 1.0D) * 100.0D);
        return (value >= 0L ? "+" : "") + value + "%";
    }

    private void submit(boolean accept) {
        if (answered || choices.length == 0) return;
        answered = true;
        CyberRacesNetwork.sendToServer(
                new SubmitRaceEvolutionPacket(accept ? current().id() : ""));
        if (!accept) Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void onClose() {
        if (!answered && choices.length > 0) submit(false);
        super.onClose();
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }

    @Override
    public boolean isPauseScreen() { return false; }
}
