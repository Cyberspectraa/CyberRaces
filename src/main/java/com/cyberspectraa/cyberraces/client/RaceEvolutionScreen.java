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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;
import java.util.Locale;

/**
 * Vanilla-styled awakening: uses Minecraft's real book page and enchanting
 * table textures, original Minecraft buttons, a live character portrait and
 * small enchanting motes. No custom UI framework, font or raster backdrop.
 *
 * The server is responsible for qualifying the player and offering a choice.
 */
public final class RaceEvolutionScreen extends Screen {
    private static final ResourceLocation BOOK =
            new ResourceLocation("minecraft", "textures/gui/book.png");
    private static final ResourceLocation ENCHANT =
            new ResourceLocation("minecraft", "textures/gui/container/enchanting_table.png");

    private static final int INK = 0x392915;
    private static final int FADED_INK = 0x756445;
    private static final int TITLE = 0xF0D28D;

    private final RaceEvolution[] options;
    private Button[] optionButtons = new Button[0];
    private Button awakenButton;
    private Button laterButton;
    private int selection;
    private int age;
    private int ritualAge = -1;
    private boolean answered;

    public RaceEvolutionScreen(String firstEvolutionId) {
        super(Component.literal("Awakening"));
        RaceEvolution first = RaceEvolution.byId(firstEvolutionId).orElse(null);
        options = first == null ? new RaceEvolution[0]
                : RaceEvolution.choicesFor(first.baseRace());
    }

    @Override
    protected void init() {
        if (options.length == 0) {
            // Don't send a decline packet if the server sent an invalid ID.
            answered = true;
            minecraft.setScreen(null);
            return;
        }

        int book = bookX();
        int y = bookY();
        int rowHeight = 22;
        optionButtons = new Button[options.length];
        for (int i = 0; i < options.length; i++) {
            final int index = i;
            optionButtons[i] = addRenderableWidget(Button.builder(
                            Component.literal(options[i].displayName()),
                            button -> select(index))
                    .bounds(book + 34, y + 43 + i * (rowHeight + 4), 124, rowHeight)
                    .build());
        }

        awakenButton = addRenderableWidget(Button.builder(
                        Component.literal("Awaken"),
                        button -> beginRitual())
                .bounds(book + 34, y + 164, 59, 20)
                .build());
        laterButton = addRenderableWidget(Button.builder(
                        Component.literal("Later"),
                        button -> defer())
                .bounds(book + 99, y + 164, 59, 20)
                .build());
    }

