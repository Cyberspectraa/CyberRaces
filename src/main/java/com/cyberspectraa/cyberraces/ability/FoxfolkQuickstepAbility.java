package com.cyberspectraa.cyberraces.ability;

import com.cyberspectraa.cyberraces.character.CharacterManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class FoxfolkQuickstepAbility {
    public static final int COOLDOWN_TICKS = 6 * 20;

    private static final String READY_TICK_KEY =
        "FoxfolkQuickstepReadyTick";

    private FoxfolkQuickstepAbility() {
    }

    public static void tryActivate(ServerPlayer player) {
        long now = player.level().getGameTime();
        long readyAt = getReadyTick(player);

        if (readyAt > now) {
            double seconds = (readyAt - now) / 20.0D;
            player.displayClientMessage(
                Component.literal(
                    String.format(
                        "Quickstep: %.1fs cooldown",
                        seconds
                    )
                ),
                true
            );
            return;
        }

        if (player.isPassenger()
            || player.isFallFlying()
            || player.isInWaterOrBubble()
            || player.isInLava()) {
            player.displayClientMessage(
                Component.literal(
                    "Quickstep needs solid footing."
                ),
                true
            );
            return;
        }

        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(
            look.x,
            0.0D,
            look.z
        );

        if (horizontal.lengthSqr() < 0.0001D) {
            return;
        }

        horizontal = horizontal.normalize();

        Vec3 current = player.getDeltaMovement();

        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(
                ParticleTypes.POOF,
                player.getX(),
                player.getY() + 0.35D,
                player.getZ(),
                8,
                0.20D,
                0.10D,
                0.20D,
                0.01D
            );
        }

        player.setDeltaMovement(
            horizontal.x * 1.35D,
            Math.max(current.y, 0.14D),
            horizontal.z * 1.35D
        );
        player.hurtMarked = true;
        player.fallDistance = 0.0F;

        setReadyTick(
            player,
            now + COOLDOWN_TICKS
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
