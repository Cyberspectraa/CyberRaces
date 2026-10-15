package com.cyberspectraa.cyberraces.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Shared Fairy hover movement used on both the local client and server.
 *
 * This intentionally behaves like a soft floating floor instead of a
 * position lock. Horizontal movement is never modified.
 */
public final class FairyHoverPhysics {
    // A small dead zone keeps hover stable instead of alternating
    // between a lift tick and a fall tick at the exact same elevation.
    private static final double LOWER_HOVER_HEIGHT = 3.20D;
    private static final double UPPER_HOVER_HEIGHT = 3.80D;
    private static final double GROUND_SCAN = 6.00D;

    private static final double LIFT_SPEED = 0.080D;
    private static final double SAFE_DESCENT = -0.065D;
    private static final double CROUCH_MIN_DESCENT = -0.08D;
    private static final double CROUCH_MAX_DESCENT = -0.17D;

    private FairyHoverPhysics() {
    }

    public static boolean canHover(Player player) {
        return player != null
            && player.isAlive()
            && !player.isSpectator()
            && !player.onGround()
            && !player.isPassenger()
            && !player.isFallFlying()
            && !player.isSwimming()
            && !player.isInWaterOrBubble()
            && !player.isInLava()
            && !player.getAbilities().flying;
    }

    /** Allow enabling hover from the ground without needing a jump first. */
    public static void takeoff(Player player) {
        if (player == null || player.isPassenger() || player.isInWaterOrBubble()
                || player.isFallFlying() || player.getAbilities().flying) return;
        if (player.onGround()) {
            Vec3 movement = player.getDeltaMovement();
            player.setDeltaMovement(
                    movement.x, Math.max(0.28D, movement.y), movement.z);
            player.fallDistance = 0.0F;
            // ServerPlayer has to send the initial lift to the client.
            if (player instanceof net.minecraft.server.level.ServerPlayer server)
                server.hurtMarked = true;
        }
    }

    /**
     * Returns true when this tick changed the player's vertical motion.
     */
    public static boolean apply(Player player) {
        if (!canHover(player)) {
            return false;
        }

        BlockHitResult ground = findGround(player);
        if (ground == null) {
            // Real cliffs and long falls remain real falls. Icarus is still
            // the Fairy's long-distance flight option.
            return false;
        }

        double height =
            player.getY() - ground.getLocation().y;

        Vec3 movement = player.getDeltaMovement();
        double nextY = movement.y;

        if (player.isShiftKeyDown()) {
            // Crouching deliberately breaks the float and gives a smooth,
            // predictable landing descent.
            nextY = Math.max(
                CROUCH_MAX_DESCENT,
                Math.min(movement.y, CROUCH_MIN_DESCENT)
            );
        } else if (movement.y > 0.10D) {
            // Never squash the player's normal jump/upward momentum.
            return false;
        } else if (height < LOWER_HOVER_HEIGHT) {
            // Gradually rise toward the 3-4 block band; avoid a large
            // instantaneous shove when stepping across uneven terrain.
            double need = LOWER_HOVER_HEIGHT - height;
            nextY = Math.min(0.095D, Math.max(0.025D,
                    LIFT_SPEED * Math.min(1.0D, need + 0.25D)));
        } else if (height <= UPPER_HOVER_HEIGHT) {
            // Within the band cancel gravity instead of oscillating
            // between upward boost and downward drift every few ticks.
            nextY = 0.0D;
        } else {
            // Above the band, descend gently while still allowing the
            // player to steer horizontally without any movement changes.
            nextY = Math.max(SAFE_DESCENT,
                    -0.018D - (height - UPPER_HOVER_HEIGHT) * 0.025D);
        }

        if (Math.abs(nextY - movement.y) < 0.0005D) {
            player.fallDistance = 0.0F;
            return false;
        }

        player.setDeltaMovement(
            movement.x,
            nextY,
            movement.z
        );
        player.fallDistance = 0.0F;
        return true;
    }

    private static BlockHitResult findGround(Player player) {
        Vec3 start = new Vec3(
            player.getX(),
            player.getY() + 0.05D,
            player.getZ()
        );

        Vec3 end = new Vec3(
            player.getX(),
            player.getY() - GROUND_SCAN,
            player.getZ()
        );

        BlockHitResult hit = player.level().clip(
            new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
            )
        );

        return hit.getType() == HitResult.Type.BLOCK
            ? hit
            : null;
    }
}
