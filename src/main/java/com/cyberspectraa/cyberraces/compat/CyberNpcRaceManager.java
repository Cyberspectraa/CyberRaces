package com.cyberspectraa.cyberraces.compat;

import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.EntityRaceSyncPacket;
import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceAttributeApplier;
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

    private static final String ROOT_KEY = "CyberRacesWildNpc";
    private static final String RACE_KEY = "Race";
    private static final String APPEARANCE_KEY = "Appearance";

    private static Class<?> cachedCyberNpcClass;
    private static Method cachedGetNpcType;

    private CyberNpcRaceManager() {
    }

    public static boolean isCyberNpc(LivingEntity entity) {
        if (entity == null) {
            return false;
        }

        ResourceLocation id = ForgeRegistries.ENTITY_TYPES
            .getKey(entity.getType());

        return CYBER_NPC_TYPE.equals(id);
    }

    public static boolean isWildCyberNpc(LivingEntity entity) {
        if (!isCyberNpc(entity)) {
            return false;
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
            CharacterAppearance appearance =
                randomAppearance(entity, race);

            CompoundTag persistent = entity.getPersistentData();
            CompoundTag root = new CompoundTag();
            root.putString(RACE_KEY, race.id());
            root.put(APPEARANCE_KEY, appearance.save());
            persistent.put(ROOT_KEY, root);
        }

        RaceAttributeApplier.apply(entity, race);
        return created;
    }

    public static void reapply(LivingEntity entity) {
        if (!isWildCyberNpc(entity)) {
            return;
        }

        getRace(entity).ifPresent(race ->
            RaceAttributeApplier.apply(entity, race)
        );
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
        CharacterAppearance appearance = getAppearance(entity);

        return new EntityRaceSyncPacket(
            entity.getUUID(),
            true,
            race.id(),
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
            case HUMAN, DWARF -> 1;
            default -> 3;
        };

        int featureStyle = variants <= 1
            ? 0
            : entity.getRandom().nextInt(variants);

        int earHeight = randomEarSetting(entity);
        int earSpread = randomEarSetting(entity);
        int earTilt = randomEarSetting(entity);

        return new CharacterAppearance(
            featureStyle,
            CharacterAppearance.AUTO_COLOR,
            earHeight,
            earSpread,
            earTilt,
            CharacterAppearance.AUTO_COLOR,
            CharacterAppearance.AUTO_COLOR,
            CharacterAppearance.BODY_TOLERANCE_DEFAULT
        );
    }

    private static int randomEarSetting(LivingEntity entity) {
        return entity.getRandom().nextInt(5) - 2;
    }

    private static Method getNpcTypeMethod(Class<?> entityClass) {
        if (cachedCyberNpcClass == entityClass) {
            return cachedGetNpcType;
        }

        cachedCyberNpcClass = entityClass;
        cachedGetNpcType = null;

        try {
            cachedGetNpcType = entityClass.getMethod("getNpcType");
        } catch (NoSuchMethodException ignored) {
        }

        return cachedGetNpcType;
    }
}
