package com.cyberspectraa.cyberraces.event;

import com.cyberspectraa.cyberraces.compat.CyberNpcRaceManager;
import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceEvolution;
import com.cyberspectraa.cyberraces.race.RaceManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-only, low-overhead homeland attunement for PLAYERS and Wild NPCs.
 *
 * A character must remain within ONE qualifying biome for 60 seconds.
 * Transient attribute modifiers stay in place while the entity remains in that
 * specific biome, and are removed immediately upon biome/dimension/race change.
 * No long potion duration, save mutation or affinity power creep.
 */
public final class BiomeAttunementService {
    private static final long DWELL_TICKS = 1200L; // one minute
    private static final UUID SPEED_ID =
            UUID.fromString("10e923ac-03d5-4c87-8ff9-4be849572a00");
    private static final UUID ARMOR_ID =
            UUID.fromString("db3552c1-ac68-42df-a396-ab45da6c778c");
    private static final UUID DAMAGE_ID =
            UUID.fromString("6fec398a-3d6c-45ee-95d3-ff3469e96a2a");
    private static final UUID LUCK_ID =
            UUID.fromString("8772296c-b723-43bc-a2c2-a9460681213b");
    private static final Map<UUID, State> STATES = new HashMap<>();

    private BiomeAttunementService() {}

    /** Deliberately conservative; no races receive their bonus everywhere. */
    private enum BiomeHome {
        PLAINS("plains and meadows"),
        FOREST("woodlands"),
        TAIGA("taiga"),
        JUNGLE("jungles"),
        MOUNTAIN("mountain peaks"),
        COLD("snowy biomes"),
        DESERT("deserts and badlands"),
        SWAMP("swamps"),
        RIVER("rivers and coasts"),
        CAVE("underground and cave biomes"),
        NETHER("the Nether"),
        SOUL("soul valleys and the deep dark"),
        END("the End"),
        SAVANNA("savannas"),
        DARK_FOREST("dark forests"),
        FLOWER("flower forests and meadows");

        final String description;
        BiomeHome(String description) { this.description = description; }

        boolean matches(LivingEntity entity, String id) {
            boolean underground = entity.getY() < 36
                    && !entity.level().canSeeSky(entity.blockPosition());
            return switch (this) {
                case PLAINS -> contains(id, "plains", "meadow", "field", "prairie");
                case FOREST -> contains(id, "forest", "wood", "grove", "orchard")
                        && !contains(id, "dark_forest", "pale_garden");
                case TAIGA -> contains(id, "taiga", "pine", "spruce");
                case JUNGLE -> contains(id, "jungle", "bamboo", "rainforest");
                case MOUNTAIN -> contains(id, "peak", "mountain", "slope",
                        "windswept", "highlands", "stony");
                case COLD -> contains(id, "snow", "frozen", "ice", "glacier",
                        "tundra", "cold");
                case DESERT -> contains(id, "desert", "badlands", "mesa", "wasteland");
                case SWAMP -> contains(id, "swamp", "mangrove", "marsh", "bog");
                case RIVER -> contains(id, "river", "ocean", "beach", "coast", "shore");
                case CAVE -> underground
                        || contains(id, "cave", "deep_dark", "underground", "dripstone");
                case NETHER -> entity.level().dimension() == Level.NETHER;
                case SOUL -> contains(id, "soul_sand_valley", "deep_dark",
                        "soul_valley");
                case END -> entity.level().dimension() == Level.END;
                case SAVANNA -> contains(id, "savanna", "grassland", "steppe");
                case DARK_FOREST -> contains(id, "dark_forest", "pale_garden",
                        "haunted_forest", "dark_wood");
                case FLOWER -> contains(id, "flower", "meadow", "cherry_grove",
                        "blossom");
            };
        }

        private static boolean contains(String id, String... terms) {
            for (String term : terms) if (id.contains(term)) return true;
            return false;
        }
    }

    private enum Gift {
        SWIFT, BULWARK, MIGHT, SPIRIT
    }

    private record Home(BiomeHome biome, Gift gift) {}
    private record State(String dimension, String biome,
            Race race, RaceEvolution evolution, long since, boolean active) {}

