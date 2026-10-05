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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class OrcWarCryAbility {
    public static final int ACTIVE_TICKS = 8 * 20;
    public static final int COOLDOWN_TICKS = 30 * 20;

    private static final UUID KNOCKBACK_ID =
        UUID.fromString("57e0e4c5-e74e-44ab-a921-56c7bb10f52a");

    private static final String READY_TICK_KEY =
        "OrcWarCryReadyTick";

    private static final Map<UUID, Long> ACTIVE_UNTIL =
        new HashMap<>();

    private OrcWarCryAbility() {
    }

    public static void tryActivate(ServerPlayer player) {
        long now = player.level().getGameTime();
        long readyAt = getReadyTick(player);

        if (readyAt > now) {
            double seconds = (readyAt - now) / 20.0D;
            player.displayClientMessage(
                Component.literal(
                    String.format(
                        "War Cry: %.1fs cooldown",
                        seconds
                    )
                ),
                true
            );
            return;
        }

        player.addEffect(
            new MobEffectInstance(
                MobEffects.DAMAGE_BOOST,
                ACTIVE_TICKS,
                0,
                false,
                true,
                true
            )
        );

        AttributeInstance knockback =
            player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);

        if (knockback != null) {
            knockback.removeModifier(KNOCKBACK_ID);
            knockback.addTransientModifier(
                new AttributeModifier(
                    KNOCKBACK_ID,
                    "CyberRaces Orc War Cry",
                    0.35D,
                    AttributeModifier.Operation.ADDITION
                )
            );
        }

        ACTIVE_UNTIL.put(
            player.getUUID(),
            now + ACTIVE_TICKS
        );

        setReadyTick(
            player,
            now + COOLDOWN_TICKS
        );

        if (player.level() instanceof ServerLevel level) {
            level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.RAVAGER_ROAR,
                SoundSource.PLAYERS,
                0.65F,
                0.90F
            );

            level.sendParticles(
                ParticleTypes.CRIT,
                player.getX(),
                player.getY() + player.getBbHeight() * 0.55D,
                player.getZ(),
                14,
                player.getBbWidth() * 0.45D,
                player.getBbHeight() * 0.30D,
                player.getBbWidth() * 0.45D,
                0.04D
            );
        }

        player.displayClientMessage(
            Component.literal(
                "War Cry: Strength and knockback resistance"
            ),
            true
        );
    }

    public static void tick(ServerPlayer player) {
        Long until = ACTIVE_UNTIL.get(player.getUUID());
        if (until == null) {
            return;
        }

        if (player.level().getGameTime() >= until) {
            cleanup(player);
        }
    }

    public static void cleanup(ServerPlayer player) {
        ACTIVE_UNTIL.remove(player.getUUID());

        AttributeInstance knockback =
            player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);

        if (knockback != null) {
            knockback.removeModifier(KNOCKBACK_ID);
        }
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
