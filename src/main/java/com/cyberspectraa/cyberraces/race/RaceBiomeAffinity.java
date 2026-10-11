package com.cyberspectraa.cyberraces.race;

import com.cyberspectraa.cyberraces.compat.CyberNpcRaceManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Race-specific habitat attunement. A player or Wild NPC must actually stay
 * in one qualifying biome for 30 seconds. A transient attribute modifier is
 * then applied for as long as that entity remains in that precise biome;
 * crossing its border or changing dimension removes it immediately (next
 * one-second sample).
 *
 * Transient attributes, not potion effects, prevent conflicts with potion
 * buffs and let us precisely remove only the racial bonus.
 *
 * State is session-only, keyed weakly by entity instance; death/logout,
 * race reassignment and dimensional moves cannot leave stale bonuses.
 */
public final class RaceBiomeAffinity {
    private static final int REQUIRED_SECONDS = 30;

    private static final UUID SPEED_ID =
            UUID.fromString("b8d4fa95-313d-43ad-890d-f8434a3e6229");
    private static final UUID ARMOR_ID =
            UUID.fromString("109f0603-a9b2-4eeb-9694-a5ad4d43ef5c");
    private static final UUID DAMAGE_ID =
            UUID.fromString("dca20c49-a2e4-46f5-9fc3-92a87d979fdb");
    private static final UUID LUCK_ID =
            UUID.fromString("a4c7e4ed-f84a-4b95-b4b7-1904040d3551");
    private static final UUID HEALTH_ID =
            UUID.fromString("1aa58b4a-5bf1-48cb-9091-3ca3e996ca0c");

    private static final Map<LivingEntity, Stay> STAYS = new WeakHashMap<>();

    private enum Bonus {
        SPEED, ARMOR, ATTACK, LUCK, HEALTH
    }

    private record Habitat(String description, Bonus bonus, double amount) {}

    private static final class Stay {
        private ResourceLocation biome;
        private ResourceKey<Level> dimension;
        private Race race;
        private int seconds;
        private boolean active;
        private Stay(ResourceLocation biome, ResourceKey<Level> dimension, Race race) {
            this.biome = biome;
            this.dimension = dimension;
            this.race = race;
        }
        private boolean matches(ResourceLocation biome, ResourceKey<Level> dimension, Race race) {
            return this.biome.equals(biome) && this.dimension.equals(dimension) && this.race == race;
        }
    }

    private RaceBiomeAffinity() {}

    private static Habitat habitat(Race race) {
        return switch (race) {
            case HUMAN -> new Habitat("plains, meadows, savannas", Bonus.SPEED, 0.04D);
            case ELF -> new Habitat("forests and jungles", Bonus.SPEED, 0.06D);
            case DWARF -> new Habitat("peaks, mountains, stony caves", Bonus.ARMOR, 2.0D);
            case HALFLING -> new Habitat("plains and meadows", Bonus.LUCK, 1.0D);
            case ORC -> new Habitat("savannas and badlands", Bonus.ATTACK, 1.0D);
            case GOBLIN -> new Habitat("swamps and caves", Bonus.SPEED, 0.05D);
            case TIEFLING -> new Habitat("Nether biomes", Bonus.ARMOR, 1.5D);
            case DRAGONBORN -> new Habitat("mountains and badlands", Bonus.ARMOR, 2.0D);
            case FAIRY -> new Habitat("flower forests, meadows and lush caves", Bonus.SPEED, 0.08D);
            case NYMPH -> new Habitat("forests, rivers and lush caves", Bonus.HEALTH, 2.0D);
            case CATFOLK -> new Habitat("jungles and savannas", Bonus.SPEED, 0.06D);
            case DOGFOLK -> new Habitat("taiga and groves", Bonus.ARMOR, 1.5D);
            case FOXFOLK -> new Habitat("taiga and snowy forests", Bonus.SPEED, 0.06D);
            case AASIMAR -> new Habitat("plains and meadows", Bonus.HEALTH, 2.0D);
            case BIRDFOLK -> new Habitat("mountains and windswept highlands", Bonus.SPEED, 0.07D);
            case SLIME -> new Habitat("swamps and lush caves", Bonus.ARMOR, 1.5D);
            case ARACHNID -> new Habitat("dark forests, jungles and caves", Bonus.SPEED, 0.06D);
            case UNDEAD -> new Habitat("dark forests and swamps", Bonus.ARMOR, 2.0D);
        };
    }

