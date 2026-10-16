package com.cyberspectraa.cyberraces.ability;

import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Free V-key racial actions, server-authoritative and independently cooled down. */
public final class NewRaceAbilities {
    private static final Map<UUID, Long> READY_AT = new HashMap<>();
    private NewRaceAbilities() {}

    public static void activate(ServerPlayer player) {
        Race race=RaceManager.getRace(player).orElse(null);
        if(race!=Race.ARACHNID && race!=Race.UNDEAD)return;
        long now=player.serverLevel().getGameTime();
        long ready=READY_AT.getOrDefault(player.getUUID(),0L);
        if(now<ready){
            player.displayClientMessage(Component.literal("Racial ability: "+((ready-now+19)/20)+"s cooldown")
                    .withStyle(ChatFormatting.GRAY),true);
            return;
        }
        if (race == Race.ARACHNID) {
            // A physical flying cobweb, not an invisible AOE debuff.
            net.minecraft.world.entity.projectile.Snowball web =
                    new net.minecraft.world.entity.projectile.Snowball(player.level(), player);
            web.setItem(new net.minecraft.world.item.ItemStack(
                    net.minecraft.world.item.Items.COBWEB));
            web.getPersistentData().putBoolean("CyberRacesWebShot", true);
            web.shootFromRotation(player, player.getXRot(), player.getYRot(),
                    0.0F, 1.65F, 0.2F);
            player.level().addFreshEntity(web);
            player.serverLevel().playSound(null, player.blockPosition(),
                    SoundEvents.SPIDER_AMBIENT, SoundSource.PLAYERS, 0.75F, 1.1F);
            READY_AT.put(player.getUUID(), now + 240L);
        } else {
            // Undead use a defensive, life-force ability rather than a normal potion.
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE, 110, 0));
            player.heal(3F);
            player.serverLevel().sendParticles(ParticleTypes.SOUL,
                    player.getX(),player.getY()+1,player.getZ(),24,0.6,0.7,0.6,0.025);
            player.serverLevel().playSound(null,player.blockPosition(),SoundEvents.SOUL_ESCAPE,SoundSource.PLAYERS,0.8F,0.75F);
            READY_AT.put(player.getUUID(),now+400L);
        }
    }

    public static void cleanup(ServerPlayer player) {
        if(player!=null)READY_AT.remove(player.getUUID());
    }
}
