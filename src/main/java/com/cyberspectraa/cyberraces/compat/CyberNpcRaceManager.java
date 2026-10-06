package com.cyberspectraa.cyberraces.compat;

import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.EntityRaceSyncPacket;
import com.cyberspectraa.cyberraces.progression.ProgressionManager;
import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceAttributeApplier;
import com.cyberspectraa.cyberraces.race.RaceEvolution;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;
import java.util.Optional;

public final class CyberNpcRaceManager {
    private static final ResourceLocation CYBER_NPC_TYPE =
        new ResourceLocation("cybernpc", "cyber_npc");

    private static final ResourceLocation ZOMBIE_CYBER_NPC_TYPE =
        new ResourceLocation("cybernpc", "zombie_cyber_npc");

    private static final String ROOT_KEY = "CyberRacesWildNpc";
    private static final String RACE_KEY = "Race";
    private static final String EVOLUTION_KEY = "Evolution";
    public static final String EVOLUTION_LOCK_TAG =
        "CyberRacesEvolutionLocked";
    private static final String APPEARANCE_KEY = "Appearance";
    private static final String NATURAL_WILD_ZOMBIE_KEY =
        "CyberNpcNaturalWildZombie";

    private static final int[] NPC_SKIN_TONES = {
        0xF2C1B2, // pale
        0xA46D33, // medium
        0x79462A, // mediumdark
        0xFCC19D, // tan
        0xDEA27B, // tanmedium
        0xC08359, // tandark
        0x5F3A1F  // dark
    };

    private static final int ZOMBIE_SKIN = 0x1E6C22;
    private static final int GOBLIN_SKIN = 0x6E9347;
    private static final int TIEFLING_SKIN = 0xB84E5C;
    private static final int NPC_BODY_TOLERANCE = 17;

    private static Class<?> cachedCyberNpcClass;
    private static Method cachedGetNpcType;
    private static Method cachedGetSkinToneIndex;

    private CyberNpcRaceManager() {
    }

    public static boolean isCyberNpc(LivingEntity entity) {
        if (entity == null) {
            return false;
        }

        ResourceLocation id = ForgeRegistries.ENTITY_TYPES
            .getKey(entity.getType());

        return CYBER_NPC_TYPE.equals(id)
            || ZOMBIE_CYBER_NPC_TYPE.equals(id);
    }

    public static boolean isZombieCyberNpc(LivingEntity entity) {
        if (entity == null) {
            return false;
        }

        ResourceLocation id = ForgeRegistries.ENTITY_TYPES
            .getKey(entity.getType());

        return ZOMBIE_CYBER_NPC_TYPE.equals(id);
    }

    public static boolean isWildCyberNpc(LivingEntity entity) {
        if (!isCyberNpc(entity)) {
            return false;
        }

        /*
         * Converted Wild NPC zombies carry CyberRacesWildNpc. Zombies that
         * replaced normal minecraft:zombie spawns carry CyberNpc's natural
         * Wild marker and should receive a new random race. Service/Main/Quest
         * conversions without either marker remain race-free.
         */
        if (isZombieCyberNpc(entity)) {
            return hasRace(entity)
                || entity.getPersistentData().getBoolean(
                    NATURAL_WILD_ZOMBIE_KEY
                );
        }

        try {
            Method method = getNpcTypeMethod(entity.getClass());
            if (method == null) {
                return false;
            }

            Object npcType = method.invoke(entity);
            return npcType != null
                && "WILD".equalsIgnoreCase(String.valueOf(npcType));
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return false;
        }
    }

    public static boolean hasRace(LivingEntity entity) {
        return getRace(entity).isPresent();
    }

    public static Optional<Race> getRace(LivingEntity entity) {
        if (entity == null) {
            return Optional.empty();
        }

        CompoundTag persistent = entity.getPersistentData();
        if (!persistent.contains(ROOT_KEY)) {
            return Optional.empty();
        }

        CompoundTag root = persistent.getCompound(ROOT_KEY);
        if (!root.contains(RACE_KEY)) {
            return Optional.empty();
        }

        return Race.byId(root.getString(RACE_KEY));
    }

