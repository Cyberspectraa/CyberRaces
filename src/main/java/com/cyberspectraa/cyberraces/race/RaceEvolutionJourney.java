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
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

/** Persistent racial affinities; Level 30 alone cannot cause evolution. */
public final class RaceEvolutionJourney {
    public static final String KEY = "EvolutionJourney";
    public static final int TOTAL_REQUIRED = 80;
    public static final int DOMINANT_REQUIRED = 28;
    private static final int BATTLE=0, MYSTIC=1, WANDERER=2, GUARDIAN=3;
    private static final int WILDS=0, WATER=1, NETHER=2, STORM=3, SKY=4, DEPTHS=5, NIGHT=6,
            COLD=7, FOREST=8, END=9, DAY=10;
    private static final String[] CLIMATES={"Wilderness","Water","Nether","Storms","Sky",
            "Underground","Night","Cold","Forest","End","Daylight"};
    private static final int ENVIRONMENT_COUNT=CLIMATES.length;
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
        // Migrate earlier seven-environment histories without erasing data.
        return Arrays.copyOf(a, ENVIRONMENT_COUNT);
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
    /** Samples exploration without awarding progress for AFK/standing still. */
    public static void tick(ServerPlayer p) {
        if (p==null || RaceManager.getRace(p).isEmpty()
                || RaceManager.getEvolution(p).isPresent())return;
        CompoundTag tag=data(p);
        BlockPos pos=p.blockPosition();
        int environment=environment(p);
        long cell=ChunkPos.asLong(pos.getX()>>4,pos.getZ()>>4)
                ^ (((long)p.level().dimension().location().hashCode())<<32);
        long[] seen=tag.getLongArray("SeenChunks");
        boolean visited=false;
        for(long x:seen)if(x==cell){visited=true;break;}
        boolean changed=false;
        if(!visited) {
            long[] next=seen.length<256?Arrays.copyOf(seen,seen.length+1)
                    :Arrays.copyOfRange(seen,1,seen.length);
            next[next.length-1]=cell;
            tag.putLongArray("SeenChunks",next);
            int[] a=scores(tag);
            a[WANDERER]=Math.min(300,a[WANDERER]+3);
            tag.putIntArray("Affinity",a);
            changed=true;
        }

        // Earn biome/context familiarity while actively travelling, even if
        // a player explores inside previously visited chunks. One contextual
        // sample per 30 seconds; no gain from standing AFK.
        long now=p.serverLevel().getGameTime();
        double dx=p.getX()-tag.getDouble("LastEnvX");
        double dz=p.getZ()-tag.getDouble("LastEnvZ");
        String dimension=p.level().dimension().location().toString();
        boolean dimensionChanged=!dimension.equals(tag.getString("LastEnvDimension"));
        if((!tag.contains("LastEnvTime") || now-tag.getLong("LastEnvTime")>=600L)
                && (dimensionChanged || dx*dx+dz*dz>=144D)) {
            int[] c=contexts(tag);
            c[environment]=Math.min(120,c[environment]+(visited?2:3));
            tag.putIntArray("Context",c);
            tag.putLong("LastEnvTime",now);
            tag.putDouble("LastEnvX",p.getX());
            tag.putDouble("LastEnvZ",p.getZ());
            tag.putString("LastEnvDimension",dimension);
            changed=true;
        }

        if(changed)save(p,tag);
        if(ready(p) && !tag.getBoolean("EvolutionReadyNotified")) {
            tag.putBoolean("EvolutionReadyNotified",true);
            save(p,tag);
            p.sendSystemMessage(Component.literal(
                    "Your racial essence is ready to awaken. Rest in a bed to glimpse your evolution.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    private static int environment(ServerPlayer p) {
        Level level=p.level();
        BlockPos pos=p.blockPosition();
        if(level.dimension()==Level.NETHER)return NETHER;
        if(level.dimension()==Level.END)return END;
        if(p.isInWaterOrBubble())return WATER;
        if(level.isThundering() && level.canSeeSky(pos))return STORM;
        Holder<Biome> biome=level.getBiome(pos);
        if(biome.value().getBaseTemperature()<0.2F)return COLD;
        if(biome.unwrapKey().map(key -> key.location().getPath().contains("forest")
                || key.location().getPath().contains("grove")
                || key.location().getPath().contains("jungle")).orElse(false))return FOREST;
        if(p.getY()>135 && level.canSeeSky(pos))return SKY;
        if(p.getY()<35)return DEPTHS;
        if(level.isNight() && level.canSeeSky(pos))return NIGHT;
        if(level.isDay() && level.canSeeSky(pos))return DAY;
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
        int[] c=contexts(data(p));
        int total=Arrays.stream(a).sum();
        int maxContext=0;
        for(int i=1;i<c.length;i++)if(c[i]>c[maxContext])maxContext=i;
        RaceEvolution proposed=recommendation(p);
        return Component.literal("Racial growth: Lv "
                +ProgressionManager.getLevel(p)+"/"+RaceEvolution.REQUIRED_LEVEL
                +" | Activity "+total+"/"+TOTAL_REQUIRED
                +" | Battle "+a[0]+", Mystic "+a[1]+", Explore "+a[2]+", Guardian "+a[3]
                +" | Favoured environment: "+CLIMATES[maxContext]+" ("+c[maxContext]+")"
                +(proposed==null?" | Not ready yet":" | Sleep for an evolution offer: "+proposed.displayName()))
                .withStyle(ChatFormatting.AQUA);
    }

    /** Affinities are selected deliberately; no branch is a dead enum case. */
    private static int alignment(RaceEvolution e) {
        return switch(e) {
            case PARAGON, WARBORN, HOBGOBLIN, ASHBORN, EMBERBLOOD,
                 THORNFAE, LYNXKIN, LIONKIN, WOLFKIN, RAPTOR,
                 MAGMA_SLIME, ACID_SLIME, VENOMFANG, HUNTSMAN, DREAD_KNIGHT,
                 EMBERFORGED, TEMPEST_NYMPH -> BATTLE;
            case AWAKENED, HIGH_ELF, RUNEBORN, SPIRITBLOOD, GLIMMERFAE,
                 ANTHOUSA, SPIRIT_FOX, SERAPHIC, FALLEN, STORMBLOOD,
                 CRYSTAL_SLIME, VOID_SLIME, WEBWEAVER, LICHBOUND, PLAGUEBORN,
                 STARSEEKER, WHISPERFOOT, EMBERFOX -> MYSTIC;
            case WAYFARER, WOOD_ELF, MOON_ELF, DARK_ELF, LIGHTFOOT, STORMCHASER,
                 GLOOMRUNNER, SCROUNGER, UMBRAL, RIFTWALKER, SKYBORN,
                 SYLPH, NAIAD, PANTHERKIN, HOUNDKIN, SWIFTFOX, TRICKSTER,
                 NIGHTWING, STORMWING, FROST_SLIME, CAVE_STALKER, WRAITH,
                 CLOUDLEAPER, FROSTHOUND, TIDEWING -> WANDERER;
            default -> GUARDIAN;
        };
    }

    /** Environmental identity breaks playstyle ties; overlaps are intentional
     * only when affinities differ. All paths have a reachable combination. */
    private static int climate(RaceEvolution e) {
        return switch(e) {
            case ASHBORN, EMBERBLOOD, MAGMA_SLIME, EMBERFORGED,
                 RIFTWALKER, EMBERFOX, PLAGUEBORN -> NETHER;
            case STORMBLOOD, STORMWING, GLIMMERFAE, STORMCHASER,
                 TEMPEST_NYMPH -> STORM;
            case SKYBORN, SYLPH, RAPTOR, LIGHTFOOT, STARSEEKER,
                 CLOUDLEAPER, MOUNTAINBORN -> SKY;
            case FROSTBLOOD, FROST_SLIME, FROSTHOUND -> COLD;
            case NAIAD, DEWDROP, TIDEWING -> WATER;
            case DEEPFORGED, RUNEBORN, ELDER_DWARF, OREAD, METAL_SLIME,
                 CAVE_STALKER, CAVEWRIGHT, EARTHSCALE, DREAD_KNIGHT -> DEPTHS;
            case MOON_ELF, DARK_ELF, UMBRAL, NIGHTWING, PANTHERKIN,
                 FALLEN, VOID_SLIME, WRAITH, LICHBOUND,
                 GLOOMRUNNER, WHISPERFOOT, VENOMFANG -> NIGHT;
            case WOOD_ELF, THORNFAE, DRYAD, ANTHOUSA, TRICKSTER, HOUNDKIN,
                 WOLFKIN, LIONKIN, SCROUNGER, WEBWEAVER,
                 BROODMOTHER, FORTUNE_TOUCHED -> FOREST;
            case DAWNBRINGER -> DAY;
            default -> WILDS;
        };
    }

    /**
     * Return a context-aware, non-binding recommendation. This is never
     * applied automatically. The player may decline and keep gathering new
     * affinities before sleeping again.
     */
    public static RaceEvolution recommendation(ServerPlayer p) {
        if (!ready(p)) return null;
        Race race = RaceManager.getRace(p).orElse(null);
        if (race == null) return null;
        int[] activity = scores(data(p));
        int[] contexts = contexts(data(p));
        RaceEvolution candidate = null;
        int highestScore = Integer.MIN_VALUE;
        for (RaceEvolution option : RaceEvolution.choicesFor(race)) {
            int habitat = climate(option);
            int bonus = habitat == WILDS
                    ? Math.min(contexts[WILDS], 12) * 2
                    : Math.min(contexts[habitat], 20) * 4;
            int score = activity[alignment(option)] * 5 + bonus;
            if (score > highestScore) {
                highestScore = score;
                candidate = option;
            }
        }
        return candidate;
    }
}
