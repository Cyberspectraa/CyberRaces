package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.SubmitRaceEvolutionPacket;
import com.cyberspectraa.cyberraces.race.RaceEvolution;
import com.cyberspectraa.cyberraces.race.RaceEvolutionStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * Enchanting-table-inspired awakening scene: animated rune halo, framed
 * lineage cards, clear before/after bonuses, and sound on each choice.
 * Everything uses Minecraft's built-in UI primitives and pixel font.
 */
public final class RaceEvolutionScreen extends Screen {
    private final RaceEvolution[] choices;
    private int index;
    private Button choiceButton;
    private boolean answered;
    private int animationTicks;
    private int selectionTicks;

    public RaceEvolutionScreen(String firstEvolutionId) {
        super(Component.literal("Bloodline Awakening"));
        RaceEvolution first = RaceEvolution.byId(firstEvolutionId).orElse(null);
        choices = first == null ? new RaceEvolution[0]
                : RaceEvolution.choicesFor(first.baseRace());
    }

    @Override
    protected void init() {
        if (choices.length == 0) {
            onClose();
            return;
        }
        int x = width / 2;
        int top = frameTop();
        int w = frameWidth();
        int bottom = frameBottom();
        int selectY = top + 89;

        if (choices.length > 1) {
            addRenderableWidget(Button.builder(Component.literal("<"),
                    button -> change(-1))
                    .bounds(x - w / 2 + 16, selectY, 22, 22).build());
            addRenderableWidget(Button.builder(Component.literal(">"),
                    button -> change(1))
                    .bounds(x + w / 2 - 38, selectY, 22, 22).build());
        }

        choiceButton = addRenderableWidget(Button.builder(
                Component.literal(current().displayName()), button -> change(1))
                .bounds(x - (w - 90) / 2, selectY, w - 90, 22).build());
        choiceButton.active = choices.length > 1;

        int gap = 8;
        int buttonWidth = Math.max(86, Math.min(150, (w - 45) / 2));
        addRenderableWidget(Button.builder(Component.literal("Awaken"),
                button -> submit(true))
                .bounds(x - buttonWidth - gap / 2, bottom - 30, buttonWidth, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Not Yet"),
                button -> submit(false))
                .bounds(x + gap / 2, bottom - 30, buttonWidth, 20).build());
    }

