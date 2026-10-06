package com.cyberspectraa.cyberraces.race;

import net.minecraft.util.RandomSource;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum RaceEvolution {
    PARAGON("paragon", "Paragon", "A human who pushes physical adaptability beyond ordinary limits.", Race.HUMAN,
        s(2,1.03,1,1,1,0.03,0.5,0,1,1), "paragon"),
    AWAKENED("awakened", "Awakened", "A human whose latent magical potential has fully surfaced.", Race.HUMAN,
        s(0,1.01,1.15,1.12,1.08,0,0,0,1,1), "awakened"),
    SURVIVOR("survivor", "Survivor", "A hardened human who endures punishment and adapts under pressure.", Race.HUMAN,
        s(4,0.99,1,1,1.05,0.06,1.5,0,1,1), "survivor"),

    HIGH_ELF("high_elf", "High Elf", "An elf whose lineage deepens its affinity for refined magic.", Race.ELF,
        s(1,1.01,1.15,1.10,1.08,0,0,0,1,1), "high_elf"),
    WOOD_ELF("wood_elf", "Wood Elf", "A woodland-adapted elf with superior mobility and survival instincts.", Race.ELF,
        s(2,1.06,1.02,1.02,1.02,0,0,0,1,1), "wood_elf"),
    MOON_ELF("moon_elf", "Moon Elf", "A swift nocturnal elf whose movement and arcane senses sharpen together.", Race.ELF,
        s(0,1.08,1.07,1.06,1.05,0,0,0,1,1), "moon_elf"),

    DEEPFORGED("deepforged", "Deepforged", "A dwarf shaped by deep stone, pressure and relentless endurance.", Race.DWARF,
        s(4,0.97,1,1,1.06,0.10,2,0,1.02,1), "deepforged"),
    RUNEBORN("runeborn", "Runeborn", "A dwarf whose body resonates with protective runic power.", Race.DWARF,
        s(2,1,1.08,1.05,1.18,0.05,1,0,1,1), "runeborn"),
    MOUNTAINBORN("mountainborn", "Mountainborn", "A broader, stronger dwarf built to hold ground against overwhelming force.", Race.DWARF,
        s(3,0.99,1,1,1.04,0.15,1,0,1.04,1), "mountainborn"),

    LIGHTFOOT("lightfoot", "Lightfoot", "A halfling whose already nimble movement becomes exceptionally quick and evasive.", Race.HALFLING,
        s(0,1.10,1,1,1,0,0,0.5,0.97,1), "lightfoot"),
    HEARTHWARDEN("hearthwarden", "Hearthwarden", "A resilient halfling protector strengthened by home, allies and community.", Race.HALFLING,
        s(3,1,1,1,1.04,0.05,1,0.5,1,1), "hearthwarden"),
    FORTUNE_TOUCHED("fortune_touched", "Fortune-Touched", "An unusually lucky halfling whose good fortune follows them into danger.", Race.HALFLING,
        s(1,1.02,1,1,1,0,0,1.5,1,1), "fortune_touched"),

    WARBORN("warborn", "Warborn", "An orc whose body is honed for relentless close combat.", Race.ORC,
        s(3,1.04,1,1,1,0.06,1,0,1.02,1), "warborn"),
    IRONHIDE("ironhide", "Ironhide", "An orc whose hide and frame harden into natural armour.", Race.ORC,
        s(5,0.98,1,1,1.05,0.10,3,0,1.03,1), "ironhide"),
    SPIRITBLOOD("spiritblood", "Spiritblood", "An orc whose spiritual lineage grants greater supernatural resistance.", Race.ORC,
        s(2,1,1.05,1.05,1.18,0.03,0.5,0,1,1), "spiritblood"),

    HOBGOBLIN("hobgoblin", "Hobgoblin", "A larger and stronger goblin evolution built for direct confrontation.", Race.GOBLIN,
        s(4,1.01,1,1,1,0.06,1,0,1.08,1), "hobgoblin"),
    GLOOMRUNNER("gloomrunner", "Gloomrunner", "A nimble goblin evolution specialising in escape, pursuit and cramped terrain.", Race.GOBLIN,
        s(0,1.11,1,1.03,1,0,0,0.25,0.98,1), "gloomrunner"),
    SCROUNGER("scrounger", "Scrounger", "A goblin whose scavenging instincts become extraordinarily effective.", Race.GOBLIN,
        s(1,1.03,1,1,1,0,0,1.0,1,1), "scrounger"),

    ASHBORN("ashborn", "Ashborn", "A tiefling whose infernal blood burns hotter and resists flame even further.", Race.TIEFLING,
        s(2,1,1.06,1.04,1.05,0,0.5,0,1,0.50), "ashborn"),
    UMBRAL("umbral", "Umbral", "A tiefling attuned to shadow, speed and elusive supernatural movement.", Race.TIEFLING,
        s(0,1.08,1.08,1.08,1.08,0,0,0,1,1), "umbral"),
    BLOODHORN("bloodhorn", "Bloodhorn", "A physically imposing tiefling with a stronger frame and heavier natural defence.", Race.TIEFLING,
        s(4,0.99,1,1,1.04,0.08,2,0,1.04,1), "bloodhorn"),

    EMBERBLOOD("emberblood", "Emberblood", "A dragonborn lineage that intensifies the ancestral fire breath.", Race.DRAGONBORN,
        s(3,1,1.03,1.02,1.04,0.05,1,0,1.02,0.80), "dragon_breath_fire"),
    FROSTBLOOD("frostblood", "Frostblood", "A dragonborn lineage whose breath and body attune to bitter cold.", Race.DRAGONBORN,
        s(3,0.99,1.04,1.02,1.10,0.06,1.5,0,1.02,1), "dragon_breath_frost"),
    STORMBLOOD("stormblood", "Stormblood", "A dragonborn lineage charged with speed and storm-like energy.", Race.DRAGONBORN,
        s(2,1.05,1.06,1.05,1.06,0.04,0.5,0,1.01,1), "dragon_breath_lightning"),
    SKYBORN("skyborn", "Skyborn", "A winged dragonborn lineage that awakens true draconic flight through Icarus dragon wings.", Race.DRAGONBORN,
        s(2,1.04,1.03,1.03,1.05,0.03,0.5,0,1.01,1), "dragon_breath_skyborn"),

    SYLPH("sylph", "Sylph", "A fairy evolution focused on air control, hover mobility and evasive movement.", Race.FAIRY,
        s(1,1.10,1.02,1.05,1.02,0,0,0,0.95,1), "fairy_hover_sylph"),
    GLIMMERFAE("glimmerfae", "Glimmerfae", "A magically radiant fairy with a stronger affinity for mana and spellcraft.", Race.FAIRY,
        s(0,1.04,1.15,1.12,1.08,0,0,0,1,1), "fairy_hover_glimmer"),
    THORNFAE("thornfae", "Thornfae", "A tougher nature-bound fairy adapted for dangerous wilderness combat.", Race.FAIRY,
        s(3,1.03,1.05,1.05,1.04,0.03,1,0,1.06,1), "fairy_hover_thorn"),

    DRYAD("dryad", "Dryad", "A forest-bound nymph lineage strengthened by trees, roots and living wood.", Race.NYMPH,
        s(3,1.01,1.05,1.05,1.05,0.03,1,0,1,1), "nature_grace_dryad"),
    NAIAD("naiad", "Naiad", "A water-bound nymph lineage adapted for rivers, lakes and aquatic movement.", Race.NYMPH,
        s(1,1.06,1.08,1.08,1.04,0,0,0,1,1), "nature_grace_naiad"),
    OREAD("oread", "Oread", "A mountain and stone nymph lineage with exceptional durability.", Race.NYMPH,
        s(4,0.98,1.02,1.02,1.10,0.08,2,0,1.02,1), "nature_grace_oread"),
    ANTHOUSA("anthousa", "Anthousa", "A flowering-meadow nymph lineage associated with growth, luck and restoration.", Race.NYMPH,
        s(2,1.03,1.08,1.10,1.04,0,0,0.5,1,1), "nature_grace_anthousa"),

    LYNXKIN("lynxkin", "Lynxkin", "A catfolk evolution built around explosive pounces and agile pursuit.", Race.CATFOLK,
        s(1,1.09,1,1,1,0,0,0,1,1), "catfolk_pounce_lynx"),
    PANTHERKIN("pantherkin", "Pantherkin", "A sleek catfolk evolution favouring stealth, darkness and repositioning.", Race.CATFOLK,
        s(1,1.07,1.03,1.03,1.03,0,0,0,1.01,1), "catfolk_pounce_panther"),
    LIONKIN("lionkin", "Lionkin", "A heavier catfolk evolution built for strength and group combat.", Race.CATFOLK,
        s(4,1.01,1,1,1,0.07,1.5,0,1.05,1), "catfolk_pounce_lion"),

    HOUNDKIN("houndkin", "Houndkin", "A dogfolk evolution with exceptional tracking instincts and pursuit stamina.", Race.DOGFOLK,
        s(1,1.05,1,1,1,0,0,0,1,1), "scent_hound"),
    WOLFKIN("wolfkin", "Wolfkin", "A pack-oriented dogfolk evolution strengthened when fighting alongside allies.", Race.DOGFOLK,
        s(3,1.03,1,1,1.03,0.05,0.5,0,1.03,1), "scent_wolf"),
    MASTIFFKIN("mastiffkin", "Mastiffkin", "A broad defensive dogfolk evolution specialised in guarding and holding ground.", Race.DOGFOLK,
        s(5,0.98,1,1,1.05,0.10,2,0,1.06,1), "scent_mastiff"),

    SWIFTFOX("swiftfox", "Swiftfox", "A foxfolk evolution focused almost entirely on speed and evasive movement.", Race.FOXFOLK,
        s(0,1.11,1,1.03,1,0,0,0,0.99,1), "quickstep_swift"),
    TRICKSTER("trickster", "Trickster", "A deceptive foxfolk evolution specialising in misdirection and escape.", Race.FOXFOLK,
        s(1,1.07,1.04,1.05,1.05,0,0,0.25,1,1), "quickstep_trickster"),
    SPIRIT_FOX("spirit_fox", "Spirit Fox", "A supernatural foxfolk evolution whose magic affinity foreshadows a future Kitsune ascension.", Race.FOXFOLK,
        s(1,1.04,1.15,1.12,1.10,0,0,0,1,1), "quickstep_spirit"),

    SERAPHIC("seraphic", "Seraphic", "A radiant aasimar lineage that deepens its connection to Holy magic and celestial power.", Race.AASIMAR,
        s(1,1.02,1.12,1.10,1.10,0,0.5,0,1,1), "aasimar_seraphic"),
    FALLEN("fallen", "Fallen", "An aasimar that turns away from radiance and channels necromantic power instead.", Race.AASIMAR,
        s(2,1.03,1.10,1.08,1.08,0.02,0.5,0,1,1), "aasimar_fallen"),
    CELESTIAL_GUARDIAN("guardian", "Guardian", "A defensive aasimar lineage focused on protection, resilience and shielding allies.", Race.AASIMAR,
        s(4,0.99,1.04,1.04,1.12,0.08,2,0,1.02,1), "aasimar_guardian"),

    RAPTOR("raptor", "Raptor", "A birdfolk lineage built around speed, diving attacks and aggressive aerial movement.", Race.BIRDFOLK,
        s(1,1.08,1,1,1,0,0,0,1,1), "wing_burst_raptor"),
    NIGHTWING("nightwing", "Nightwing", "A quiet birdfolk lineage adapted for darkness, scouting and controlled flight.", Race.BIRDFOLK,
        s(1,1.05,1.03,1.03,1.05,0,0,0.25,1,1), "wing_burst_nightwing"),
    STORMWING("stormwing", "Stormwing", "A high-altitude birdfolk lineage empowered by storms and exceptional aerial mobility.", Race.BIRDFOLK,
        s(2,1.07,1.05,1.05,1.08,0.02,0.5,0,1,1), "wing_burst_storm");

    public static final int REQUIRED_LEVEL = 30;

    private final String id;
    private final String displayName;
    private final String description;
    private final Race baseRace;
    private final RaceEvolutionStats stats;
    private final String abilityProfile;

    RaceEvolution(
        String id,
        String displayName,
        String description,
        Race baseRace,
        RaceEvolutionStats stats,
        String abilityProfile
    ) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.baseRace = baseRace;
        this.stats = stats;
        this.abilityProfile = abilityProfile;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public String description() { return description; }
    public Race baseRace() { return baseRace; }
    public RaceEvolutionStats stats() { return stats; }
    public String abilityProfile() { return abilityProfile; }

    public static Optional<RaceEvolution> byId(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        String normalized = id.trim().toLowerCase(Locale.ROOT);

        return Arrays.stream(values())
            .filter(value -> value.id.equals(normalized))
            .findFirst();
    }

    public static RaceEvolution[] choicesFor(Race race) {
        if (race == null) {
            return new RaceEvolution[0];
        }

        return Arrays.stream(values())
            .filter(value -> value.baseRace == race)
            .toArray(RaceEvolution[]::new);
    }

    public static Optional<RaceEvolution> randomFor(
        Race race,
        RandomSource random
    ) {
        RaceEvolution[] choices = choicesFor(race);

        if (choices.length == 0) {
            return Optional.empty();
        }

        return Optional.of(
            choices[random.nextInt(choices.length)]
        );
    }

    private static RaceEvolutionStats s(
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
        return new RaceEvolutionStats(
            healthBonus,
            movementMultiplier,
            maxManaMultiplier,
            manaRegenMultiplier,
            spellResistanceMultiplier,
            knockbackResistanceBonus,
            armorBonus,
            luckBonus,
            scaleMultiplier,
            fireDamageMultiplier
        );
    }
}