    private static String bonusName(Habitat h) {
        return switch (h.bonus) {
            case SPEED -> "+" + (int)Math.round(h.amount * 100D) + "% movement speed";
            case ARMOR -> "+" + h.amount + " natural armour";
            case ATTACK -> "+" + h.amount + " attack damage";
            case LUCK -> "+" + h.amount + " Luck";
            case HEALTH -> "+" + h.amount + " maximum health";
        };
    }

    private static boolean contains(String name, String... words) {
        for (String word : words) if (name.contains(word)) return true;
        return false;
    }

    private static boolean tag(Holder<Biome> biome, String name) {
        return biome.is(TagKey.create(Registries.BIOME,
                new ResourceLocation("minecraft", "is_" + name)));
    }

    private static boolean appropriate(Race race, ServerLevel level,
            Holder<Biome> biome, ResourceLocation id) {
        String name = id.getPath();
        boolean nether = level.dimension() == Level.NETHER;
        if (race == Race.TIEFLING) return nether;
        // A Nether crimson forest is not an ordinary elven forest.
        if (nether || level.dimension() == Level.END) return false;

        boolean forest = tag(biome, "forest") || contains(name, "forest", "woods");
        boolean jungle = tag(biome, "jungle") || contains(name, "jungle", "rainforest");
        boolean mountain = tag(biome, "mountain") ||
                contains(name, "mountain", "peak", "windswept_hill", "stony");
        boolean river = tag(biome, "river") || contains(name, "river", "stream");
        boolean swamp = tag(biome, "swamp") ||
                contains(name, "swamp", "mangrove", "marsh");
        boolean taiga = tag(biome, "taiga") || contains(name, "taiga", "grove");
        boolean plains = tag(biome, "plains") ||
                contains(name, "plains", "meadow", "prairie", "grassland");
        boolean savanna = tag(biome, "savanna") || contains(name, "savanna");
        boolean badlands = tag(biome, "badlands") || contains(name, "badlands", "mesa");
        boolean cave = contains(name, "cave", "deep_dark", "underground");
        boolean lush = contains(name, "lush_caves", "lush_cave");
        boolean dark = contains(name, "dark_forest", "dark_woods", "darkwood");
        boolean snowy = tag(biome, "snowy") ||
                contains(name, "snow", "frozen", "ice_spikes");
        boolean flower = contains(name, "flower_forest", "cherry_grove", "meadow");
        boolean windswept = contains(name, "windswept", "jagged_peaks", "frozen_peaks");

        return switch (race) {
            case HUMAN -> plains || savanna;
            case ELF -> forest || jungle;
            case DWARF -> mountain || cave;
            case HALFLING -> plains;
            case ORC -> savanna || badlands;
            case GOBLIN -> swamp || cave;
            case TIEFLING -> false; // handled above
            case DRAGONBORN -> mountain || badlands;
            case FAIRY -> flower || lush;
            case NYMPH -> forest || river || lush;
            case CATFOLK -> jungle || savanna;
            case DOGFOLK -> taiga;
            case FOXFOLK -> taiga || snowy && (forest || contains(name,"grove"));
            case AASIMAR -> plains;
            case BIRDFOLK -> mountain || windswept;
            case SLIME -> swamp || lush;
            case ARACHNID -> dark || jungle || cave;
            case UNDEAD -> dark || swamp;
        };
    }

