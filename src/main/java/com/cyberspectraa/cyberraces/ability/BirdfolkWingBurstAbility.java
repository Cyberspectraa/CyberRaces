package com.cyberspectraa.cyberraces.ability;

import com.cyberspectraa.cyberraces.character.CharacterManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public final class BirdfolkWingBurstAbility {
    public static final int COOLDOWN_TICKS = 7 * 20;
    private static final String READY_TICK_KEY = "BirdfolkWingBurstReadyTick";

    private BirdfolkWingBurstAbility() {
    }

    public static void tryActivate(ServerPlayer player) {
        long now = player.level().getGameTime();
        long readyAt = readyAt(player);

        if (readyAt > now) {
            player.displayClientMessage(
                Component.literal(
                    String.format(
                        "Wing Burst: %.1fs cooldown",
                        (readyAt - now) / 20.0D
                    )
                ),
                true
            );
            return;
        }

        if (player.isPassenger() || player.isInWaterOrBubble()) {
            player.displayClientMessage(
                Component.literal("Wing Burst needs open air."),
                true
            );
            return;
        }

        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);

        if (horizontal.lengthSqr() < 0.0001D) {
            horizontal = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            horizontal = horizontal.normalize();
        }

        Vec3 current = player.getDeltaMovement();
        double forward = player.onGround() ? 0.62D : 0.78D;
        double lift = player.onGround() ? 0.58D : 0.34D;

        player.setDeltaMovement(
            current.x * 0.35D + horizontal.x * forward,
            Math.max(current.y, lift),
            current.z * 0.35D + horizontal.z * forward
        );
        player.hurtMarked = true;
        player.fallDistance = 0.0F;

        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(
                ParticleTypes.CLOUD,
                player.getX(),
                player.getY() + 0.25D,
                player.getZ(),
                12,
                0.30D,
                0.14D,
                0.30D,
                0.02D
            );

            level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.PHANTOM_FLAP,
                SoundSource.PLAYERS,
                0.75F,
                1.15F
            );
        }

        setReadyAt(player, now + COOLDOWN_TICKS);
    }

    private static long readyAt(ServerPlayer player) {
        CompoundTag root = player.getPersistentData()
            .getCompound(CharacterManager.ROOT_KEY);
        return root.getLong(READY_TICK_KEY);
    }

    private static void setReadyAt(ServerPlayer player, long readyAt) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.contains(CharacterManager.ROOT_KEY)
            ? persistent.getCompound(CharacterManager.ROOT_KEY)
            : new CompoundTag();

        root.putLong(READY_TICK_KEY, readyAt);
        persistent.put(CharacterManager.ROOT_KEY, root);
    }
}
