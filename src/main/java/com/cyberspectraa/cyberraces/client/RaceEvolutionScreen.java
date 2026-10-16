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

/** A server-chosen offer after sleeping, not a free evolution selector. */
public final class RaceEvolutionScreen extends Screen {
    private final RaceEvolution proposed;
    private boolean answered;

    public RaceEvolutionScreen(String proposedId) {
        super(Component.literal("Racial Evolution"));
        this.proposed = RaceEvolution.byId(proposedId).orElse(null);
    }

    @Override
    protected void init() {
        if (proposed == null) {
            onClose();
            return;
        }
        int x = width / 2;
        int y = Math.max(16, height / 2 - 122);
        addRenderableWidget(Button.builder(Component.literal("Evolve"), b -> submit(true))
                .bounds(x - 149, y + 207, 140, 22).build());
        addRenderableWidget(Button.builder(Component.literal("Not Yet"), b -> submit(false))
                .bounds(x + 9, y + 207, 140, 22).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        if (proposed == null) {
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }
        int x = width / 2;
        int y = Math.max(16, height / 2 - 122);
        graphics.drawCenteredString(font, title, x, y, 0xF7DBA1);
        graphics.drawCenteredString(font,
                Component.literal("Evolution: " + proposed.displayName()),
                x, y + 24, 0xA7E3D3);

        List<FormattedCharSequence> description =
                font.split(Component.literal(proposed.description()), 302);
        int textY = y + 61;
        for (FormattedCharSequence line : description) {
            graphics.drawCenteredString(font, line, x, textY, 0xE5E5E5);
            textY += 11;
        }

        RaceEvolutionStats stats = proposed.stats();
        graphics.drawCenteredString(font,
                Component.literal("Physical: " + physicalSummary(stats)),
                x, y + 128, 0xA7C8E3);
        graphics.drawCenteredString(font,
                Component.literal("Arcane: " + arcaneSummary(stats)),
                x, y + 143, 0xC6A7E3);
        graphics.drawCenteredString(font,
                Component.literal("Ability affinity: " + prettyProfile(proposed.abilityProfile())),
                x, y + 158, 0xE6C66B);
        graphics.drawCenteredString(font,
                Component.literal("Keep growing or rest again to reconsider."),
                x, y + 183, 0xBDBDBD);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void submit(boolean accept) {
        if (answered) return;
        answered = true;
        CyberRacesNetwork.sendToServer(
                new SubmitRaceEvolutionPacket(accept ? proposed.id() : ""));
        if (!accept) Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void onClose() {
        if (!answered) submit(false);
        super.onClose();
    }

    @Override
    public boolean shouldCloseOnEsc() { return true; }

    @Override
    public boolean isPauseScreen() { return false; }

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
