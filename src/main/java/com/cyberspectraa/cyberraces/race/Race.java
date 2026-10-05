package com.cyberspectraa.cyberraces.race;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * V1 balance definitions.
 *
 * Multipliers use 1.0 as normal. The first values are intentionally conservative
 * until the races have been tested against Better Combat and Iron's Spells.
 */
public enum Race {
    HUMAN("human", "Human", 1.00f, 20.0, 1.00, 1.00, 1.00, 1.00, 1.00, 0.00, 0.0, 1.00, FlightType.NONE),
    ELF("elf", "Elf", 1.03f, 18.0, 1.05, 1.00, 1.15, 1.10, 1.00, 0.00, 0.0, 1.00, FlightType.NONE),
    DWARF("dwarf", "Dwarf", 0.82f, 22.0, 0.95, 1.00, 0.90, 1.00, 1.10, 0.20, 1.0, 1.00, FlightType.NONE),
    HALFLING("halfling", "Halfling", 0.72f, 18.0, 1.03, 0.95, 1.00, 1.00, 1.00, 0.00, 0.0, 1.00, FlightType.NONE),
    ORC("orc", "Orc", 1.10f, 24.0, 1.00, 1.15, 0.85, 0.90, 1.00, 0.15, 0.0, 1.00, FlightType.NONE),
    GOBLIN("goblin", "Goblin", 0.75f, 18.0, 1.08, 1.00, 1.00, 1.05, 1.00, -0.05, 0.0, 1.00, FlightType.NONE),
    TIEFLING("tiefling", "Tiefling", 1.00f, 20.0, 1.00, 1.00, 1.15, 1.05, 1.00, 0.00, 0.0, 0.50, FlightType.NONE),
    DRAGONBORN("dragonborn", "Dragonborn", 1.07f, 22.0, 0.98, 1.10, 1.05, 1.00, 1.05, 0.10, 2.0, 0.85, FlightType.NONE),
    FAIRY("fairy", "Fairy", 0.50f, 14.0, 1.10, 0.90, 1.30, 1.20, 0.95, -0.10, 0.0, 1.00, FlightType.ICARUS_NATURAL),
    CATFOLK("catfolk", "Catfolk", 0.96f, 20.0, 1.08, 1.00, 1.00, 1.00, 1.00, 0.00, 0.0, 1.00, FlightType.NONE),
    DOGFOLK("dogfolk", "Dogfolk", 1.02f, 22.0, 1.04, 1.05, 0.95, 1.00, 1.00, 0.08, 0.0, 1.00, FlightType.NONE),
    FOXFOLK("foxfolk", "Foxfolk", 0.96f, 18.0, 1.06, 1.00, 1.10, 1.05, 1.00, 0.00, 0.0, 1.00, FlightType.NONE);

    private final String id;
    private final String displayName;
    private final float scale;
    private final double maxHealth;
    private final double movementMultiplier;
    private final double hungerMultiplier;
    private final double maxManaMultiplier;
    private final double manaRegenMultiplier;
    private final double spellResistanceMultiplier;
    private final double knockbackResistanceBonus;
    private final double armorBonus;
    private final double fireDamageMultiplier;
    private final FlightType flightType;

    Race(
        String id,
        String displayName,
        float scale,
        double maxHealth,
        double movementMultiplier,
        double hungerMultiplier,
        double maxManaMultiplier,
        double manaRegenMultiplier,
        double spellResistanceMultiplier,
        double knockbackResistanceBonus,
        double armorBonus,
        double fireDamageMultiplier,
        FlightType flightType
    ) {
        this.id = id;
        this.displayName = displayName;
        this.scale = scale;
        this.maxHealth = maxHealth;
        this.movementMultiplier = movementMultiplier;
        this.hungerMultiplier = hungerMultiplier;
        this.maxManaMultiplier = maxManaMultiplier;
        this.manaRegenMultiplier = manaRegenMultiplier;
        this.spellResistanceMultiplier = spellResistanceMultiplier;
        this.knockbackResistanceBonus = knockbackResistanceBonus;
        this.armorBonus = armorBonus;
        this.fireDamageMultiplier = fireDamageMultiplier;
        this.flightType = flightType;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public float scale() { return scale; }
    public double maxHealth() { return maxHealth; }
    public double movementMultiplier() { return movementMultiplier; }
    public double hungerMultiplier() { return hungerMultiplier; }
    public double maxManaMultiplier() { return maxManaMultiplier; }
    public double manaRegenMultiplier() { return manaRegenMultiplier; }
    public double spellResistanceMultiplier() { return spellResistanceMultiplier; }
    public double knockbackResistanceBonus() { return knockbackResistanceBonus; }
    public double armorBonus() { return armorBonus; }
    public double fireDamageMultiplier() { return fireDamageMultiplier; }
    public FlightType flightType() { return flightType; }

    public static Optional<Race> byId(String id) {
        if (id == null) {
            return Optional.empty();
        }

        String normalised = id.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
            .filter(race -> race.id.equals(normalised))
            .findFirst();
    }
}
