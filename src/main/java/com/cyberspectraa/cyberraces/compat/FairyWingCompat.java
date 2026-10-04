package com.cyberspectraa.cyberraces.compat;

import com.cyberspectraa.cyberraces.CyberRaces;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;
import java.util.Optional;

public final class FairyWingCompat {
    public static final String BACK_SLOT = "back";
    public static final ResourceLocation ZANZAS_WINGS_ID = new ResourceLocation("icarus", "zanzas_wings");

    private static final String RACIAL_WING_TAG = "CyberRacesFairyWing";

    private FairyWingCompat() {
    }

    public static ItemStack createRacialWings() {
        Item wingItem = ForgeRegistries.ITEMS.getValue(ZANZAS_WINGS_ID);
        if (wingItem == null || wingItem == Items.AIR) {
            CyberRaces.LOGGER.error("Could not find Icarus item {}", ZANZAS_WINGS_ID);
            return ItemStack.EMPTY;
        }

        ItemStack wings = new ItemStack(wingItem);
        wings.enchant(Enchantments.BINDING_CURSE, 1);
        wings.getOrCreateTag().putBoolean(RACIAL_WING_TAG, true);
        wings.getOrCreateTag().putBoolean("Unbreakable", true);
        wings.setDamageValue(0);
        return wings;
    }

    public static boolean isRacialFairyWing(ItemStack stack) {
        return !stack.isEmpty()
            && stack.hasTag()
            && stack.getTag() != null
            && stack.getTag().getBoolean(RACIAL_WING_TAG);
    }

    public static ItemStack getEquippedRacialWings(LivingEntity entity) {
        Optional<top.theillusivec4.curios.api.type.capability.ICuriosItemHandler> optional =
            CuriosApi.getCuriosInventory(entity).resolve();

        if (optional.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (Map.Entry<String, ICurioStacksHandler> entry : optional.get().getCurios().entrySet()) {
            IDynamicStackHandler stacks = entry.getValue().getStacks();
            for (int index = 0; index < stacks.getSlots(); index++) {
                ItemStack stack = stacks.getStackInSlot(index);
                if (isRacialFairyWing(stack)) {
                    return stack;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    public static boolean hasEquippedRacialWings(LivingEntity entity) {
        return !getEquippedRacialWings(entity).isEmpty();
    }

    public static void ensureEquipped(ServerPlayer player) {
        ItemStack existingRacialWings = getEquippedRacialWings(player);
        if (!existingRacialWings.isEmpty()) {
            repairAndMark(existingRacialWings);
            return;
        }

        ItemStack racialWings = createRacialWings();
        if (racialWings.isEmpty()) {
            return;
        }

        CuriosApi.getCuriosInventory(player).resolve().ifPresent(handler ->
            handler.getStacksHandler(BACK_SLOT).ifPresentOrElse(
                backHandler -> equipIntoBackSlot(player, backHandler, racialWings),
                () -> CyberRaces.LOGGER.error("Fairy {} has no Curios '{}' slot", player.getGameProfile().getName(), BACK_SLOT)
            )
        );
    }

    public static void removeRacialWings(ServerPlayer player) {
        CuriosApi.getCuriosInventory(player).resolve().ifPresent(handler -> {
            for (ICurioStacksHandler curioHandler : handler.getCurios().values()) {
                IDynamicStackHandler stacks = curioHandler.getStacks();
                for (int index = 0; index < stacks.getSlots(); index++) {
                    if (isRacialFairyWing(stacks.getStackInSlot(index))) {
                        stacks.setStackInSlot(index, ItemStack.EMPTY);
                    }
                }
            }
        });
    }

    private static void equipIntoBackSlot(ServerPlayer player, ICurioStacksHandler backHandler, ItemStack racialWings) {
        IDynamicStackHandler stacks = backHandler.getStacks();

        if (stacks.getSlots() <= 0) {
            CyberRaces.LOGGER.error("Fairy {} has a Curios back handler with no slots", player.getGameProfile().getName());
            return;
        }

        int target = -1;
        for (int index = 0; index < stacks.getSlots(); index++) {
            if (stacks.getStackInSlot(index).isEmpty()) {
                target = index;
                break;
            }
        }

        if (target < 0) {
            target = 0;
            ItemStack displaced = stacks.getStackInSlot(target).copy();
            if (!displaced.isEmpty()) {
                player.drop(displaced, false);
            }
        }

        stacks.setStackInSlot(target, racialWings);
    }

    private static void repairAndMark(ItemStack wings) {
        if (wings.getEnchantmentLevel(Enchantments.BINDING_CURSE) <= 0) {
            wings.enchant(Enchantments.BINDING_CURSE, 1);
        }

        wings.getOrCreateTag().putBoolean(RACIAL_WING_TAG, true);
        wings.getOrCreateTag().putBoolean("Unbreakable", true);
        wings.setDamageValue(0);
    }
}
