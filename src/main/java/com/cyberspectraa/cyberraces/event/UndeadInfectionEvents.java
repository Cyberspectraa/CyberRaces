package com.cyberspectraa.cyberraces.event;

import com.cyberspectraa.cyberraces.character.CharacterManager;
import com.cyberspectraa.cyberraces.character.CharacterSyncService;
import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Player infection is an acquired racial state, never a new base race.
 * The 120-second cybernpc:zombification effect remains owned by CyberNpc,
 * and the Wild NPC conversion implementation is untouched.
 *
 * Cure ritual matches zombie villagers: Weakness + a regular golden apple,
 * followed by a 3-5 minute conversion wait. Teammates right-click the
 * afflicted player, while solo players can eat the apple themselves.
 */
public final class UndeadInfectionEvents {
    private static final ResourceLocation INFECTION_ID =
            new ResourceLocation("cybernpc", "zombification");
    private static final int INFECTION_TICKS = 20 * 120;
    private static final float INFECTION_CHANCE_PER_HIT = 0.01F;
    private static final int CURE_MIN_TICKS = 20 * 60 * 3;
    private static final int CURE_ADDITIONAL_TICKS = 20 * 60 * 2;

    private UndeadInfectionEvents() {}

    /** CyberNpc is optional; do not load its classes or register its effect. */
    private static MobEffect zombification() {
        return ModList.get().isLoaded("cybernpc")
                ? ForgeRegistries.MOB_EFFECTS.getValue(INFECTION_ID)
                : null;
    }

