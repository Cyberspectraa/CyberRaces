package com.cyberspectraa.cyberraces.event;

import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Gives player Fairies a short-range ground hover without granting creative
 * flight. Icarus remains the race's proper long-distance flight system.
 *
 * The hover only engages after the Fairy leaves the ground. Holding crouch
 * turns the hover into a gentle descent so landing remains deliberate.
 */
public final class FairyHoverEvents {
    private static final double TARGET_HEIGHT = 3.40D;
    private static final double LOWER_BAND = 3.05D;
    private static final double UPPER_BAND = 3.75D;
    private static final double GROUND_SCAN = 5.75D;

    private static final double MAX_RISE = 0.18D;
    private static final double MAX_SETTLE = -0.10D;
    private static final double CROUCH_DESCENT = -0.16D;

    private FairyHoverEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
            || !(event.player instanceof ServerPlayer player)
            || player.isSpectator()
            || !player.isAlive()) {
            return;
        }

        if (RaceManager.getRace(player).orElse(null) != Race.FAIRY) {
            return;
        }

        if (player.onGround()
            || player.isPassenger()
            || player.isFallFlying()
            || player.isSwimming()
            || player.isInWaterOrBubble()
            || player.isInLava()
            || player.getAbilities().flying) {
            return;
        }

        BlockHitResult ground = findGround(player);
        if (ground == null) {
            // No nearby floor means this is a genuine fall/cliff. Hover is
            // deliberately short-range rather than infinite slow falling.
            return;
        }

        double height = player.getY() - ground.getLocation().y;
        Vec3 movement = player.getDeltaMovement();
        double nextY;

        if (player.isShiftKeyDown()) {
            // Crouch is the intentional "land" control. Keep the descent
            // gentle and suppress fall damage while a nearby floor exists.
            nextY = Math.max(movement.y, CROUCH_DESCENT);
        } else if (height < LOWER_BAND) {
            double lift =
                Mth.clamp(
                    (TARGET_HEIGHT - height) * 0.075D,
                    0.045D,
                    MAX_RISE
                );

            // Preserve some upward momentum from the initial jump, but stop a
            // normal jump from shooting far above the hover envelope.
            nextY = movement.y > 0.0D
                ? Math.min(MAX_RISE, Math.max(lift, movement.y * 0.72D))
                : lift;
        } else if (height > UPPER_BAND) {
            double settle =
                -Mth.clamp(
                    (height - TARGET_HEIGHT) * 0.055D,
                    0.025D,
                    -MAX_SETTLE
                );

            nextY = Math.max(MAX_SETTLE, Math.min(movement.y, settle));
        } else {
            // Inside the hover band, damp vertical movement into a stable
            // float instead of snapping Y to a fixed position.
            nextY = movement.y * 0.38D;

            if (Math.abs(nextY) < 0.012D) {
                nextY = 0.0D;
            }
        }

        player.setDeltaMovement(
            movement.x,
            nextY,
            movement.z
        );

        player.fallDistance = 0.0F;

        // ServerPlayer movement packets normally coalesce tiny velocity
        // changes. Mark it so the client sees the hover smoothly.
        player.hurtMarked = true;
    }

    private static BlockHitResult findGround(ServerPlayer player) {
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