    @Override
    public void tick() {
        age++;
        if (ritualAge >= 0 && !answered) {
            ritualAge++;
            if (ritualAge >= 27) {
                finish(true);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        if (options.length == 0) return;

        int bx = bookX();
        int by = bookY();
        boolean stage = showStage();

        if (stage) {
            renderEnchantingStage(graphics, stageX(), by + 12, partialTick,
                    mouseX, mouseY);
        }

        // An actual vanilla book page, not a painted faux-parchment box.
        graphics.blit(BOOK, bx, by, 0, 0, 192, 192, 256, 256);

        graphics.drawCenteredString(font, "Bloodline", bx + 96, by + 17, INK);
        graphics.drawCenteredString(font, "Awakening", bx + 96, by + 27, FADED_INK);

        for (int i = 0; i < options.length; i++) {
            if (i == selection) {
                int row = by + 47 + i * 26;
                graphics.fill(bx + 27, row, bx + 30, row + 14,
                        0xFF9B763C);
            }
        }

        RaceEvolution current = options[selection];
        int descriptionY = by + (options.length == 1 ? 81 : 96);
        graphics.drawCenteredString(font, current.displayName(),
                bx + 96, descriptionY, INK);

        List<FormattedCharSequence> lines = font.split(
                Component.literal(current.description()), 119);
        int textY = descriptionY + 12;
        for (FormattedCharSequence line : lines) {
            if (textY > by + 132) break;
            graphics.drawString(font, line, bx + 36, textY, INK, false);
            textY += 10;
        }

        if (ritualAge >= 0) {
            graphics.drawCenteredString(font, "Awakening...",
                    bx + 96, by + 144, 0x7D5123);
        } else {
            RaceEvolutionStats stats = current.stats();
            String health = "+" + compact(stats.healthBonus()) + " Health";
            String armour = "+" + compact(stats.armorBonus()) + " Armour";
            graphics.drawString(font, health + "  " + armour,
                    bx + 37, by + 145, FADED_INK, false);
            graphics.drawString(font,
                    percentage(stats.movementMultiplier()) + " Speed  "
                            + percentage(stats.maxManaMultiplier()) + " Mana",
                    bx + 37, by + 155, FADED_INK, false);
        }

        if (ritualAge >= 0) {
            renderAwakeningMotes(graphics, bx, by, current);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderEnchantingStage(GuiGraphics graphics, int x, int y,
            float partialTick, int mouseX, int mouseY) {
        graphics.blit(ENCHANT, x, y, 0, 0, 176, 166, 256, 256);

        // Use vanilla enchanting-table highlights for the player's stage.
        int centreX = x + 88;
        for (int i = 0; i < 16; i++) {
            int tick = age + i * 11;
            int px = x + 35 + Math.floorMod(i * 23 + tick / 5, 104);
            int py = y + 23 + Math.floorMod(i * 19 - tick, 108);
            int colour = i % 3 == 0 ? 0xFFD8C481 : 0xFF6CBDB7;
            graphics.fill(px, py, px + 1, py + 2, colour);
        }

        if (minecraft.player != null) {
            boolean wasInvisible = minecraft.player.isInvisible();
            minecraft.player.setInvisible(false);
            try {
                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        graphics, centreX, y + 148, 46,
                        (float) (centreX - mouseX),
                        (float) (y + 75 - mouseY),
                        minecraft.player);
            } finally {
                minecraft.player.setInvisible(wasInvisible);
            }
        }

        graphics.drawCenteredString(font, "Your Character",
                centreX, y + 11, TITLE);
    }

    private void renderAwakeningMotes(GuiGraphics graphics, int bx,
            int by, RaceEvolution choice) {
        int rgb = switch (choice) {
            case HIGH_ELF -> 0xE0C983;
            case DARK_ELF -> 0xA68ACC;
            case ELDER_DWARF -> 0xBF965B;
            case HOBGOBLIN -> 0x9AAA6B;
            case SKYBORN -> 0xC47761;
        };

        for (int i = 0; i < 26; i++) {
            int step = ritualAge + i * 13;
            int px = bx + 39 + Math.floorMod(i * 17 + step * 2, 113);
            int py = by + 31 + Math.floorMod(i * 11 - step * 3, 119);
            int alpha = Math.min(255, 100 + ritualAge * 5);
            graphics.fill(px, py, px + 2, py + 2,
                    (alpha << 24) | rgb);
        }
    }

    private void select(int next) {
        if (answered || ritualAge >= 0 || next == selection) return;
        selection = next;
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.ENCHANTMENT_TABLE_USE, 0.85F));
    }

    private void beginRitual() {
        if (answered || ritualAge >= 0) return;
        ritualAge = 0;
        awakenButton.active = false;
        laterButton.active = false;
        for (Button b : optionButtons) b.active = false;
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(SoundEvents.BEACON_ACTIVATE, 1.1F));
    }

    private void defer() {
        if (ritualAge >= 0) return;
        finish(false);
    }

    private void finish(boolean accepted) {
        if (answered || options.length == 0) return;
        answered = true;
        CyberRacesNetwork.sendToServer(new SubmitRaceEvolutionPacket(
                accepted ? options[selection].id() : ""));
        if (!accepted) Minecraft.getInstance().setScreen(null);
    }

    private static String compact(double amount) {
        return amount == Math.rint(amount)
                ? Integer.toString((int) amount)
                : String.format(Locale.ROOT, "%.1f", amount);
    }

    private static String percentage(double multiplier) {
        int value = (int) Math.round((multiplier - 1.0D) * 100);
        return (value >= 0 ? "+" : "") + value + "%";
    }

    private boolean showStage() { return width >= 385; }
    private int bookX() {
        return showStage() ? (width - 380) / 2 + 183 : (width - 192) / 2;
    }
    private int stageX() { return (width - 380) / 2 + 2; }
    private int bookY() { return (height - 194) / 2; }

    @Override
    public void onClose() {
        if (!answered && ritualAge < 0) finish(false);
        super.onClose();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return ritualAge < 0;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
