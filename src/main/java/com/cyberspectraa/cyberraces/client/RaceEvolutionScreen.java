package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.SubmitRaceEvolutionPacket;
import com.cyberspectraa.cyberraces.race.RaceEvolution;
import com.cyberspectraa.cyberraces.race.RaceEvolutionStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * The Awakening: a proper Minecraft deepslate-and-gold ritual scene.
 * A live player portrait, two separately selectable lineage tablets for
 * Elves, circling runes, evolving glyphs, a confirmation sequence and
 * a brief crescendo make the choice feel like an event rather than a form.
 *
 * All visuals are drawn using Minecraft pixel UI primitives (no external
 * fonts, procedural gradients, or mandatory resource packs).
 */
public final class RaceEvolutionScreen extends Screen {
    private static final int GOLD = 0xFFE2C480;
    private static final int CREAM = 0xFFF2E7CF;
    private static final int STONE = 0xFF242321;
    private final RaceEvolution[] paths;
    private int selected;
    private int ticks;
    private int selectionAge;
    private int ritualAge = -1;
    private boolean answered;
    private Button confirm;
    private Button[] pathButtons;

    public RaceEvolutionScreen(String firstEvolutionId) {
        super(Component.literal("The Awakening"));
        RaceEvolution first = RaceEvolution.byId(firstEvolutionId).orElse(null);
        paths = first == null ? new RaceEvolution[0]
                : RaceEvolution.choicesFor(first.baseRace());
    }

    @Override
    protected void init() {
        if (paths.length == 0) {
            onClose();
            return;
        }
        int left = left();
        int rightX = rightX();
        int rightWidth = rightWidth();
        int top = top();
        int paneTop = top + 53;
        int cardHeight = Math.max(21, Math.min(31, panelHeight() / 9));

        pathButtons = new Button[paths.length];
        for (int i = 0; i < paths.length; i++) {
            final int choice = i;
            int y = paneTop + i * (cardHeight + 7);
            pathButtons[i] = addRenderableWidget(
                Button.builder(Component.literal(paths[i].displayName()),
                        button -> select(choice))
                    .bounds(rightX + 8, y + 3, rightWidth - 16,
                            cardHeight - 6).build()
            );
        }

        int bottom = top + panelHeight();
        confirm = addRenderableWidget(Button.builder(
                Component.literal("Awaken"), button -> beginRitual())
                .bounds(rightX + 8, bottom - 29,
                        Math.max(66, (rightWidth - 26) * 2 / 3), 20)
                .build());
        addRenderableWidget(Button.builder(
                Component.literal("Later"), button -> decline())
                .bounds(rightX + 14 + Math.max(66, (rightWidth - 26) * 2 / 3),
                        bottom - 29,
                        Math.max(48, rightWidth - 27
                            - Math.max(66, (rightWidth - 26) * 2 / 3)), 20)
                .build());
    }