    public static Optional<RaceEvolution> getEvolution(
        LivingEntity entity
    ) {
        if (entity == null) {
            return Optional.empty();
        }

        CompoundTag persistent = entity.getPersistentData();

        if (!persistent.contains(ROOT_KEY)) {
            return Optional.empty();
        }

        CompoundTag root = persistent.getCompound(ROOT_KEY);
        Race race = getRace(entity).orElse(null);

        if (race == null || !root.contains(EVOLUTION_KEY)) {
            return Optional.empty();
        }

        RaceEvolution evolution =
            RaceEvolution.byId(
                root.getString(EVOLUTION_KEY)
            ).orElse(null);

        if (evolution == null
                || evolution.baseRace() != race) {
            return Optional.empty();
        }

        return Optional.of(evolution);
    }

    public static CharacterAppearance getAppearance(LivingEntity entity) {
        if (entity == null) {
            return CharacterAppearance.defaults();
        }

        CompoundTag persistent = entity.getPersistentData();
        if (!persistent.contains(ROOT_KEY)) {
            return CharacterAppearance.defaults();
        }

        CompoundTag root = persistent.getCompound(ROOT_KEY);
        if (!root.contains(APPEARANCE_KEY)) {
            return CharacterAppearance.defaults();
        }

        return CharacterAppearance.load(
            root.getCompound(APPEARANCE_KEY)
        );
    }

    public static boolean ensureAssigned(LivingEntity entity) {
        if (!isWildCyberNpc(entity)) {
            return false;
        }

        Race race = getRace(entity).orElse(null);
        boolean created = race == null;

        if (created) {
            race = randomRace(entity);

            CompoundTag persistent = entity.getPersistentData();
            CompoundTag root = new CompoundTag();
            root.putString(RACE_KEY, race.id());
            root.put(
                APPEARANCE_KEY,
                randomAppearance(entity, race).save()
            );
            persistent.put(ROOT_KEY, root);
        }

        ensureEvolution(entity, race);

        /*
         * Natural Wild Zombie replacements skip living-skin migration. Their
         * actual body is already the CyberNpc zombie-green skin and
         * renderAppearance() handles green skin-matched race features.
         */
        if (isZombieCyberNpc(entity)) {
            RaceAttributeApplier.apply(
                entity,
                race,
                getEvolution(entity).orElse(null)
            );
            return created;
        }

        /*
         * Migrate both new and existing Wild NPCs onto skin-aware feature
         * colours. This preserves their chosen ear/tail style and fit values.
         */
        CharacterAppearance current = getAppearance(entity);
        CharacterAppearance updated =
            withNpcSkinColours(entity, race, current);

        if (!updated.equals(current)) {
            CompoundTag persistent = entity.getPersistentData();
            CompoundTag root = persistent.contains(ROOT_KEY)
                ? persistent.getCompound(ROOT_KEY)
                : new CompoundTag();

            root.putString(RACE_KEY, race.id());
            root.put(APPEARANCE_KEY, updated.save());
            persistent.put(ROOT_KEY, root);
        }

        RaceAttributeApplier.apply(
            entity,
            race,
            getEvolution(entity).orElse(null)
        );
        return created;
    }

    public static void reapply(LivingEntity entity) {
        if (!isWildCyberNpc(entity)) {
            return;
        }

        getRace(entity).ifPresent(race -> {
            ensureEvolution(entity, race);
            RaceAttributeApplier.apply(
                entity,
                race,
                getEvolution(entity).orElse(null)
            );
        });
    }

