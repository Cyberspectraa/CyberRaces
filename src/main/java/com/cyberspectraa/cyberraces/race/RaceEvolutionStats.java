package com.cyberspectraa.cyberraces.race;

public record RaceEvolutionStats(
    double healthBonus,
    double movementMultiplier,
    double maxManaMultiplier,
    double manaRegenMultiplier,
    double spellResistanceMultiplier,
    double knockbackResistanceBonus,
    double armorBonus,
    double luckBonus,
    double scaleMultiplier,
    double fireDamageMultiplier
) {
    public static final RaceEvolutionStats NEUTRAL =
        new RaceEvolutionStats(
            0.0D,
            1.0D,
            1.0D,
            1.0D,
            1.0D,
            0.0D,
            0.0D,
            0.0D,
            1.0D,
            1.0D
        );
}
