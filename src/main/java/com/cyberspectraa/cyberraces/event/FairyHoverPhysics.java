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
    private static final double LOWER_HOVER_HEIGHT = 2.90D;
    private static final double UPPER_HOVER_HEIGHT = 4.20D;
    private static final double GROUND_SCAN = 6.00D;

    private static final double LIFT_SPEED = 0.065D;
    private static final double FLOAT_DESCENT = -0.015D;
    private static final double SAFE_DESCENT = -0.12D;
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
            // Once a jump starts to lose momentum, gently carry the Fairy up
            // into the hover zone rather than snapping to a fixed Y level.
            nextY = Math.max(movement.y, LIFT_SPEED);
        } else if (height <= UPPER_HOVER_HEIGHT) {
            // Inside the hover band, allow a tiny downward drift. This feels
            // much less rigid than forcing zero velocity every tick.
            if (movement.y < FLOAT_DESCENT) {
                nextY = FLOAT_DESCENT;
            }
        } else {
            // Above the normal hover zone, simply soften the descent. Do not
            // pull the player downward toward an exact target.
            if (movement.y < SAFE_DESCENT) {
                nextY = SAFE_DESCENT;
            }
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