    public static boolean ensureEvolution(
        LivingEntity entity,
        Race race
    ) {
        if (entity == null
                || race == null
                || entity.getPersistentData().getBoolean(
                    EVOLUTION_LOCK_TAG
                )
                || ProgressionManager.getLevel(entity)
                    < RaceEvolution.REQUIRED_LEVEL) {
            return false;
        }

        if (getEvolution(entity).isPresent()) {
            return false;
        }

        RaceEvolution evolution =
            RaceEvolution.randomFor(
                race,
                entity.getRandom()
            ).orElse(null);

        if (evolution == null) {
            return false;
        }

        CompoundTag persistent = entity.getPersistentData();
        CompoundTag root = persistent.contains(ROOT_KEY)
            ? persistent.getCompound(ROOT_KEY)
            : new CompoundTag();

        root.putString(EVOLUTION_KEY, evolution.id());
        persistent.put(ROOT_KEY, root);
        return true;
    }

    public static double effectiveFireDamageMultiplier(
        LivingEntity entity
    ) {
        Race race = getRace(entity).orElse(null);

        if (race == null) {
            return 1.0D;
        }

        double multiplier = race.fireDamageMultiplier();

        RaceEvolution evolution =
            getEvolution(entity).orElse(null);

        if (evolution != null) {
            multiplier *=
                evolution.stats().fireDamageMultiplier();
        }

        return multiplier;
    }

    public static void clearIfNotWild(LivingEntity entity) {
        if (entity == null
            || !isCyberNpc(entity)
            || isWildCyberNpc(entity)
            || !entity.getPersistentData().contains(ROOT_KEY)) {
            return;
        }

        RaceAttributeApplier.clear(entity);
        entity.getPersistentData().remove(ROOT_KEY);
        broadcastClear(entity);
    }

    public static void syncTo(
        LivingEntity entity,
        net.minecraft.server.level.ServerPlayer receiver
    ) {
        if (!isWildCyberNpc(entity)) {
            return;
        }

        getRace(entity).ifPresent(race ->
            CyberRacesNetwork.sendToPlayer(
                receiver,
                packetFor(entity, race)
            )
        );
    }

