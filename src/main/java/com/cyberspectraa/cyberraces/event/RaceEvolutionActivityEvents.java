package com.cyberspectraa.cyberraces.event;

import com.cyberspectraa.cyberraces.character.CharacterManager;
import com.cyberspectraa.cyberraces.race.RaceEvolutionJourney;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class RaceEvolutionActivityEvents {
    private RaceEvolutionActivityEvents() {}

    private static boolean readyPlayer(ServerPlayer p) {
        return p != null && CharacterManager.isCharacterCreated(p);
    }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer p)
                || !readyPlayer(p) || !(event.getEntity() instanceof Enemy
                    || event.getEntity() instanceof EnderDragon)) return;
        boolean boss = event.getEntity() instanceof WitherBoss
                || event.getEntity() instanceof EnderDragon;
        ResourceLocation weapon = ForgeRegistries.ITEMS.getKey(p.getMainHandItem().getItem());
        String id = weapon == null ? "" : weapon.getPath();
        boolean magic = id.contains("staff") || id.contains("wand")
                || id.contains("spellbook") || id.contains("spell_book")
                || id.contains("scroll") || id.contains("grimoire");
        RaceEvolutionJourney.battle(p,magic,boss);
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && readyPlayer(p)
                && event.getAmount() > 0) RaceEvolutionJourney.hurt(p);
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && readyPlayer(p)
                && event.getAmount() > 0) RaceEvolutionJourney.heal(p);
    }

    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || !readyPlayer(p)) return;
        ResourceLocation item = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
        String id = item == null ? "" : item.getPath();
        if (id.contains("staff") || id.contains("wand") || id.contains("scroll")
                || id.contains("spellbook") || id.contains("spell_book")
                || id.contains("potion") || id.contains("grimoire")) {
            RaceEvolutionJourney.magic(p);
        }
    }
}