    public static void tickPlayer(ServerPlayer player) {
        if (player == null) return;
        Race race = RaceManager.getRace(player).orElse(null);
        RaceEvolution evolution = RaceManager.getEvolution(player).orElse(null);
        tick(player, race, evolution);
    }

    public static void tickWildNpc(LivingEntity entity) {
        if (entity == null || !CyberNpcRaceManager.isWildCyberNpc(entity)) return;
        tick(entity, CyberNpcRaceManager.getRace(entity).orElse(null), null);
    }

    private static void tick(LivingEntity entity, Race race, RaceEvolution evolution) {
        if (entity.level().isClientSide) return;
        UUID uuid = entity.getUUID();
        if (race == null || !entity.isAlive()) {
            clear(entity);
            return;
        }

        Home home = home(race, evolution);
        String dimension = entity.level().dimension().location().toString();
        String biome = entity.level().getBiome(entity.blockPosition())
                .unwrapKey().map(key -> key.location().toString()).orElse("unknown");
        String biomePath = biome.contains(":") ? biome.substring(biome.indexOf(':') + 1) : biome;
        boolean matching = home.biome.matches(entity, biomePath);
        State existing = STATES.get(uuid);

        // A change of exact biome, dimension or race immediately expires
        // attunement. Crossing into another suitable biome resets the timer.
        if (existing != null && (!existing.biome.equals(biome)
                || !existing.dimension.equals(dimension)
                || existing.race != race || existing.evolution != evolution)) {
            boolean wasActive = existing.active;
            clear(entity);
            if (wasActive && entity instanceof ServerPlayer player) {
                player.displayClientMessage(Component.literal(
                        "Your previous biome attunement fades.").withStyle(ChatFormatting.GRAY), true);
            }
            existing = null;
        }

        if (!matching) {
            if (existing != null) clear(entity);
            return;
        }

        long now = entity.level().getGameTime();
        if (existing == null || now < existing.since) {
            STATES.put(uuid, new State(dimension, biome, race, evolution, now, false));
            return;
        }
        if (!existing.active && now - existing.since >= DWELL_TICKS) {
            apply(entity, home.gift, evolution != null);
            STATES.put(uuid, new State(dimension, biome, race, evolution, existing.since, true));
            if (entity instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.literal(
                        "Biome attunement awakened: " + home.biome.description
                        + " — " + giftDescription(home.gift, evolution != null) + ".")
                        .withStyle(ChatFormatting.GREEN));
            }
        }