    @SubscribeEvent
    public static void onZombieDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(event.getSource().getEntity() instanceof Zombie)
                || event.getAmount() <= 0.0F
                || !CharacterManager.isCharacterCreated(player)
                || player.isCreative() || player.isSpectator()
                || RaceManager.getRace(player).orElse(null) == Race.UNDEAD) {
            return;
        }

        MobEffect effect = zombification();
        if (effect == null || player.hasEffect(effect)
                || player.getRandom().nextFloat() >= INFECTION_CHANCE_PER_HIT) {
            return;
        }

        player.addEffect(new MobEffectInstance(
                effect, INFECTION_TICKS, 0, false, true, true));
        player.sendSystemMessage(Component.literal(
                "You contracted Zombification! Find a cure before it takes hold."));
    }

    @SubscribeEvent
    public static void onInfectionExpired(MobEffectEvent.Expired event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !CharacterManager.isCharacterCreated(player)) {
            return;
        }
        MobEffect effect = zombification();
        MobEffectInstance expired = event.getEffectInstance();
        if (effect != null && expired != null && expired.getEffect() == effect) {
            transform(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || !CharacterManager.isCharacterCreated(player)) {
            return;
        }

        // An expiry listener catches the normal case. Check the final tick
        // too, to cover effect tick order in a modded server.
        MobEffect effect = zombification();
        if (effect != null && RaceManager.getRace(player).orElse(null) != Race.UNDEAD) {
            MobEffectInstance infection = player.getEffect(effect);
            if (infection != null && infection.getDuration() <= 1) {
                transform(player);
            }
        }

        int remaining = RaceManager.undeadCureRemaining(player);
        if (remaining <= 0) return;
        if (RaceManager.getRace(player).orElse(null) != Race.UNDEAD) {
            RaceManager.setUndeadCureRemaining(player, 0);
            return;
        }

        if (remaining == 1) {
            finishCure(player);
            return;
        }

        RaceManager.setUndeadCureRemaining(player, remaining - 1);
        if (player.tickCount % 40 == 0) {
            player.serverLevel().sendParticles(
                    ParticleTypes.SMOKE,
                    player.getX(), player.getY() + player.getBbHeight() * 0.55D,
                    player.getZ(), 3, 0.35D, 0.5D, 0.35D, 0.015D);
        }
        if (player.tickCount % 400 == 0) {
            player.displayClientMessage(Component.literal(
                    "Curing Undead: about " + ((remaining + 19) / 20)
                            + " seconds remaining"), true);
        }
    }

    @SubscribeEvent
    public static void onGiveGoldenApple(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer healer)
                || !(event.getTarget() instanceof ServerPlayer target)
                || !healer.getItemInHand(event.getHand()).is(Items.GOLDEN_APPLE)
                || !startTreatment(target)) {
            return;
        }

        if (!healer.getAbilities().instabuild) {
            healer.getItemInHand(event.getHand()).shrink(1);
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    /**
     * Right-clicking yourself is not supported by Minecraft's entity
     * interaction system; an eaten apple works as an equivalent solo cure.
     * The food item is already consumed by vanilla, so never shrink here.
     */
    @SubscribeEvent
    public static void onEatGoldenApple(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof ServerPlayer player
                && event.getItem().is(Items.GOLDEN_APPLE)) {
            startTreatment(player);
        }
    }

    private static boolean startTreatment(ServerPlayer player) {
        if (!CharacterManager.isCharacterCreated(player)
                || !player.hasEffect(MobEffects.WEAKNESS)
                || RaceManager.undeadCureRemaining(player) > 0) {
            return false;
        }

        boolean undead = RaceManager.getRace(player).orElse(null) == Race.UNDEAD;
        MobEffect effect = zombification();
        boolean infected = effect != null && player.hasEffect(effect);
        if (!undead && !infected) return false;

        player.removeEffect(MobEffects.WEAKNESS);

        if (!undead) {
            // Early treatment can clear the disease without a racial change.
            player.removeEffect(effect);
            player.sendSystemMessage(Component.literal(
                    "The Zombification infection has been cured."));
            player.serverLevel().sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    player.getX(), player.getY() + 1.0D, player.getZ(),
                    18, 0.5D, 0.6D, 0.5D, 0.02D);
            player.serverLevel().playSound(
                    null, player.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED,
                    SoundSource.PLAYERS, 0.8F, 1.0F);
            return true;
        }

        if (infected) player.removeEffect(effect);
        int duration = CURE_MIN_TICKS
                + player.getRandom().nextInt(CURE_ADDITIONAL_TICKS + 1);
        RaceManager.setUndeadCureRemaining(player, duration);
        player.sendSystemMessage(Component.literal(
                "The golden apple is working! Your Undead curse will fade "
                + "in about " + ((duration + 19) / 20) + " seconds."));
        player.serverLevel().playSound(
                null, player.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CURE,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    private static void transform(ServerPlayer player) {
        if (RaceManager.getRace(player).orElse(null) == Race.UNDEAD
                || RaceManager.getBaseRace(player).isEmpty()) return;

        RaceManager.setAcquiredUndead(player, true);
        player.sendSystemMessage(Component.literal(
                "Zombification has taken hold. You are now "
                + RaceManager.getEffectiveDisplayName(player)
                + ". Weakness and a golden apple can restore you."));
        player.serverLevel().sendParticles(
                ParticleTypes.SOUL, player.getX(), player.getY() + 1.0D,
                player.getZ(), 24, 0.5D, 0.8D, 0.5D, 0.02D);
        player.serverLevel().playSound(
                null, player.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED,
                SoundSource.PLAYERS, 0.85F, 0.7F);
    }

    private static void finishCure(ServerPlayer player) {
        // Pre-overhaul characters who chose Undead directly have no
        // original ancestry on record. Fall back to Human for those saves.
        if (RaceManager.isAcquiredUndead(player)) {
            RaceManager.setAcquiredUndead(player, false);
        } else if (RaceManager.getBaseRace(player).orElse(null) == Race.UNDEAD) {
            RaceManager.setRace(player, Race.HUMAN);
            CharacterSyncService.broadcast(player);
        } else {
            RaceManager.setUndeadCureRemaining(player, 0);
            return;
        }

        player.sendSystemMessage(Component.literal(
                "You are cured! Your race is now "
                        + RaceManager.getEffectiveDisplayName(player) + "."));
        player.serverLevel().sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                player.getX(), player.getY() + 1.0D, player.getZ(),
                35, 0.6D, 0.9D, 0.6D, 0.03D);
        player.serverLevel().playSound(
                null, player.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED,
                SoundSource.PLAYERS, 1.0F, 1.05F);
    }
}
