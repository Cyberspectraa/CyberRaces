package com.cyberspectraa.cyberraces.event;

import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.SlimeParticleColor;
import com.cyberspectraa.cyberraces.character.CharacterManager;
import com.cyberspectraa.cyberraces.race.RaceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Slime movement is naturally springy, with no V ability.
 * Server-owned movement and particle events don't depend on the ordinary
 * vanilla sprint effect being emitted by any particular animation mod.
 */
public final class SlimeMovementEvents {
    private static final Map<UUID, Long> BOUNCE_PENDING = new HashMap<>();
    private static final Set<UUID> WAS_AIRBORNE = new HashSet<>();
    private SlimeMovementEvents() {}

    private static boolean isSlime(ServerPlayer player) {
        return RaceManager.getRace(player).orElse(null) == Race.SLIME;
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !isSlime(player)) return;

        float distance = event.getDistance();
        event.setDistance(0.0F);
        event.setDamageMultiplier(0.0F);

        // Sneaking deliberately absorbs the landing instead of rebounding.
        if (distance >= 3.0F && !player.isShiftKeyDown()
                && !player.isInWaterOrBubble()) {
            BOUNCE_PENDING.put(player.getUUID(),
                    player.level().getGameTime() + 8);
        }
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !isSlime(player) || player.isInWaterOrBubble()) return;
        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(velocity.x, velocity.y * 1.14D, velocity.z);
        player.hurtMarked = true;
        player.serverLevel().sendParticles(SlimeParticleColor.forAppearance(CharacterManager.getAppearance(player)),
                player.getX(), player.getY() + 0.08D, player.getZ(),
                12, 0.28D, 0.03D, 0.28D, 0.045D);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)) return;
        UUID id = player.getUUID();
        if (!isSlime(player)) {
            BOUNCE_PENDING.remove(id);
            WAS_AIRBORNE.remove(id);
            return;
        }

        boolean grounded = player.onGround();
        if (!grounded && player.getDeltaMovement().y < -0.05D) {
            WAS_AIRBORNE.add(id);
        } else if (grounded && WAS_AIRBORNE.remove(id)) {
            player.serverLevel().sendParticles(SlimeParticleColor.forAppearance(CharacterManager.getAppearance(player)),
                    player.getX(), player.getY() + 0.06D, player.getZ(),
                    16, 0.36D, 0.045D, 0.36D, 0.055D);
        }

        Long until = BOUNCE_PENDING.get(id);
        if (until != null) {
            if (until < player.level().getGameTime()
                    || player.isShiftKeyDown() || player.isInWaterOrBubble()) {
                BOUNCE_PENDING.remove(id);
            } else if (grounded) {
                BOUNCE_PENDING.remove(id);
                Vec3 motion = player.getDeltaMovement();
                player.setDeltaMovement(motion.x, 0.50D, motion.z);
                player.hurtMarked = true;
                player.serverLevel().sendParticles(SlimeParticleColor.forAppearance(CharacterManager.getAppearance(player)),
                        player.getX(), player.getY() + 0.08D, player.getZ(),
                        18, 0.36D, 0.06D, 0.36D, 0.065D);
            }
        }

        // Visible slime splashes even when movement/animation mods suppress
        // vanilla block-dust sprint particle callbacks.
        Vec3 velocity = player.getDeltaMovement();
        if (grounded && player.tickCount % 4 == 0
                && velocity.horizontalDistanceSqr() > 0.004D
                && !player.isPassenger()) {
            player.serverLevel().sendParticles(SlimeParticleColor.forAppearance(CharacterManager.getAppearance(player)),
                    player.getX(), player.getY() + 0.05D, player.getZ(),
                    3, 0.22D, 0.025D, 0.22D, 0.025D);
        }
    }

    @SubscribeEvent
    public static void onLogout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        BOUNCE_PENDING.remove(id);
        WAS_AIRBORNE.remove(id);
    }
}