        // An existing active focus grants a small health recovery, without any
        // lingering potion effects once the biome is left.
        State current = STATES.get(uuid);
        if (current != null && current.active && home.gift == Gift.SPIRIT
                && entity instanceof ServerPlayer && now % 200L == 0) {
            if (entity.getHealth() < entity.getMaxHealth())
                entity.heal(evolution == null ? 0.5F : 1.0F);
        }
    }

    /** A single compatible base race applies to Wild NPCs. */
    private static Home home(Race race, RaceEvolution evolution) {
        if (evolution != null) {
            return new Home(evolutionBiome(evolution), evolutionGift(evolution));
        }
        return switch (race) {
            case HUMAN -> new Home(BiomeHome.PLAINS, Gift.SWIFT);
            case ELF -> new Home(BiomeHome.FOREST, Gift.SPIRIT);
            case DWARF -> new Home(BiomeHome.MOUNTAIN, Gift.BULWARK);
            case HALFLING -> new Home(BiomeHome.FLOWER, Gift.SWIFT);
            case ORC -> new Home(BiomeHome.SAVANNA, Gift.MIGHT);
            case GOBLIN -> new Home(BiomeHome.CAVE, Gift.SWIFT);
            case TIEFLING -> new Home(BiomeHome.NETHER, Gift.SPIRIT);
            case DRAGONBORN -> new Home(BiomeHome.MOUNTAIN, Gift.BULWARK);
            case FAIRY -> new Home(BiomeHome.FLOWER, Gift.SWIFT);
            case NYMPH -> new Home(BiomeHome.FOREST, Gift.SPIRIT);
            case CATFOLK -> new Home(BiomeHome.JUNGLE, Gift.SWIFT);
            case DOGFOLK -> new Home(BiomeHome.TAIGA, Gift.BULWARK);
            case FOXFOLK -> new Home(BiomeHome.TAIGA, Gift.SWIFT);
            case AASIMAR -> new Home(BiomeHome.PLAINS, Gift.SPIRIT);
            case BIRDFOLK -> new Home(BiomeHome.MOUNTAIN, Gift.SWIFT);
            case SLIME -> new Home(BiomeHome.SWAMP, Gift.BULWARK);
            case ARACHNID -> new Home(BiomeHome.CAVE, Gift.SWIFT);
            case UNDEAD -> new Home(BiomeHome.SOUL, Gift.BULWARK);
        };
    }

    /**
     * Every evolution has its own plausible favored habitat. For unlisted
     * martial/non-biome subclasses, the base race's homeland remains sensible.
     * The identifiers are NOT changed by new display names.
     */
    private static BiomeHome evolutionBiome(RaceEvolution evo) {
        return switch (evo) {
            case HIGH_ELF, WOOD_ELF, DRYAD, THORNFAE, SCROUNGER,
                    TRICKSTER, WEBWEAVER, BROODMOTHER, HOUNDKIN,
                    GLIMMERFAE, SPIRIT_FOX -> BiomeHome.FOREST;
            case MOON_ELF, UMBRAL, NIGHTWING, PANTHERKIN,
                    GLOOMRUNNER, WHISPERFOOT, VOID_SLIME -> BiomeHome.DARK_FOREST;
            case DEEPFORGED, RUNEBORN, CAVEWRIGHT, METAL_SLIME,
                    CAVE_STALKER, EARTHSCALE, OREAD, DREAD_KNIGHT,
                    HOBGOBLIN, IRONHIDE -> BiomeHome.CAVE;
            case MOUNTAINBORN, SKYBORN, RAPTOR, SYLPH,
                    CLOUDLEAPER, STARSEEKER -> BiomeHome.MOUNTAIN;
            case FROSTBLOOD, FROST_SLIME, FROSTHOUND,
                    WOLFKIN -> BiomeHome.COLD;
            case ASHBORN, EMBERBLOOD, MAGMA_SLIME, EMBERFORGED,
                    RIFTWALKER, EMBERFOX, BLOODHORN -> BiomeHome.NETHER;
            case STORMBLOOD, STORMWING, STORMCHASER,
                    TEMPEST_NYMPH -> BiomeHome.MOUNTAIN;
            case NAIAD, DEWDROP, TIDEWING -> BiomeHome.RIVER;
            case ACID_SLIME, PLAGUEBORN, REVENANT -> BiomeHome.SWAMP;
            case VENOMFANG, LYNXKIN, HUNTSMAN -> BiomeHome.JUNGLE;
            case ROYAL_SLIME, ANTHOUSA, LIGHTFOOT, FORTUNE_TOUCHED,
                    HEARTHWARDEN, WAYFARER, DAWNBRINGER -> BiomeHome.FLOWER;
            case LICHBOUND, WRAITH, FALLEN -> BiomeHome.SOUL;
            case CRYSTAL_SLIME, SPIRITBLOOD -> BiomeHome.END;
            case LIONKIN, WARBORN -> BiomeHome.SAVANNA;
            default -> home(evo.baseRace(), null).biome;
        };
    }

    private static Gift evolutionGift(RaceEvolution evolution) {
        return switch (evolution) {
            case WARBORN, HOBGOBLIN, EMBERBLOOD, LYNXKIN,
                    LIONKIN, VENOMFANG, HUNTSMAN, DREAD_KNIGHT,
                    MAGMA_SLIME, ACID_SLIME, STORMCHASER, EMBERFORGED,
                    TEMPEST_NYMPH -> Gift.MIGHT;
            case SURVIVOR, DEEPFORGED, MOUNTAINBORN, IRONHIDE, BLOODHORN,
                    CELESTIAL_GUARDIAN, OREAD, WOLFKIN, MASTIFFKIN,
                    METAL_SLIME, ROYAL_SLIME, BROODMOTHER, REVENANT,
                    EARTHSCALE, CAVE_STALKER -> Gift.BULWARK;
            case AWAKENED, HIGH_ELF, RUNEBORN, SPIRITBLOOD,
                    GLIMMERFAE, ANTHOUSA, SPIRIT_FOX, SERAPHIC, FALLEN,
                    STORMBLOOD, CRYSTAL_SLIME, VOID_SLIME, LICHBOUND,
                    PLAGUEBORN, STARSEEKER, DAWNBRINGER -> Gift.SPIRIT;
            default -> Gift.SWIFT;
        };
    }

    private static void apply(LivingEntity entity, Gift gift, boolean evolved) {
        removeModifiers(entity);
        switch (gift) {
            case SWIFT -> add(entity.getAttribute(Attributes.MOVEMENT_SPEED),
                    SPEED_ID, "CyberRaces biome swiftness",
                    evolved ? 0.10D : 0.055D, AttributeModifier.Operation.MULTIPLY_TOTAL);
            case BULWARK -> add(entity.getAttribute(Attributes.ARMOR),
                    ARMOR_ID, "CyberRaces biome fortitude",
                    evolved ? 2.0D : 1.0D, AttributeModifier.Operation.ADDITION);
            case MIGHT -> add(entity.getAttribute(Attributes.ATTACK_DAMAGE),
                    DAMAGE_ID, "CyberRaces biome might",
                    evolved ? 1.50D : 0.75D, AttributeModifier.Operation.ADDITION);
            case SPIRIT -> add(entity.getAttribute(Attributes.LUCK),
                    LUCK_ID, "CyberRaces biome spirit",
                    evolved ? 1.0D : 0.5D, AttributeModifier.Operation.ADDITION);
        }
    }

    private static String giftDescription(Gift gift, boolean evolved) {
        return switch (gift) {
            case SWIFT -> evolved ? "+10% movement speed" : "+5.5% movement speed";
            case BULWARK -> evolved ? "+2 natural armor" : "+1 natural armor";
            case MIGHT -> evolved ? "+1.5 attack damage" : "+0.75 attack damage";
            case SPIRIT -> evolved ? "+1 Luck and slow recovery"
                    : "+0.5 Luck and slow recovery";
        };
    }

    private static void add(AttributeInstance attribute, UUID id, String name,
                            double amount, AttributeModifier.Operation operation) {
        if (attribute == null) return;
        attribute.removeModifier(id);
        attribute.addTransientModifier(new AttributeModifier(id, name, amount, operation));
    }

    private static void removeModifiers(LivingEntity entity) {
        remove(entity.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_ID);
        remove(entity.getAttribute(Attributes.ARMOR), ARMOR_ID);
        remove(entity.getAttribute(Attributes.ATTACK_DAMAGE), DAMAGE_ID);
        remove(entity.getAttribute(Attributes.LUCK), LUCK_ID);
    }

    private static void remove(AttributeInstance attribute, UUID uuid) {
        if (attribute != null) attribute.removeModifier(uuid);
    }

    /** Explicit cleanup on logout, untracking or entity removal. */
    public static void clear(LivingEntity entity) {
        if (entity == null) return;
        State previous = STATES.remove(entity.getUUID());
        removeModifiers(entity);
        if (entity instanceof ServerPlayer player
                && previous != null && previous.active) {
            EvolutionTalentEvents.clearOwned(player);
        }
    }

    public static boolean isAttuned(ServerPlayer player) {
        State state = STATES.get(player.getUUID());
        return state != null && state.active;
    }

    public static Component status(ServerPlayer player) {
        Race race = RaceManager.getRace(player).orElse(null);
        if (race == null) return Component.literal("No race selected.");
        RaceEvolution evolution = RaceManager.getEvolution(player).orElse(null);
        Home home = home(race, evolution);
        State state = STATES.get(player.getUUID());
        int left = state == null || !state.active
                ? state == null ? 60 : (int)Math.max(0L, (DWELL_TICKS
                  - (player.level().getGameTime()-state.since)+19L)/20L) : 0;
        String line = state != null && state.active
                ? "Attuned to "+home.biome.description+": "+giftDescription(home.gift,evolution!=null)
                : "Favored biome: "+home.biome.description+" | Attunement requires 60 seconds in the SAME biome"
                    +(state != null ? " ("+left+"s left)" : "");
        return Component.literal(line).withStyle(ChatFormatting.AQUA);
    }
}
