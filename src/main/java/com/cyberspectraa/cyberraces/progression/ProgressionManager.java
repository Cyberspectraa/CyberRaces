package com.cyberspectraa.cyberraces.progression;

import com.cyberspectraa.cyberraces.race.Race;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.registries.ForgeRegistries;

public final class ProgressionManager {
    public static final String ROOT_KEY = "CyberProgression";
    public static final int DATA_VERSION = 1;
    public static final int MAX_LEVEL = 1000;

    private static final String VERSION_KEY = "Version";
    private static final String LEVEL_KEY = "Level";
    private static final String TOTAL_XP_KEY = "TotalXp";

    private ProgressionManager() {
    }

    public static int getLevel(LivingEntity entity) {
        return read(entity).getInt(LEVEL_KEY);
    }

    public static long getTotalExperience(LivingEntity entity) {
        return read(entity).getLong(TOTAL_XP_KEY);
    }

    public static long getExperienceIntoLevel(LivingEntity entity) {
        int level = getLevel(entity);
        return Math.max(
            0L,
            getTotalExperience(entity) - totalExperienceForLevel(level)
        );
    }

    public static long getExperienceForNextLevel(LivingEntity entity) {
        return experienceForNextLevel(getLevel(entity));
    }

    public static long experienceForNextLevel(int level) {
        int safeLevel = Math.max(1, Math.min(MAX_LEVEL, level));

        if (safeLevel >= MAX_LEVEL) {
            return 0L;
        }

        long n = safeLevel - 1L;
        return 100L + 35L * n + 3L * n * n;
    }

    public static long totalExperienceForLevel(int level) {
        int safeLevel = Math.max(1, Math.min(MAX_LEVEL, level));
        long m = safeLevel - 1L;

        if (m <= 0L) {
            return 0L;
        }

        return 100L * m
            + (35L * m * (m - 1L)) / 2L
            + ((m - 1L) * m * (2L * m - 1L)) / 2L;
    }

    public static ProgressionChange addExperience(
        LivingEntity entity,
        long baseAmount,
        String source
    ) {
        if (entity == null
                || entity.level().isClientSide
                || baseAmount <= 0L) {
            int level = entity == null ? 1 : getLevel(entity);
            long total = entity == null ? 0L : getTotalExperience(entity);
            return new ProgressionChange(level, level, 0L, total, source);
        }

        CompoundTag root = read(entity);
        int oldLevel = root.getInt(LEVEL_KEY);
        long oldTotal = root.getLong(TOTAL_XP_KEY);

        if (oldLevel >= MAX_LEVEL) {
            return new ProgressionChange(
                oldLevel,
                oldLevel,
                0L,
                oldTotal,
                source
            );
        }

        long applied = Math.max(
            1L,
            Math.round(baseAmount * experienceMultiplier(entity))
        );

        long maxTotal = totalExperienceForLevel(MAX_LEVEL);
        long newTotal = Math.min(maxTotal, safeAdd(oldTotal, applied));
        int newLevel = levelForTotalExperience(newTotal);

        root.putInt(VERSION_KEY, DATA_VERSION);
        root.putInt(LEVEL_KEY, newLevel);
        root.putLong(TOTAL_XP_KEY, newTotal);
        write(entity, root);

        if (newLevel > oldLevel) {
            onLevelUp(entity, oldLevel, newLevel);
        }

        return new ProgressionChange(
            oldLevel,
            newLevel,
            newTotal - oldTotal,
            newTotal,
            source
        );
    }

    public static ProgressionChange awardCombatKill(
        LivingEntity killer,
        LivingEntity victim
    ) {
        long amount = combatExperience(victim);

        if (amount <= 0L) {
            int level = killer == null ? 1 : getLevel(killer);
            long total = killer == null ? 0L : getTotalExperience(killer);
            return new ProgressionChange(
                level,
                level,
                0L,
                total,
                "combat"
            );
        }

        return addExperience(killer, amount, "combat");
    }

    public static long combatExperience(LivingEntity victim) {
        if (victim == null) {
            return 0L;
        }

        var typeId = ForgeRegistries.ENTITY_TYPES.getKey(victim.getType());

        // Killing CyberNpc characters should never become an XP farm.
        if (typeId != null && "cybernpc".equals(typeId.getNamespace())) {
            return 0L;
        }

        if (victim instanceof EnderDragon || victim instanceof WitherBoss) {
            return 750L;
        }

        if (!(victim instanceof Enemy)) {
            return 0L;
        }

        return Math.max(
            12L,
            Math.min(
                120L,
                Math.round(12.0D + victim.getMaxHealth() * 0.45D)
            )
        );
    }

