package com.cyberspectraa.cyberraces.ability;

import com.cyberspectraa.cyberraces.character.CharacterManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class NymphNatureGraceAbility {
    public static final int ACTIVE_TICKS = 8 * 20;
    public static final int COOLDOWN_TICKS = 30 * 20;

    private static final String READY_TICK_KEY =
        "NymphNatureGraceReadyTick";

    private NymphNatureGraceAbility() {
    }

    public static void tryActivate(ServerPlayer player) {
        long now = player.level().getGameTime();
        long readyAt = getReadyTick(player);

        if (readyAt > now) {
            double seconds = (readyAt - now) / 20.0D;
            player.displayClientMessage(
                Component.literal(
                    String.format(
                        "Nature's Grace: %.1fs cooldown",
                        seconds
                    )
                ),
                true
            );
            return;
        }

        player.addEffect(
            new MobEffectInstance(
                MobEffects.REGENERATION,
                ACTIVE_TICKS,
                0,
                false,
                true,
                true
            )
        );

        player.addEffect(
            new MobEffectInstance(
                MobEffects.MOVEMENT_SPEED,
                ACTIVE_TICKS,
                0,
                false,
                true,
                true
            )
        );

        setReadyTick(
            player,
            now + COOLDOWN_TICKS
        );

        if (player.level() instanceof ServerLevel level) {
            level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.ALLAY_AMBIENT_WITH_ITEM,
                SoundSource.PLAYERS,
                0.35F,
                1.25F
            );

            level.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                player.getX(),
                player.getY() + player.getBbHeight() * 0.55D,
                player.getZ(),
                8,
                player.getBbWidth() * 0.30D,
                player.getBbHeight() * 0.22D,
                player.getBbWidth() * 0.30D,
                0.01D
            );
        }

        player.displayClientMessage(
            Component.literal(
                "Nature's Grace: regeneration and speed"
            ),
            true
        );
    }

    private static long getReadyTick(ServerPlayer player) {
        CompoundTag root =
            player.getPersistentData().getCompound(
                CharacterManager.ROOT_KEY
            );

        return root.getLong(READY_TICK_KEY);
    }

    private static void setReadyTick(
        ServerPlayer player,
        long readyTick
    ) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag root =
            persistent.contains(CharacterManager.ROOT_KEY)
                ? persistent.getCompound(CharacterManager.ROOT_KEY)
                : new CompoundTag();

        root.putLong(READY_TICK_KEY, readyTick);
        persistent.put(CharacterManager.ROOT_KEY, root);
    }
}
