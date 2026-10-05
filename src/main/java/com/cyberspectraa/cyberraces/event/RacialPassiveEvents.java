package com.cyberspectraa.cyberraces.event;

import com.cyberspectraa.cyberraces.ability.DragonBreathAbility;
import com.cyberspectraa.cyberraces.compat.CyberNpcRaceManager;
import com.cyberspectraa.cyberraces.compat.IronSpellsCompat;
import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;
import java.util.UUID;

public final class RacialPassiveEvents {
    private static final UUID FOX_SNEAK_SPEED_ID =
        UUID.fromString("f9074b94-851b-45cf-94d3-63a68194f71d");

    private static final String HUMAN_XP_REMAINDER =
        "HumanXpRemainder";

    private RacialPassiveEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
            || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        updateFoxSneakSpeed(player);

        if (IronSpellsCompat.isLoaded()) {
            DragonBreathAbility.tick(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
            && IronSpellsCompat.isLoaded()) {
            DragonBreathAbility.cleanup(player);
        }
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();

        if (!(entity instanceof ServerPlayer player)
            || RaceManager.getRace(player).orElse(null) != Race.CATFOLK
            || player.isInWaterOrBubble()
            || player.isSwimming()) {
            return;
        }

        var motion = player.getDeltaMovement();
        player.setDeltaMovement(
            motion.x,
            motion.y * 1.12D,
            motion.z
        );
        player.hurtMarked = true;
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
            || RaceManager.getRace(player).orElse(null) != Race.DWARF) {
            return;
        }

        if (player.getY() < player.level().getSeaLevel()
            && !player.level().canSeeSky(player.blockPosition())) {
            event.setNewSpeed(event.getNewSpeed() * 1.15F);
        }
    }

    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
            || RaceManager.getRace(player).orElse(null) != Race.ELF) {
            return;
        }

        int improvedCharge = Math.min(
            20,
            Math.round(event.getCharge() * 1.15F)
        );

        event.setCharge(improvedCharge);
    }

    @SubscribeEvent
    public static void onXpChange(PlayerXpEvent.XpChange event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
            || event.getAmount() <= 0
            || RaceManager.getRace(player).orElse(null) != Race.HUMAN) {
            return;
        }

        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.contains("CyberRaces")
            ? persistent.getCompound("CyberRaces")
            : new CompoundTag();

        double remainder = root.getDouble(HUMAN_XP_REMAINDER);
        double exactBonus = remainder + event.getAmount() * 0.05D;
        int wholeBonus = (int) Math.floor(exactBonus);

        root.putDouble(
            HUMAN_XP_REMAINDER,
            exactBonus - wholeBonus
        );
        persistent.put("CyberRaces", root);

        if (wholeBonus > 0) {
            event.setAmount(event.getAmount() + wholeBonus);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        Race targetRace = resolveRace(target).orElse(null);

        if (targetRace == Race.CATFOLK
            && event.getSource().is(DamageTypeTags.IS_FALL)) {
            event.setAmount(event.getAmount() * 0.40F);
        } else if (targetRace == Race.HALFLING
            && event.getSource().is(DamageTypeTags.IS_FALL)) {
            event.setAmount(event.getAmount() * 0.65F);
        }

        if (targetRace == Race.TIEFLING
            && event.getSource().is(DamageTypeTags.IS_FIRE)
            && target.level() instanceof ServerLevel level) {
            level.sendParticles(
                ParticleTypes.FLAME,
                target.getX(),
                target.getY() + target.getBbHeight() * 0.55D,
                target.getZ(),
                8,
                target.getBbWidth() * 0.35D,
                target.getBbHeight() * 0.25D,
                target.getBbWidth() * 0.35D,
                0.015D
            );
        }

        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            Race attackerRace = resolveRace(attacker).orElse(null);

            if (attackerRace == Race.ORC
                && attacker.getMaxHealth() > 0.0F
                && attacker.getHealth() / attacker.getMaxHealth() <= 0.35F) {
                event.setAmount(event.getAmount() * 1.20F);
            }
        }
    }

    private static void updateFoxSneakSpeed(ServerPlayer player) {
        AttributeInstance speed =
            player.getAttribute(Attributes.MOVEMENT_SPEED);

        if (speed == null) {
            return;
        }

        boolean shouldHaveBoost =
            RaceManager.getRace(player).orElse(null) == Race.FOXFOLK
                && player.isShiftKeyDown()
                && !player.isPassenger();

        AttributeModifier existing =
            speed.getModifier(FOX_SNEAK_SPEED_ID);

        if (shouldHaveBoost && existing == null) {
            speed.addTransientModifier(
                new AttributeModifier(
                    FOX_SNEAK_SPEED_ID,
                    "CyberRaces Foxfolk sneak speed",
                    0.20D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
                )
            );
        } else if (!shouldHaveBoost && existing != null) {
            speed.removeModifier(FOX_SNEAK_SPEED_ID);
        }
    }

    private static Optional<Race> resolveRace(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            return RaceManager.getRace(player);
        }

        if (CyberNpcRaceManager.isWildCyberNpc(entity)) {
            return CyberNpcRaceManager.getRace(entity);
        }

        return Optional.empty();
    }
}