    @Override
    public void tick() {
        ticks++;
        selectionAge++;
        if (ritualAge >= 0) {
            ritualAge++;
            if (ritualAge >= 30) finish(true);
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        renderBackground(g);
        if (paths.length == 0) {
            super.render(g, mx, my, partial);
            return;
        }
        int l = left(), t = top(), w = panelWidth(), h = panelHeight();
        int rx = rightX(), rw = rightWidth(), stage = rx - l;
        RaceEvolution path = paths[selected];
        int accent = accent(path);
        float time = ticks + partial;

        // The frame is cut from deepslate, with a copper-gold inset and
        // dark book-paper panels rather than a translucent floating HUD.
        g.fill(l - 3, t - 3, l + w + 3, t + h + 3, 0xFF10100F);
        g.fill(l, t, l + w, t + h, 0xFF594D39);
        g.fill(l + 3, t + 3, l + w - 3, t + h - 3, STONE);
        g.fill(l + 6, t + 5, l + w - 6, t + 6, 0xFFAD8853);
        g.fill(l + 6, t + 32, l + w - 6, t + 33, 0xFF554935);
        g.fill(rx - 5, t + 37, rx - 4, t + h - 7, 0xFF554935);

        g.drawCenteredString(font, "THE AWAKENING",
                l + w / 2, t + 11, GOLD);
        g.drawCenteredString(font,
                "A new chapter begins",
                l + w / 2, t + 23, 0xFFA99B82);

        renderStage(g, l + 8, t + 41, stage - 16, h - 51,
                path, accent, time, mx, my);
        renderChoices(g, rx, rw, t, h, path, accent, time);

        // Vanilla widgets remain visible, focused, keyboard accessible.
        super.render(g, mx, my, partial);
        if (ritualAge >= 0) renderRitualFlash(g, l, t, w, h, accent);
    }

    private void renderStage(GuiGraphics g, int x, int y, int width, int height,
            RaceEvolution path, int accent, float time, int mx, int my) {
        int cx = x + width / 2;
        g.fill(x, y, x + width, y + height, 0xFF171917);
        g.fill(x + 2, y + 2, x + width - 2, y + height - 2, 0xFF1E2422);
        g.fill(x + 3, y + 3, x + width - 3, y + 4, 0xFF4B4A37);
        g.fill(x + 3, y + height - 4, x + width - 3, y + height - 3, 0xFF4B4A37);

        int heartY = y + Math.max(32, height / 3);
        int intensity = ritualAge < 0 ? 0 : Math.min(28, ritualAge);
        int radius = 26 + intensity / 2;
        // Enchantment glimmers travel around the player, and change colour
        // with the selected ancestry. Each rune remains pixel-shaped.
        for (int i = 0; i < 32; i++) {
            double theta = (i * Math.PI * 2 / 32.0) + time * 0.016;
            int px = cx + (int) Math.round(Math.cos(theta) * radius);
            int py = heartY + (int) Math.round(Math.sin(theta) * (height / 4.5));
            if (px > x + 4 && px < x + width - 4
                    && py > y + 5 && py < y + height - 8) {
                int color = ((i + ticks / 4) % 5 == 0)
                        ? 0xFFE8D9B1 : (0xFF000000 | accent);
                g.fill(px, py, px + 2, py + 2, color);
            }
        }
        for (int i = 0; i < 13; i++) {
            int seed = i * 37 + 19;
            int px = x + 8 + Math.floorMod(seed, Math.max(9, width - 16));
            int py = y + 8 + Math.floorMod(
                    (seed * 3 - ticks * (1 + i % 3) / 3),
                    Math.max(12, height - 17));
            g.fill(px, py, px + 1, py + 2,
                    i % 3 == 0 ? 0xFFD0BC86 : 0xFF6A7A70);
        }
        g.fill(cx - width / 3, y + height - 25, cx + width / 3,
                y + height - 23, 0xFF76654A);
        g.fill(cx - width / 4, y + height - 27, cx + width / 4,
                y + height - 25, 0xFF9A8152);

        if (minecraft != null && minecraft.player != null) {
            boolean invisible = minecraft.player.isInvisible();
            minecraft.player.setInvisible(false);
            int scale = Math.max(24, Math.min(width / 2, height / 3));
            try {
                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        g, cx, y + height - 30, scale,
                        (float) (cx - mx), (float) (heartY - my),
                        minecraft.player);
            } finally {
                minecraft.player.setInvisible(invisible);
            }
        }
        g.drawCenteredString(font, "YOUR BLOODLINE", cx,
                y + height - 14, 0xFFB2A58C);
    }

    private void renderChoices(GuiGraphics g, int rx, int width, int t, int height,
            RaceEvolution path, int accent, float time) {
        int line = t + 40;
        g.drawString(font, "CHOOSE YOUR PATH", rx + 8, line,
                GOLD, false);
        int cardHeight = Math.max(21, Math.min(31, panelHeight() / 9));
        for (int i = 0; i < paths.length; i++) {
            int y = t + 53 + i * (cardHeight + 7);
            boolean active = i == selected;
            int edge = active ? 0xFFBDA06B : 0xFF665947;
            g.fill(rx + 5, y, rx + width - 5,
                    y + cardHeight, edge);
            g.fill(rx + 7, y + 2, rx + width - 7,
                    y + cardHeight - 2, active ? 0xFF4A4134 : 0xFF292722);
            if (active) {
                g.fill(rx + 6, y + 2, rx + 9, y + cardHeight - 2,
                        0xFF000000 | accent);
            }
        }

        // Lower half changes when selecting High versus Dark Elf.
        int infoY = t + 55 + paths.length * (cardHeight + 7) + 6;
        int room = t + height - 37 - infoY;
        boolean showStats = height >= 254;
        g.fill(rx + 7, infoY - 5, rx + width - 7, infoY - 4,
                0xFF75664B);
        g.drawString(font, "BECOME " + path.displayName().toUpperCase(),
                rx + 8, infoY, 0xFF000000 | accent, false);

        List<FormattedCharSequence> description = font.split(
                Component.literal(path.description()), width - 18);
        int dy = infoY + 16;
        for (FormattedCharSequence row : description) {
            if (dy + 10 >= (showStats
                    ? t + height - 81 : t + height - 42)) break;
            g.drawString(font, row, rx + 8, dy, CREAM, false);
            dy += 11;
        }
        if (showStats && room > 54) {
            drawTrait(g, rx + 8, t + height - 73,
                    width - 18, path.stats(), accent);
        }
        g.fill(rx + 8, t + height - 36, rx + width - 8,
                t + height - 35, 0xFF75664B);
        if (ritualAge >= 0) {
            String progress = ritualAge < 18
                    ? "The bloodline stirs..." : "Awakening!";
            g.drawCenteredString(font, progress,
                    rx + width / 2, t + height - 49, GOLD);
        }
    }