    public static void setLevel(LivingEntity entity, int level) {
        if (entity == null || entity.level().isClientSide) {
            return;
        }

        int safeLevel = Math.max(1, Math.min(MAX_LEVEL, level));
        CompoundTag root = read(entity);
        int oldLevel = root.getInt(LEVEL_KEY);

        root.putInt(VERSION_KEY, DATA_VERSION);
        root.putInt(LEVEL_KEY, safeLevel);
        root.putLong(
            TOTAL_XP_KEY,
            totalExperienceForLevel(safeLevel)
        );
        write(entity, root);

        if (safeLevel > oldLevel) {
            onLevelUp(entity, oldLevel, safeLevel);
        }
    }

    public static void copyData(
        LivingEntity oldEntity,
        LivingEntity newEntity
    ) {
        if (oldEntity == null || newEntity == null) {
            return;
        }

        CompoundTag oldPersistent = oldEntity.getPersistentData();

        if (oldPersistent.contains(ROOT_KEY)) {
            newEntity.getPersistentData().put(
                ROOT_KEY,
                oldPersistent.getCompound(ROOT_KEY).copy()
            );
        } else {
            ensure(newEntity);
        }
    }

    public static void ensure(LivingEntity entity) {
        if (entity != null) {
            read(entity);
        }
    }

    private static CompoundTag read(LivingEntity entity) {
        CompoundTag persistent = entity.getPersistentData();
        CompoundTag root = persistent.contains(ROOT_KEY)
            ? persistent.getCompound(ROOT_KEY)
            : new CompoundTag();

        int version = root.contains(VERSION_KEY)
            ? root.getInt(VERSION_KEY)
            : 0;

        if (version < DATA_VERSION) {
            migrate(root, version);
        }

        int level = root.contains(LEVEL_KEY)
            ? Math.max(1, Math.min(MAX_LEVEL, root.getInt(LEVEL_KEY)))
            : 1;

        long totalXp = root.contains(TOTAL_XP_KEY)
            ? Math.max(0L, root.getLong(TOTAL_XP_KEY))
            : totalExperienceForLevel(level);

        int calculatedLevel = levelForTotalExperience(totalXp);

        root.putInt(VERSION_KEY, DATA_VERSION);
        root.putInt(LEVEL_KEY, calculatedLevel);
        root.putLong(TOTAL_XP_KEY, totalXp);
        persistent.put(ROOT_KEY, root);

        return root;
    }

    private static void write(
        LivingEntity entity,
        CompoundTag root
    ) {
        entity.getPersistentData().put(ROOT_KEY, root);
    }

    private static void migrate(CompoundTag root, int oldVersion) {
        // Version 1 is the first public progression format. Keep migration
        // centralized here so later curve/data changes can preserve worlds.
        if (oldVersion <= 0) {
            if (!root.contains(LEVEL_KEY)) {
                root.putInt(LEVEL_KEY, 1);
            }
            if (!root.contains(TOTAL_XP_KEY)) {
                root.putLong(
                    TOTAL_XP_KEY,
                    totalExperienceForLevel(
                        Math.max(1, root.getInt(LEVEL_KEY))
                    )
                );
            }
        }

        root.putInt(VERSION_KEY, DATA_VERSION);
    }

    private static int levelForTotalExperience(long totalXp) {
        long safeXp = Math.max(0L, totalXp);
        int low = 1;
        int high = MAX_LEVEL;

        while (low < high) {
            int mid = low + (high - low + 1) / 2;

            if (totalExperienceForLevel(mid) <= safeXp) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }

        return low;
    }

    private static double experienceMultiplier(LivingEntity entity) {
        String raceId = raceId(entity);
        return Race.HUMAN.id().equals(raceId) ? 1.05D : 1.0D;
    }

    private static String raceId(LivingEntity entity) {
        CompoundTag persistent = entity.getPersistentData();

        if (entity instanceof ServerPlayer
                && persistent.contains("CyberRaces")) {
            return persistent
                .getCompound("CyberRaces")
                .getString("Race");
        }

        if (persistent.contains("CyberRacesWildNpc")) {
            return persistent
                .getCompound("CyberRacesWildNpc")
                .getString("Race");
        }

        return "";
    }

    private static long safeAdd(long left, long right) {
        if (right > 0L && left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }

    private static void onLevelUp(
        LivingEntity entity,
        int oldLevel,
        int newLevel
    ) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        player.sendSystemMessage(
            Component.literal("Cyber Level ")
                .withStyle(ChatFormatting.GOLD)
                .append(
                    Component.literal(
                        oldLevel + " -> " + newLevel
                    ).withStyle(ChatFormatting.YELLOW)
                )
        );

        player.playNotifySound(
            SoundEvents.PLAYER_LEVELUP,
            SoundSource.PLAYERS,
            0.75F,
            1.0F
        );
    }

    public record ProgressionChange(
        int oldLevel,
        int newLevel,
        long experienceAdded,
        long totalExperience,
        String source
    ) {
        public boolean leveledUp() {
            return newLevel > oldLevel;
        }
    }
}
