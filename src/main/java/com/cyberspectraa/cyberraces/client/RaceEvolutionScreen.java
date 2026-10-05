package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.SubmitRaceEvolutionPacket;
import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceEvolution;
import com.cyberspectraa.cyberraces.race.RaceEvolutionStats;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public final class RaceEvolutionScreen extends Screen {
    private final Race baseRace;
    private final RaceEvolution[] choices;

    private int evolutionIndex;
    private Button evolutionButton;

    public RaceEvolutionScreen(String baseRaceId) {
        super(Component.literal("Choose Race Evolution"));
        this.baseRace = Race.byId(baseRaceId).orElse(Race.HUMAN);
        this.choices = RaceEvolution.choicesFor(this.baseRace);
    }

    @Override
    protected void init() {
        if (choices.length == 0) {
            onClose();
            return;
        }

        int centerX = width / 2;
        int top = Math.max(18, height / 2 - 122);

        addRenderableWidget(
            Button.builder(Component.literal("<"), button -> changeEvolution(-1))
                .bounds(centerX - 150, top + 45, 28, 20)
                .build()
        );

        evolutionButton = addRenderableWidget(
            Button.builder(
                    Component.literal(currentEvolution().displayName()),
                    button -> changeEvolution(1)
                )
                .bounds(centerX - 116, top + 45, 232, 20)
                .build()
        );

        addRenderableWidget(
            Button.builder(Component.literal(">"), button -> changeEvolution(1))
                .bounds(centerX + 122, top + 45, 28, 20)
                .build()
        );

        addRenderableWidget(
            Button.builder(Component.literal("Choose Evolution"), button -> submit())
                .bounds(centerX - 80, top + 205, 160, 22)
                .build()
        );
    }

    @Override
    public void render(
        GuiGraphics graphics,
        int mouseX,
        int mouseY,
        float partialTick
    ) {
        renderBackground(graphics);

        if (choices.length == 0) {
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        int centerX = width / 2;
        int top = Math.max(18, height / 2 - 122);
        RaceEvolution evolution = currentEvolution();

        graphics.drawCenteredString(font, title, centerX, top, 0xFFFFFF);
        graphics.drawCenteredString(
            font,
            Component.literal(baseRace.displayName() + " — Cyber Level 30 Evolution"),
            centerX,
            top + 18,
            0xA7E3D3
        );

        List<FormattedCharSequence> description =
            font.split(Component.literal(evolution.description()), 310);

        int textY = top + 80;
        for (FormattedCharSequence line : description) {
            graphics.drawCenteredString(font, line, centerX, textY, 0xE5E5E5);
            textY += 11;
        }

        RaceEvolutionStats stats = evolution.stats();

        graphics.drawCenteredString(
            font,
            Component.literal("Physical: " + physicalSummary(stats)),
            centerX,
            top + 130,
            0xA7C8E3
        );

        graphics.drawCenteredString(
            font,
            Component.literal("Arcane: " + arcaneSummary(stats)),
            centerX,
            top + 145,
            0xC6A7E3
        );

        graphics.drawCenteredString(
            font,
            Component.literal("Evolution ability profile: " + prettyProfile(evolution.abilityProfile())),
            centerX,
            top + 160,
            0xE6C66B
        );

        graphics.drawCenteredString(
            font,
            Component.literal("Race Ascension unlocks later at Cyber Level 60"),
            centerX,
            top + 180,
            0xBDBDBD
        );

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void changeEvolution(int direction) {
        evolutionIndex = Math.floorMod(
            evolutionIndex + direction,
            choices.length
        );

        if (evolutionButton != null) {
            evolutionButton.setMessage(
                Component.literal(currentEvolution().displayName())
            );
        }
    }

    private RaceEvolution currentEvolution() {
        return choices[evolutionIndex];
    }

    private void submit() {
        CyberRacesNetwork.sendToServer(
            new SubmitRaceEvolutionPacket(currentEvolution().id())
        );
    }

    private static String physicalSummary(RaceEvolutionStats stats) {
        StringBuilder builder = new StringBuilder();

        if (Math.abs(stats.healthBonus()) > 0.001D) {
            append(builder, signed(stats.healthBonus()) + " health");
        }
        if (Math.abs(stats.movementMultiplier() - 1.0D) > 0.001D) {
            append(builder, percent(stats.movementMultiplier()) + " speed");
        }
        if (Math.abs(stats.armorBonus()) > 0.001D) {
            append(builder, signed(stats.armorBonus()) + " armour");
        }
        if (Math.abs(stats.knockbackResistanceBonus()) > 0.001D) {
            append(builder, signedPercent(stats.knockbackResistanceBonus()) + " knockback resist");
        }
        if (Math.abs(stats.luckBonus()) > 0.001D) {
            append(builder, signed(stats.luckBonus()) + " luck");
        }
        if (Math.abs(stats.scaleMultiplier() - 1.0D) > 0.001D) {
            append(builder, percent(stats.scaleMultiplier()) + " size");
        }

        return builder.isEmpty() ? "specialised racial traits" : builder.toString();
    }

    private static String arcaneSummary(RaceEvolutionStats stats) {
        StringBuilder builder = new StringBuilder();

        if (Math.abs(stats.maxManaMultiplier() - 1.0D) > 0.001D) {
            append(builder, percent(stats.maxManaMultiplier()) + " mana affinity");
        }
        if (Math.abs(stats.manaRegenMultiplier() - 1.0D) > 0.001D) {
            append(builder, percent(stats.manaRegenMultiplier()) + " mana regen");
        }
        if (Math.abs(stats.spellResistanceMultiplier() - 1.0D) > 0.001D) {
            append(builder, percent(stats.spellResistanceMultiplier()) + " spell resist");
        }

        return builder.isEmpty() ? "no additional magic affinity" : builder.toString();
    }

    private static String signed(double value) {
        return (value >= 0 ? "+" : "") + String.format("%.1f", value);
    }

    private static String percent(double multiplier) {
        double value = (multiplier - 1.0D) * 100.0D;
        return (value >= 0 ? "+" : "") + Math.round(value) + "%";
    }

    private static String signedPercent(double value) {
        double percent = value * 100.0D;
        return (percent >= 0 ? "+" : "") + Math.round(percent) + "%";
    }

    private static String prettyProfile(String value) {
        if (value == null || value.isBlank()) {
            return "Standard";
        }

        String[] parts = value.split("_");
        StringBuilder builder = new StringBuilder();

        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)))
                .append(part.substring(1));
        }

        return builder.toString();
    }

    private static void append(StringBuilder builder, String value) {
        if (!builder.isEmpty()) {
            builder.append(", ");
        }
        builder.append(value);
    }
}