    private void drawTrait(GuiGraphics g, int x, int y, int width,
            RaceEvolutionStats stats, int accent) {
        g.drawString(font, "YOUR NEW STRENGTH", x, y, GOLD, false);
        String first = "+" + compact(stats.healthBonus()) + " health"
                + "   +" + compact(stats.armorBonus()) + " armour";
        g.drawString(font, first, x, y + 12, CREAM, false);
        String second = formatPercent(stats.movementMultiplier()) + " speed"
                + "   " + formatPercent(stats.maxManaMultiplier()) + " mana";
        g.drawString(font, second, x, y + 23, CREAM, false);
    }

    private void renderRitualFlash(GuiGraphics g,
            int l, int t, int w, int h, int accent) {
        int phase = ritualAge;
        int flash = phase < 15 ? phase * 7 : Math.max(0, 210 - phase * 6);
        int alpha = Math.max(0, Math.min(210, flash));
        g.fill(l + 5, t + 5, l + w - 5, t + h - 5,
                (alpha << 24) | (accent & 0xFFFFFF));
        int cx = l + w / 2, cy = t + h / 2;
        int reach = phase * 7;
        g.fill(cx - reach, cy - 1, cx + reach, cy + 1, GOLD);
        g.fill(cx - 1, cy - reach / 2, cx + 1, cy + reach / 2, GOLD);
        g.drawCenteredString(font, "AWAKENING",
                cx, cy - 20, CREAM);
    }

    private void select(int choice) {
        if (answered || ritualAge >= 0 || choice == selected) return;
        selected = choice;
        selectionAge = 0;
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.ENCHANTMENT_TABLE_USE,
                        0.75F + 0.1F * choice));
    }

    private void beginRitual() {
        if (answered || ritualAge >= 0) return;
        ritualAge = 0;
        confirm.active = false;
        for (Button button : pathButtons) button.active = false;
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.BEACON_ACTIVATE, 1.0F));
    }

    private void decline() {
        if (ritualAge >= 0) return;
        finish(false);
    }

    private void finish(boolean accept) {
        if (answered || paths.length == 0) return;
        answered = true;
        CyberRacesNetwork.sendToServer(new SubmitRaceEvolutionPacket(
                accept ? paths[selected].id() : ""));
        if (!accept) Minecraft.getInstance().setScreen(null);
    }

    private static String compact(double value) {
        return value == Math.rint(value) ? Integer.toString((int) value)
                : String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private static String formatPercent(double value) {
        long percent = Math.round((value - 1.0D) * 100.0D);
        return (percent >= 0 ? "+" : "") + percent + "%";
    }

    private static int accent(RaceEvolution path) {
        return switch (path) {
            case HIGH_ELF -> 0xD5C98F;
            case DARK_ELF -> 0x9D7ABA;
            case ELDER_DWARF -> 0xCF9F68;
            case HOBGOBLIN -> 0x7EA652;
            case SKYBORN -> 0xC36F5E;
        };
    }

    private int panelWidth() { return Math.min(width - 18, 548); }
    private int panelHeight() { return Math.min(height - 16, 296); }
    private int left() { return (width - panelWidth()) / 2; }
    private int top() { return (height - panelHeight()) / 2; }
    private int rightX() { return left() + Math.max(120, panelWidth() / 3); }
    private int rightWidth() { return left() + panelWidth() - rightX() - 7; }

    @Override
    public void onClose() {
        if (!answered && ritualAge < 0) finish(false);
        super.onClose();
    }
    @Override
    public boolean shouldCloseOnEsc() { return ritualAge < 0; }
    @Override
    public boolean isPauseScreen() { return false; }
}
