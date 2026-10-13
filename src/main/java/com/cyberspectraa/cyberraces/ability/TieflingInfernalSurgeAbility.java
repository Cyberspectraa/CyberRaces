package com.cyberspectraa.cyberraces.ability;

import com.cyberspectraa.cyberraces.character.CharacterManager;
import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * V: Infernal Surge, an eight-second offensive melee window.
 * No reliance on Iron's Spells, shared potion buffs, or mana.
 */
public final class TieflingInfernalSurgeAbility {
    public static final int ACTIVE_TICKS = 8 * 20;
    public static final int COOLDOWN_TICKS = 32 * 20;
    private static final String READY = "TieflingSurgeReadyTick";
    private static final String ACTIVE = "TieflingSurgeActiveUntil";

    private TieflingInfernalSurgeAbility() {}

    public static void tryActivate(ServerPlayer player) {
        long now = player.level().getGameTime();
        CompoundTag root = player.getPersistentData()
                .getCompound(CharacterManager.ROOT_KEY);
        long ready = root.getLong(READY);
        if (ready > now) {
            player.displayClientMessage(Component.literal(
                    "Infernal Surge: " + ((ready - now + 19L) / 20L)
                            + "s cooldown"), true);
            return;
        }

        root.putLong(READY, now + COOLDOWN_TICKS);
        root.putLong(ACTIVE, now + ACTIVE_TICKS);
        player.getPersistentData().put(CharacterManager.ROOT_KEY, root);
        player.displayClientMessage(Component.literal(
                "Infernal Surge: stronger burning melee attacks (8s)"), true);
        if (player.level() instanceof ServerLevel level) {
            level.playSound(null, player.blockPosition(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8F, 0.75F);
            level.sendParticles(ParticleTypes.FLAME,
                    player.getX(), player.getY() + player.getBbHeight() * 0.5D,
                    player.getZ(), 18, 0.4D, 0.6D, 0.4D, 0.045D);
        }
    }

    @SubscribeEvent
    public static void onMeleeHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)
                || event.getSource().getDirectEntity() != player
                || event.getEntity() == player
                || event.getAmount() <= 0.0F
                || RaceManager.getRace(player).orElse(null) != Race.TIEFLING) {
            return;
        }

        CompoundTag root = player.getPersistentData()
                .getCompound(CharacterManager.ROOT_KEY);
        if (root.getLong(ACTIVE) <= player.level().getGameTime()) return;

        event.setAmount(event.getAmount() * 1.25F);
        if (!event.getEntity().fireImmune())
            event.getEntity().setSecondsOnFire(3);
    }
}
