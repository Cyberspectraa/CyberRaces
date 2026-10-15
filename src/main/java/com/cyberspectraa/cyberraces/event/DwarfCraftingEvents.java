package com.cyberspectraa.cyberraces.event;

import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Dwarven craftsmanship occasionally produces magical equipment.
 *
 * Forge fires ItemCraftedEvent when the output is taken from a crafting
 * result slot, not just when ingredients appear in the grid. The crafted
 * ItemStack itself is enchanted so no duplicate item can be introduced.
 */
public final class DwarfCraftingEvents {
    private static final float MASTERWORK_CHANCE = 0.15F;

    private DwarfCraftingEvents() {}

    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || RaceManager.getRace(player).orElse(null) != Race.DWARF) {
            return;
        }

        ItemStack result = event.getCrafting();
        // Only single-piece, unenchanted equipment with normal Minecraft
        // enchantability. This also allows supported modded armor/tools.
        if (result.isEmpty() || result.getCount() != 1
                || !result.isEnchantable() || result.isEnchanted()
                || result.getItem().getEnchantmentValue() <= 0) {
            return;
        }

        if (player.getRandom().nextFloat() >= MASTERWORK_CHANCE) return;

        int level = 7 + player.getRandom().nextInt(8);
        EnchantmentHelper.enchantItem(player.getRandom(), result, level, false);
        if (!result.isEnchanted()) return;

        player.displayClientMessage(Component.literal(
                "Dwarven craftsmanship! Your item gained a random enchantment."), true);
        player.serverLevel().playSound(null, player.blockPosition(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS,
                0.55F, 1.3F);
        player.serverLevel().sendParticles(ParticleTypes.ENCHANT,
                player.getX(), player.getY() + 1.0D, player.getZ(),
                14, 0.5D, 0.45D, 0.5D, 0.06D);
    }
}
