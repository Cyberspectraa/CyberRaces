package com.cyberspectraa.cyberraces.ability;

import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceEvolution;
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
        if(race!=Race.SLIME && race!=Race.ARACHNID && race!=Race.UNDEAD)return;
        long now=player.serverLevel().getGameTime();
        long ready=READY_AT.getOrDefault(player.getUUID(),0L);
        if(now<ready){
            player.displayClientMessage(Component.literal("Racial ability: "+((ready-now+19)/20)+"s cooldown")
                    .withStyle(ChatFormatting.GRAY),true);
            return;
        }
        RaceEvolution path=RaceManager.getEvolution(player).orElse(null);
        if(race==Race.SLIME) {
            Vec3 direction=player.getLookAngle().multiply(0.9D,0D,0.9D);
            // Elastic jump: safe on the ground, without forced hovering.
            if(!player.onGround() && !player.isInWaterOrBubble()) {
                player.displayClientMessage(Component.literal("Land first to use Elastic Leap."),true);
                return;
            }
            player.setDeltaMovement(player.getDeltaMovement().add(direction.x,
                    path==RaceEvolution.METAL_SLIME?0.62D:0.84D,direction.z));
            player.hurtMarked=true;
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,75,0,false,true));
            if(path==RaceEvolution.MAGMA_SLIME)player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,100,0));
            if(path==RaceEvolution.CRYSTAL_SLIME || path==RaceEvolution.ROYAL_SLIME)
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,110,0));
            player.serverLevel().sendParticles(path==RaceEvolution.MAGMA_SLIME?ParticleTypes.FLAME:ParticleTypes.ITEM_SLIME,
                    player.getX(),player.getY()+0.35,player.getZ(),22,0.4,0.3,0.4,0.08);
            player.serverLevel().playSound(null,player.blockPosition(),SoundEvents.SLIME_SQUISH,SoundSource.PLAYERS,0.9F,1.0F);
            READY_AT.put(player.getUUID(),now+240L);
        } else if(race==Race.ARACHNID) {
            int hit=0;
            double range=path==RaceEvolution.WEBWEAVER?11D:8D;
            for(LivingEntity target:player.serverLevel().getEntitiesOfClass(LivingEntity.class,
                    new AABB(player.blockPosition()).inflate(range),
                    e->e!=player && e.isAlive() && !(e instanceof ServerPlayer))) {
                Vec3 towards=target.position().subtract(player.position()).normalize();
                if(player.getLookAngle().dot(towards)<0.35D || !player.hasLineOfSight(target))continue;
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                        path==RaceEvolution.WEBWEAVER?125:85,2));
                if(path==RaceEvolution.VENOMFANG)target.addEffect(new MobEffectInstance(MobEffects.POISON,80,0));
                ++hit;
                if(hit>=5)break;
            }
            player.serverLevel().sendParticles(ParticleTypes.CLOUD,player.getX(),player.getEyeY(),player.getZ(),
                    30,0.7,0.45,0.7,0.04);
            player.serverLevel().playSound(null,player.blockPosition(),SoundEvents.SPIDER_AMBIENT,SoundSource.PLAYERS,0.8F,0.7F);
            READY_AT.put(player.getUUID(),now+320L);
            player.displayClientMessage(Component.literal("Web Snare: "+hit+" targets tangled."),true);
        } else {
            // Undead use a defensive, life-force ability rather than a normal potion.
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,
                    path==RaceEvolution.REVENANT || path==RaceEvolution.DREAD_KNIGHT?160:110,0));
            if(path==RaceEvolution.LICHBOUND)player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,100,0));
            else player.heal(path==RaceEvolution.REVENANT?6F:3F);
            if(path==RaceEvolution.WRAITH)player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,120,0));
            if(path==RaceEvolution.PLAGUEBORN)player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,100,0));
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