    public static void broadcast(LivingEntity entity) {
        if (!isWildCyberNpc(entity)) {
            return;
        }

        getRace(entity).ifPresent(race ->
            CyberRacesNetwork.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY.with(() -> entity),
                packetFor(entity, race)
            )
        );
    }

    private static void broadcastClear(LivingEntity entity) {
        CyberRacesNetwork.CHANNEL.send(
            PacketDistributor.TRACKING_ENTITY.with(() -> entity),
            EntityRaceSyncPacket.clear(entity.getUUID())
        );
    }

    private static EntityRaceSyncPacket packetFor(
        LivingEntity entity,
        Race race
    ) {
        CharacterAppearance appearance =
            renderAppearance(entity, race);

        return new EntityRaceSyncPacket(
            entity.getUUID(),
            true,
            race.id(),
            getEvolution(entity)
                .map(evolution -> evolution.id())
                .orElse(""),
            appearance.featureStyle(),
            appearance.featureColor(),
            appearance.earHeight(),
            appearance.earSpread(),
            appearance.earTilt(),
            appearance.bodySourceColor(),
            appearance.bodyTargetColor(),
            appearance.bodyTolerance()
        );
    }

    private static CharacterAppearance renderAppearance(
        LivingEntity entity,
        Race race
    ) {
        CharacterAppearance saved = getAppearance(entity);

        if (!isZombieCyberNpc(entity)) {
            return saved;
        }

        int featureColor = switch (race) {
            case ELF, HALFLING, ORC, GOBLIN, FAIRY, TIEFLING ->
                ZOMBIE_SKIN;
            default -> saved.featureColor();
        };

        /*
         * The CyberNpc zombie skin itself is already green. Do not apply the
         * living Goblin/Tiefling body recolour again after conversion.
         */
        return new CharacterAppearance(
            saved.featureStyle(),
            featureColor,
            saved.earHeight(),
            saved.earSpread(),
            saved.earTilt(),
            CharacterAppearance.AUTO_COLOR,
            CharacterAppearance.AUTO_COLOR,
            saved.bodyTolerance()
        );
    }

    private static Race randomRace(LivingEntity entity) {
        Race[] races = Race.values();
        return races[entity.getRandom().nextInt(races.length)];
    }

    private static CharacterAppearance randomAppearance(
        LivingEntity entity,
        Race race
    ) {
        int variants = switch (race) {
            case CATFOLK -> 11;
            case DOGFOLK -> 9;
            case FOXFOLK -> 2;
            case BIRDFOLK -> 16;
            case HUMAN, DWARF, NYMPH, AASIMAR -> 1;
            default -> 3;
        };

        int featureStyle = variants <= 1
            ? 0
            : entity.getRandom().nextInt(variants);

        int earHeight = randomEarSetting(entity);
        int earSpread = randomEarSetting(entity);
        int earTilt = randomEarSetting(entity);

        CharacterAppearance base = new CharacterAppearance(
            featureStyle,
            CharacterAppearance.AUTO_COLOR,
            earHeight,
            earSpread,
            earTilt,
            CharacterAppearance.AUTO_COLOR,
            CharacterAppearance.AUTO_COLOR,
            CharacterAppearance.BODY_TOLERANCE_DEFAULT
        );

        return withNpcSkinColours(entity, race, base);
    }

    private static CharacterAppearance withNpcSkinColours(
        LivingEntity entity,
        Race race,
        CharacterAppearance appearance
    ) {
        int skinTone = npcSkinToneColour(entity);

        int featureColor = switch (race) {
            case ELF, HALFLING, ORC, FAIRY ->
                skinTone;
            case GOBLIN ->
                GOBLIN_SKIN;
            case TIEFLING ->
                TIEFLING_SKIN;
            default ->
                appearance.featureColor();
        };

        int bodySource = appearance.bodySourceColor();
        int bodyTarget = appearance.bodyTargetColor();

        if (race == Race.GOBLIN || race == Race.TIEFLING) {
            bodySource = skinTone;
            bodyTarget = race == Race.GOBLIN
                ? GOBLIN_SKIN
                : TIEFLING_SKIN;
        }

        return new CharacterAppearance(
            appearance.featureStyle(),
            featureColor,
            appearance.earHeight(),
            appearance.earSpread(),
            appearance.earTilt(),
            bodySource,
            bodyTarget,
            race == Race.GOBLIN || race == Race.TIEFLING
                ? NPC_BODY_TOLERANCE
                : appearance.bodyTolerance()
        );
    }

    private static int npcSkinToneColour(LivingEntity entity) {
        int index = getSkinToneIndex(entity);
        if (index < 0) {
            return CharacterAppearance.AUTO_COLOR;
        }

        index = Math.max(0, Math.min(NPC_SKIN_TONES.length - 1, index));
        return NPC_SKIN_TONES[index];
    }

    private static int getSkinToneIndex(LivingEntity entity) {
        if (entity == null || isZombieCyberNpc(entity)) {
            return -1;
        }

        try {
            Method method = getSkinToneMethod(entity.getClass());
            if (method == null) {
                return -1;
            }

            Object value = method.invoke(entity);
            return value instanceof Number number
                ? number.intValue()
                : -1;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return -1;
        }
    }

    private static int randomEarSetting(LivingEntity entity) {
        return entity.getRandom().nextInt(5) - 2;
    }

    private static Method getNpcTypeMethod(Class<?> entityClass) {
        cacheMethods(entityClass);
        return cachedGetNpcType;
    }

    private static Method getSkinToneMethod(Class<?> entityClass) {
        cacheMethods(entityClass);
        return cachedGetSkinToneIndex;
    }

    private static void cacheMethods(Class<?> entityClass) {
        if (cachedCyberNpcClass == entityClass) {
            return;
        }

        cachedCyberNpcClass = entityClass;
        cachedGetNpcType = null;
        cachedGetSkinToneIndex = null;

        try {
            cachedGetNpcType = entityClass.getMethod("getNpcType");
        } catch (NoSuchMethodException ignored) {
        }

        try {
            cachedGetSkinToneIndex =
                entityClass.getMethod("getSkinToneIndex");
        } catch (NoSuchMethodException ignored) {
        }
    }
}
