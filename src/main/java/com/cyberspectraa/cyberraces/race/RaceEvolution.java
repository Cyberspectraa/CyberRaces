package com.cyberspectraa.cyberraces.race;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Only the five supported racial evolution paths.
 * Each is unlocked at Cyber Level 30; there is no activity-point or
 * environment tracking. An Elf chooses between High Elf and Dark Elf.
 */
public enum RaceEvolution {
    HIGH_ELF("high_elf", "High Elf",
        "An elf with stronger mana and greater resistance to spells.",
        Race.ELF, stats(1, 1.01, 1.15, 1.10, 1.08, 0, 0, 0, 1, 1)),
    DARK_ELF("dark_elf", "Dark Elf",
        "An elf with stronger magic and quicker movement.",
        Race.ELF, stats(1, 1.05, 1.10, 1.06, 1.08, 0, 0, 0, 1, 1)),
    ELDER_DWARF("elder_dwarf", "Elder Dwarf",
        "A tougher dwarf with better armour and knockback resistance.",
        Race.DWARF, stats(4, 0.97, 1.02, 1.02, 1.08, 0.10, 2, 0, 1, 1)),
    HOBGOBLIN("hobgoblin", "Hobgoblin",
        "A larger, stronger goblin with extra health and armour.",
        Race.GOBLIN, stats(4, 1.01, 1, 1, 1, 0.06, 1, 0, 1.08, 1)),
    SKYBORN("skyborn", "Winged Dragonborn",
        "Grow dragon wings for flight when Icarus and Curios are installed.",
        Race.DRAGONBORN, stats(2, 1.04, 1.03, 1.03, 1.05, 0.03, 0.5, 0, 1.01, 1));

    public static final int REQUIRED_LEVEL = 30;

    private final String id;
    private final String displayName;
    private final String description;
    private final Race baseRace;
    private final RaceEvolutionStats stats;

    RaceEvolution(String id, String displayName, String description,
            Race baseRace, RaceEvolutionStats stats) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.baseRace = baseRace;
        this.stats = stats;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public String description() { return description; }
    public Race baseRace() { return baseRace; }
    public RaceEvolutionStats stats() { return stats; }

    public boolean availableToEvolve() { return true; }

    public static Optional<RaceEvolution> byId(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        String normalized = id.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values()).filter(e -> e.id.equals(normalized)).findFirst();
    }

    public static RaceEvolution[] choicesFor(Race race) {
        return Arrays.stream(values())
                .filter(e -> e.baseRace == race)
                .toArray(RaceEvolution[]::new);
    }

    private static RaceEvolutionStats stats(
            double health, double speed, double mana, double manaRegen,
            double spellResist, double knockback, double armor,
            double luck, double scale, double fire) {
        return new RaceEvolutionStats(health, speed, mana, manaRegen,
                spellResist, knockback, armor, luck, scale, fire);
    }
}