    @Override
    public void tick() {
        animationTicks++;
        selectionTicks++;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        if (choices.length == 0) {
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        int cx = width / 2;
        int left = cx - frameWidth() / 2;
        int right = cx + frameWidth() / 2;
        int top = frameTop();
        int bottom = frameBottom();
        RaceEvolution selected = current();
        int glow = evolutionColour(selected);

        // Stone border, parchment inset, and small engraved corner details.
        graphics.fill(left - 2, top - 2, right + 2, bottom + 2, 0xFF201B19);
        graphics.fill(left, top, right, bottom, 0xFF5A4631);
        graphics.fill(left + 3, top + 3, right - 3, bottom - 3, 0xFF2D2924);
        graphics.fill(left + 7, top + 7, right - 7, bottom - 7, 0xFF302C29);
        graphics.fill(left + 10, top + 32, right - 10, top + 33, 0xFF806C4B);
        graphics.fill(left + 10, bottom - 37, right - 10, bottom - 36, 0xFF806C4B);

        graphics.drawCenteredString(font, "BLOODLINE AWAKENING",
                cx, top + 12, 0xFFE8CD91);
        graphics.drawCenteredString(font,
                selected.baseRace().displayName() + "  -  Cyber Level 30",
                cx, top + 39, 0xFFD5C29B);

        drawRunes(graphics, cx, top + 70, glow, partialTick);

        // Glowing name panel sits behind the actual vanilla Minecraft button.
        graphics.fill(cx - (frameWidth() - 82) / 2, top + 86,
                cx + (frameWidth() - 82) / 2, top + 114,
                (glow & 0xFFFFFF) | 0x29000000);

        List<FormattedCharSequence> lines = font.split(
                Component.literal(selected.description()), frameWidth() - 46);
        int y = top + 123;
        for (FormattedCharSequence line : lines) {
            if (y > bottom - 78) break;
            graphics.drawCenteredString(font, line, cx, y, 0xFFE6DDC8);
            y += 10;
        }

        RaceEvolutionStats stats = selected.stats();
        graphics.drawCenteredString(font, "New traits",
                cx, bottom - 78, 0xFFE3C57C);
        String physical = "+" + (int) stats.healthBonus() + " Health"
                + "  |  +" + formatNumber(stats.armorBonus()) + " Armour"
                + "  |  " + percent(stats.movementMultiplier()) + " Speed";
        String arcane = percent(stats.maxManaMultiplier()) + " Mana"
                + "  |  " + percent(stats.manaRegenMultiplier()) + " Regen";
        graphics.drawCenteredString(font, physical,
                cx, bottom - 65, 0xFFD1C7AF);
        graphics.drawCenteredString(font, arcane,
                cx, bottom - 54, 0xFFD1C7AF);

        // Animated glyphs don't move the buttons or text.
        int flicker = (int) (7.0F * Math.sin((animationTicks + partialTick) * 0.12F));
        graphics.fill(left + 13, top + 11, left + 17,
                top + 18 + Math.max(0, flicker / 2), 0xFFD7AB5D);
        graphics.fill(right - 17, top + 11, right - 13,
                top + 18 + Math.max(0, flicker / 2), 0xFFD7AB5D);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawRunes(GuiGraphics graphics, int cx, int cy,
            int color, float partialTick) {
        float time = animationTicks + partialTick;
        double rotation = time * 0.025D;
        // Orbiting runes and an inner ward, drawn as pixel-sized Minecraft
        // enchantment markings rather than a generic glowing web panel.
        for (int i = 0; i < 24; i++) {
            double phase = rotation + i * Math.PI / 12D;
            int x = cx + (int) Math.round(Math.cos(phase) * 85D);
            int y = cy + (int) Math.round(Math.sin(phase) * 13D);
            int shade = i % 3 == 0 ? 0xAAFFFFFF : 0x66FFFFFF;
            graphics.fill(x - 1, y - 1, x + 2, y + 1,
                    (color & 0x00FFFFFF) | (shade & 0xFF000000));
        }
        int radius = 9 + (int) (2 * Math.sin(time * 0.08F));
        graphics.fill(cx - radius, cy - 2, cx + radius + 1, cy + 2,
                0xFF3F3830);
        graphics.fill(cx - radius / 2, cy - 1, cx + radius / 2 + 1, cy + 1,
                0xFF000000 | color);
    }

    private static int evolutionColour(RaceEvolution evolution) {
        return switch (evolution) {
            case HIGH_ELF -> 0xF2D985;
            case DARK_ELF -> 0xA18BDC;
            case ELDER_DWARF -> 0xD9C7A6;
            case HOBGOBLIN -> 0xA6C56D;
            case SKYBORN -> 0x67B8B5;
        };
    }

    private RaceEvolution current() { return choices[index]; }

    private void change(int direction) {
        if (choices.length < 2) return;
        index = Math.floorMod(index + direction, choices.length);
        selectionTicks = 0;
        if (choiceButton != null)
            choiceButton.setMessage(Component.literal(current().displayName()));
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.ENCHANTMENT_TABLE_USE, 0.9F));
    }

    private int frameWidth() { return Math.min(width - 14, 390); }
    private int frameHeight() { return Math.min(height - 14, 242); }
    private int frameTop() { return (height - frameHeight()) / 2; }
    private int frameBottom() { return frameTop() + frameHeight(); }

    private static String percent(double multiplier) {
        long value = Math.round((multiplier - 1.0D) * 100.0D);
        return (value >= 0L ? "+" : "") + value + "%";
    }

    private static String formatNumber(double value) {
        return value == Math.rint(value) ? String.valueOf((int) value)
                : String.format("%.1f", value);
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
