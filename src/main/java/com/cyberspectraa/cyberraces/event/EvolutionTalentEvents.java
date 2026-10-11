package com.cyberspectraa.cyberraces.event;

import com.cyberspectraa.cyberraces.race.RaceEvolution;
import com.cyberspectraa.cyberraces.race.RaceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Distinctive evolution passives are layered on top of the persistent
 * biome-attunement attributes. A valid biome must be occupied for 60 seconds
 * first; then the additional passive is available under its own conditions.
 *
 * Only tracked short-duration effects generated here are removed, to avoid
 * accidentally deleting long potion effects gained from other mods/items.
 */
public final class EvolutionTalentEvents {
    private record Owned(MobEffect effect, int maxDuration) {}
    private static final Map<UUID, Owned> OWNED = new HashMap<>();
    private EvolutionTalentEvents() {}

    public static void tick(ServerPlayer player) {
        if (player == null || player.tickCount % 20 != 0) return;
        RaceEvolution evo = RaceManager.getEvolution(player).orElse(null);
        if (evo == null || !BiomeAttunementService.isAttuned(player)) {
            clearOwned(player);
            return;
        }

        boolean sky = player.level().canSeeSky(player.blockPosition());
        boolean cold = player.level().getBiome(player.blockPosition()).value()
                .getBaseTemperature() < 0.2F;
        boolean nether = player.level().dimension() == Level.NETHER;
        boolean night = player.level().isNight();
        boolean daylight = player.level().isDay();
        boolean storm = player.level().isThundering() && sky;

        MobEffect next = null;
        int duration = 48;
        switch (evo) {
            case WAYFARER -> {
                if (sky && player.isSprinting()) next = MobEffects.MOVEMENT_SPEED;
            }
            case STARSEEKER -> {
                if (sky && (night || player.getY() > 135)) {
                    next = MobEffects.NIGHT_VISION;
                    duration = 240;
                }
            }
            case EMBERFORGED, RIFTWALKER, EMBERFOX -> {
                if (nether) next = MobEffects.FIRE_RESISTANCE;
            }
            case WHISPERFOOT -> {
                if (player.isShiftKeyDown() && night) next = MobEffects.MOVEMENT_SPEED;
            }
            case STORMCHASER, TEMPEST_NYMPH -> {
                if (storm) next = MobEffects.MOVEMENT_SPEED;
            }
            case CAVEWRIGHT -> {
                if (player.getY() < player.level().getSeaLevel() && !sky)
                    next = MobEffects.DIG_SPEED;
            }
            case EARTHSCALE -> {
                if (player.getY() < 35) next = MobEffects.DAMAGE_RESISTANCE;
            }
            case DEWDROP -> {
                if (player.isInWaterOrBubble()) next = MobEffects.WATER_BREATHING;
            }
            case CLOUDLEAPER -> {
                if (player.fallDistance > 1.5F) next = MobEffects.SLOW_FALLING;
            }
            case FROSTHOUND -> {
                if (cold) next = MobEffects.MOVEMENT_SPEED;
            }
            case DAWNBRINGER -> {
                if (daylight && sky && player.getHealth() < player.getMaxHealth())
                    next = MobEffects.REGENERATION;
            }
            case TIDEWING -> {
                if (player.isInWaterOrBubble()) next = MobEffects.DOLPHINS_GRACE;
            }
            default -> {
                // Older evolutions retain their permanent stats and abilities;
                // the shared biome attunement adds a new distinct conditional bonus.
            }
        }

        Owned previous = OWNED.get(player.getUUID());
        if (previous != null && (next == null || previous.effect != next)) {
            clearOwned(player);
        }
        if (next == null) return;

        MobEffectInstance current = player.getEffect(next);
        // Respect externally granted or stronger potion effects. Never replace
        // a level-two potion with our minor environmental passive.
        if (current != null && !isOurs(current, duration)) return;
        if (current == null || current.getDuration() <= 25) {
            player.addEffect(new MobEffectInstance(next, duration, 0,
                    true, false, true));
            OWNED.put(player.getUUID(), new Owned(next, duration));
        }
    }

    private static boolean isOurs(MobEffectInstance instance, int maximum) {
        return instance.getAmplifier() == 0
                && instance.isAmbient() && !instance.isVisible()
                && instance.getDuration() <= maximum;
    }

    /** Called when leaving the biome, changing race or logging out. */
    public static void clearOwned(ServerPlayer player) {
        if (player == null) return;
        Owned existing = OWNED.remove(player.getUUID());
        if (existing == null) return;
        MobEffectInstance current = player.getEffect(existing.effect);
        if (current != null && isOurs(current, existing.maxDuration)) {
            player.removeEffect(existing.effect);
        }
    }
}
