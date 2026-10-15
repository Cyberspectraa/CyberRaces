package com.cyberspectraa.cyberraces.loot;

import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * A Dwarf naturally mines resources as though they had Fortune II.
 * This is a block-ONLY loot modifier: no crafting, mob drops, loot
 * containers or decorative block duplication.
 */
public final class DwarfFortuneModifier extends LootModifier {
    public static final Codec<DwarfFortuneModifier> CODEC =
            RecordCodecBuilder.create(instance ->
                    codecStart(instance).apply(instance, DwarfFortuneModifier::new));

    public DwarfFortuneModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(
            ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        Entity source = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (!(source instanceof ServerPlayer player)
                || RaceManager.getRace(player).orElse(null) != Race.DWARF) {
            return generatedLoot;
        }
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (state == null || tool == null || generatedLoot.isEmpty()
                || !(state.is(Tags.Blocks.ORES)
                    || state.is(Blocks.GLOWSTONE)
                    || state.is(Blocks.GRAVEL)
                    || state.is(Blocks.NETHER_WART))) {
            return generatedLoot;
        }

        // Don't bypass Silk Touch or amplify an existing Fortune II+.
        if (EnchantmentHelper.getItemEnchantmentLevel(
                    Enchantments.SILK_TOUCH, tool) > 0) {
            return generatedLoot;
        }
        int actualFortune = EnchantmentHelper.getItemEnchantmentLevel(
                Enchantments.BLOCK_FORTUNE, tool);
        if (actualFortune >= 2) return generatedLoot;

        // A simple Fortune-II style bonus roll, capped by the already
        // present tool's Fortune level. It never manufactures the ore
        // block itself (e.g. from a modded silk-touch equivalent).
        int extraFortune = 2 - actualFortune;
        for (ItemStack drop : new ArrayList<>(generatedLoot)) {
            if (drop.isEmpty() || drop.getItem() == state.getBlock().asItem())
                continue;
            int extraMultiplier = Math.max(
                    0, context.getRandom().nextInt(extraFortune + 1) - 1);
            int extraCount = drop.getCount() * extraMultiplier;
            while (extraCount > 0) {
                int amount = Math.min(extraCount, drop.getMaxStackSize());
                ItemStack bonus = drop.copy();
                bonus.setCount(amount);
                generatedLoot.add(bonus);
                extraCount -= amount;
            }
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
