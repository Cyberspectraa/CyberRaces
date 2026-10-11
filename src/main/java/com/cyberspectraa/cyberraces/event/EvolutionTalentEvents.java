package com.cyberspectraa.cyberraces.event;

import com.cyberspectraa.cyberraces.race.RaceEvolution;
import com.cyberspectraa.cyberraces.race.RaceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;

/**
 * Small situational passives for the fifteen newly added original-race paths.
 * The main stat bonuses still come from RaceEvolutionStats; these only apply
 * to PLAYERS, never NPCs (which no longer receive evolutions).
 */
public final class EvolutionTalentEvents {
    private EvolutionTalentEvents() {}

    public static void tick(ServerPlayer player) {
        if (player == null || player.tickCount % 60 != 0) return;
        RaceEvolution evo = RaceManager.getEvolution(player).orElse(null);
        if (evo == null) return;

        boolean sky = player.level().canSeeSky(player.blockPosition());
        boolean cold = player.level().getBiome(player.blockPosition()).value()
                .getBaseTemperature() < 0.2F;
        boolean nether = player.level().dimension() == Level.NETHER;
        boolean night = player.level().isNight();
        boolean daylight = player.level().isDay();
        boolean storm = player.level().isThundering() && sky;

        switch (evo) {
            case WAYFARER -> {
                if (sky && player.isSprinting())
                    add(player, MobEffects.MOVEMENT_SPEED);
            }
            case STARSEEKER -> {
                if (sky && (night || player.getY() > 135))
                    add(player, MobEffects.NIGHT_VISION, 240);
            }
            case EMBERFORGED, RIFTWALKER, EMBERFOX -> {
                if (nether) add(player, MobEffects.FIRE_RESISTANCE);
            }
            case WHISPERFOOT -> {
                if (player.isShiftKeyDown() && night)
                    add(player, MobEffects.MOVEMENT_SPEED);
            }
            case STORMCHASER, TEMPEST_NYMPH -> {
                if (storm) add(player, MobEffects.MOVEMENT_SPEED);
            }
            case CAVEWRIGHT -> {
                if (player.getY() < player.level().getSeaLevel()
                        && !sky)
                    add(player, MobEffects.DIG_SPEED);
            }
            case EARTHSCALE -> {
                if (player.getY() < 35)
                    add(player, MobEffects.DAMAGE_RESISTANCE);
            }
            case DEWDROP -> {
                if (player.isInWaterOrBubble())
                    add(player, MobEffects.WATER_BREATHING);
            }
            case CLOUDLEAPER -> {
                if (player.fallDistance > 1.5F)
                    add(player, MobEffects.SLOW_FALLING);
            }
            case FROSTHOUND -> {
                if (cold) add(player, MobEffects.MOVEMENT_SPEED);
            }
            case DAWNBRINGER -> {
                if (daylight && sky && player.getHealth() < player.getMaxHealth())
                    add(player, MobEffects.REGENERATION);
            }
            case TIDEWING -> {
                if (player.isInWaterOrBubble())
                    add(player, MobEffects.DOLPHINS_GRACE);
            }
            default -> {
                // Existing paths continue using their original stats/abilities.
            }
        }
    }

    private static void add(ServerPlayer player, MobEffect effect) {
        add(player, effect, 90);
    }

    private static void add(ServerPlayer player, MobEffect effect, int duration) {
        MobEffectInstance current = player.getEffect(effect);
        if (current != null && (current.getAmplifier() > 0
                || current.getDuration() >= 65)) return;
        // Ambient, without obstructive particles; wears off soon when the
        // environmental requirement is no longer met.
        player.addEffect(new MobEffectInstance(effect, duration, 0,
                true, false, true));
    }
}