    @SubscribeEvent
    public static void livingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level) ||
                entity.tickCount % 20 != 0) return;

        Race race;
        if (entity instanceof ServerPlayer player) {
            race = RaceManager.getRace(player).orElse(null);
        } else if (CyberNpcRaceManager.isWildCyberNpc(entity)) {
            race = CyberNpcRaceManager.getRace(entity).orElse(null);
            // Defensive check for migrated/older data: never permit Undead
            // affinity on NPCs even before an assignment/migration occurs.
            if (race == Race.UNDEAD) race = null;
        } else {
            if (STAYS.containsKey(entity)) clear(entity);
            return;
        }

        if (race == null || !entity.isAlive()) {
            clear(entity);
            return;
        }
        Holder<Biome> biome = level.getBiome(entity.blockPosition());
        ResourceLocation id = biome.unwrapKey().map(key -> key.location()).orElse(null);
        if (id == null || !appropriate(race, level, biome, id)) {
            clear(entity);
            return;
        }
        Stay stay = STAYS.get(entity);
        if (stay == null || !stay.matches(id, level.dimension(), race)) {
            clear(entity);
            stay = new Stay(id, level.dimension(), race);
            STAYS.put(entity, stay);
        }
        if (stay.active) return;
        stay.seconds++;
        if (stay.seconds >= REQUIRED_SECONDS) {
            apply(entity, habitat(race));
            stay.active = true;
            if (entity instanceof ServerPlayer player) {
                player.displayClientMessage(Component.literal("Habitat attuned: ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(bonusName(habitat(race)))), true);
            }
        }
    }

    private static void apply(LivingEntity entity, Habitat habitat) {
        // The race-specific values are small and never overwrite class buffs.
        AttributeInstance attribute = switch (habitat.bonus) {
            case SPEED -> entity.getAttribute(Attributes.MOVEMENT_SPEED);
            case ARMOR -> entity.getAttribute(Attributes.ARMOR);
            case ATTACK -> entity.getAttribute(Attributes.ATTACK_DAMAGE);
            case LUCK -> entity.getAttribute(Attributes.LUCK);
            case HEALTH -> entity.getAttribute(Attributes.MAX_HEALTH);
        };
        UUID id = switch (habitat.bonus) {
            case SPEED -> SPEED_ID;
            case ARMOR -> ARMOR_ID;
            case ATTACK -> DAMAGE_ID;
            case LUCK -> LUCK_ID;
            case HEALTH -> HEALTH_ID;
        };
        if (attribute != null) {
            attribute.removeModifier(id);
            attribute.addTransientModifier(new AttributeModifier(id,
                    "CyberRaces habitat attunement", habitat.amount,
                    habitat.bonus == Bonus.SPEED
                            ? AttributeModifier.Operation.MULTIPLY_TOTAL
                            : AttributeModifier.Operation.ADDITION));
        }
    }

    public static void clear(LivingEntity entity) {
        if (entity == null) return;
        Stay old = STAYS.remove(entity);
        if (old == null) return;
        remove(entity.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_ID);
        remove(entity.getAttribute(Attributes.ARMOR), ARMOR_ID);
        remove(entity.getAttribute(Attributes.ATTACK_DAMAGE), DAMAGE_ID);
        remove(entity.getAttribute(Attributes.LUCK), LUCK_ID);
        remove(entity.getAttribute(Attributes.MAX_HEALTH), HEALTH_ID);
        if (entity.getHealth() > entity.getMaxHealth())
            entity.setHealth(entity.getMaxHealth());
        if (old.active && entity instanceof ServerPlayer player) {
            player.displayClientMessage(Component.literal("Habitat bonus faded.")
                    .withStyle(ChatFormatting.GRAY), true);
        }
    }

    private static void remove(AttributeInstance attribute, UUID id) {
        if (attribute != null && attribute.getModifier(id) != null)
            attribute.removeModifier(id);
    }

    @SubscribeEvent
    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) clear(player);
    }

    @SubscribeEvent
    public static void died(LivingDeathEvent event) {
        if (STAYS.containsKey(event.getEntity())) clear(event.getEntity());
    }

    public static Component status(ServerPlayer player) {
        Race race = RaceManager.getRace(player).orElse(null);
        if (race == null) return Component.literal("Choose a race first.");
        Habitat habitat = habitat(race);
        Stay stay = STAYS.get(player);
        String progress = stay == null ? "No qualifying biome"
                : stay.active ? "ACTIVE until you leave this biome"
                : stay.seconds + "/" + REQUIRED_SECONDS + " seconds";
        String here = player.serverLevel().getBiome(player.blockPosition())
                .unwrapKey().map(k -> k.location().toString()).orElse("unknown");
        return Component.literal(race.displayName() + " habitat: " +
                habitat.description + " | Bonus: " + bonusName(habitat)
                + " | Current: " + here + " | " + progress)
                .withStyle(ChatFormatting.AQUA);
    }
}
