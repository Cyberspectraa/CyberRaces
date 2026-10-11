package com.cyberspectraa.cyberraces.race;

import com.cyberspectraa.cyberraces.character.CharacterSyncService;
import com.cyberspectraa.cyberraces.progression.ProgressionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import java.util.Arrays;

/** Persistent racial affinities; Level 30 alone cannot cause evolution. */
public final class RaceEvolutionJourney {
    public static final String KEY = "EvolutionJourney";
    public static final int TOTAL_REQUIRED = 80;
    public static final int DOMINANT_REQUIRED = 28;
    private static final int BATTLE=0, MYSTIC=1, WANDERER=2, GUARDIAN=3;
    private static final int WILDS=0, WATER=1, NETHER=2, STORM=3, SKY=4, DEPTHS=5, NIGHT=6;
    private RaceEvolutionJourney() {}

    private static CompoundTag data(ServerPlayer p) {
        return p.getPersistentData().getCompound("CyberRaces").getCompound(KEY);
    }
    private static void save(ServerPlayer p, CompoundTag tag) {
        CompoundTag root=p.getPersistentData().getCompound("CyberRaces");
        root.put(KEY,tag);
        p.getPersistentData().put("CyberRaces",root);
    }
    public static void reset(ServerPlayer p) {
        CompoundTag root=p.getPersistentData().getCompound("CyberRaces");
        root.remove(KEY);
        p.getPersistentData().put("CyberRaces",root);
    }
    private static int[] scores(CompoundTag tag) {
        int[] a=tag.getIntArray("Affinity");
        return a.length==4?a:new int[4];
    }
    private static int[] contexts(CompoundTag tag) {
        int[] a=tag.getIntArray("Context");
        return a.length==7?a:new int[7];
    }
    private static void add(ServerPlayer p,int affinity,int amount,String key,int cooldown) {
        if(p==null || RaceManager.getRace(p).isEmpty()
                || RaceManager.getEvolution(p).isPresent())return;
        CompoundTag tag=data(p);
        long now=p.serverLevel().getGameTime();
        if(tag.contains(key) && now-tag.getLong(key)<cooldown)return;
        tag.putLong(key,now);
        int[] a=scores(tag);
        a[affinity]=Math.min(300,a[affinity]+amount);
        tag.putIntArray("Affinity",a);
        save(p,tag);
    }
    public static void battle(ServerPlayer p,boolean magic,boolean boss) {
        add(p,magic?MYSTIC:BATTLE,boss?14:4,
                magic?"MagicKill":"BattleKill",12);
        if(boss)add(p,GUARDIAN,3,"BossKill",20);
    }
    public static void hurt(ServerPlayer p) {
        add(p,GUARDIAN,2,"Hurt",55);
    }
    public static void heal(ServerPlayer p) {
        add(p,MYSTIC,2,"Heal",75);
        add(p,GUARDIAN,1,"Recovery",75);
    }
    public static void magic(ServerPlayer p) {
        add(p,MYSTIC,3,"Magic",90);
    }
    /** One sample per five seconds; only new chunks count. */
    public static void tick(ServerPlayer p) {
        if(p==null || RaceManager.getRace(p).isEmpty()
                || RaceManager.getEvolution(p).isPresent())return;
        CompoundTag tag=data(p);
        BlockPos pos=p.blockPosition();
        long cell=ChunkPos.asLong(pos.getX()>>4,pos.getZ()>>4)
                ^ (((long)p.level().dimension().location().hashCode())<<32);
        long[] seen=tag.getLongArray("SeenChunks");
        boolean known=false;
        for(long x:seen)if(x==cell){known=true;break;}
        if(!known) {
            long[] newSeen=seen.length<256?Arrays.copyOf(seen,seen.length+1)
                    :Arrays.copyOfRange(seen,1,seen.length);
            newSeen[newSeen.length-1]=cell;
            tag.putLongArray("SeenChunks",newSeen);
            int[] a=scores(tag);
            a[WANDERER]=Math.min(300,a[WANDERER]+3);
            tag.putIntArray("Affinity",a);
            int[] c=contexts(tag);
            int env=environment(p);
            c[env]=Math.min(300,c[env]+1);
            tag.putIntArray("Context",c);
            save(p,tag);
        }
        tryEvolve(p);
    }
    private static int environment(ServerPlayer p) {
        if(p.level().dimension()==Level.NETHER)return NETHER;
        if(p.isInWaterOrBubble())return WATER;
        if(p.level().isThundering() && p.level().canSeeSky(p.blockPosition()))return STORM;
        if(p.getY()>135 && p.level().canSeeSky(p.blockPosition()))return SKY;
        if(p.getY()<35)return DEPTHS;
        if(p.level().isNight() && p.level().canSeeSky(p.blockPosition()))return NIGHT;
        return WILDS;
    }
    public static boolean ready(ServerPlayer p) {
        if(p==null || RaceManager.getRace(p).isEmpty()
                || RaceManager.getEvolution(p).isPresent()
                || ProgressionManager.getLevel(p)<RaceEvolution.REQUIRED_LEVEL)return false;
        int[] a=scores(data(p));
        return Arrays.stream(a).sum()>=TOTAL_REQUIRED
                && Arrays.stream(a).max().orElse(0)>=DOMINANT_REQUIRED;
    }
    public static Component status(ServerPlayer p) {
        int[] a=scores(data(p));
        return Component.literal("Racial growth: Level "
                +ProgressionManager.getLevel(p)+"/"+RaceEvolution.REQUIRED_LEVEL
                +" | Activity "+Arrays.stream(a).sum()+"/"+TOTAL_REQUIRED
                +" | Battle "+a[0]+" Mystic "+a[1]+" Explore "+a[2]+" Guardian "+a[3]
                +" (dominant "+DOMINANT_REQUIRED+" required)")
                .withStyle(ChatFormatting.AQUA);
    }
    private static int alignment(RaceEvolution e) {
        return switch(e) {
            case PARAGON,WARBORN,HOBGOBLIN,EMBERBLOOD,THORNFAE,
                 LYNXKIN,LIONKIN,WOLFKIN,RAPTOR,ASHBORN -> BATTLE;
            case AWAKENED,HIGH_ELF,RUNEBORN,SPIRITBLOOD,GLIMMERFAE,
                 ANTHOUSA,SPIRIT_FOX,SERAPHIC,FALLEN,STORMBLOOD -> MYSTIC;
            case WOOD_ELF,MOON_ELF,LIGHTFOOT,GLOOMRUNNER,SKYBORN,
                 SYLPH,NAIAD,HOUNDKIN,SWIFTFOX,TRICKSTER,
                 NIGHTWING,STORMWING,SCROUNGER,FORTUNE_TOUCHED -> WANDERER;
            default -> GUARDIAN;
        };
    }
    private static int climate(RaceEvolution e) {
        return switch(e) {
            case ASHBORN,EMBERBLOOD -> NETHER;
            case STORMBLOOD,STORMWING,GLIMMERFAE -> STORM;
            case SKYBORN,SYLPH,RAPTOR -> SKY;
            case FROSTBLOOD,NAIAD -> WATER;
            case DEEPFORGED,MOUNTAINBORN,OREAD,RUNEBORN -> DEPTHS;
            case MOON_ELF,UMBRAL,NIGHTWING,PANTHERKIN,FALLEN -> NIGHT;
            default -> WILDS;
        };
    }
    public static void tryEvolve(ServerPlayer p) {
        if(!ready(p))return;
        Race race=RaceManager.getRace(p).orElse(null);
        if(race==null)return;
        int[] a=scores(data(p)),c=contexts(data(p));
        RaceEvolution chosen=null;
        int best=Integer.MIN_VALUE;
        for(RaceEvolution e:RaceEvolution.choicesFor(race)){
            int habitat=climate(e);
            // Playstyle dominates; ordinary surface travel is only a weak
            // tie-breaker. Rare habitats have a capped situational bonus.
            int contextBonus=habitat==WILDS
                    ? Math.min(c[WILDS],12)*2
                    : Math.min(c[habitat],20)*4;
            int value=a[alignment(e)]*5+contextBonus;
            if(value>best){best=value;chosen=e;}
        }
        if(chosen==null || !RaceManager.setEvolution(p,chosen))return;
        CharacterSyncService.broadcast(p);
        p.serverLevel().sendParticles(ParticleTypes.END_ROD,p.getX(),p.getY()+1,p.getZ(),
                70,0.7,0.9,0.7,0.045);
        p.serverLevel().playSound(null,p.blockPosition(),SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS,0.85F,1.15F);
        p.sendSystemMessage(Component.literal("Your experiences awaken a new lineage: ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(chosen.displayName()).withStyle(ChatFormatting.YELLOW)));
    }
}
