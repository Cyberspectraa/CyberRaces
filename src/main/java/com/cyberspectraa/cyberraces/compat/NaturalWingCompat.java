package com.cyberspectraa.cyberraces.compat;

import com.cyberspectraa.cyberraces.CyberRaces;
import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import com.cyberspectraa.cyberraces.character.CharacterManager;
import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceEvolution;
import com.cyberspectraa.cyberraces.race.RaceManager;
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

public final class NaturalWingCompat {
    private static final String BACK_SLOT = "back";
    private static final String NATURAL_WING_TAG = "CyberRacesNaturalWing";
    private static final String NATURAL_WING_KIND = "CyberRacesNaturalWingKind";

    private static final String[] DYE_NAMES = {
        "white", "orange", "magenta", "light_blue",
        "yellow", "lime", "pink", "gray",
        "light_gray", "cyan", "purple", "blue",
        "brown", "green", "red", "black"
    };

    private static final int[] DYE_RGB = {
        0xF9FFFE, 0xF9801D, 0xC74EBD, 0x3AB3DA,
        0xFED83D, 0x80C71F, 0xF38BAA, 0x474F52,
        0x9D9D97, 0x169C9C, 0x8932B8, 0x3C44AA,
        0x835432, 0x5E7C16, 0xB02E26, 0x1D1D21
    };

    private NaturalWingCompat() {
    }

    public static boolean isNaturalWing(ItemStack stack) {
        return !stack.isEmpty()
            && stack.hasTag()
            && stack.getTag() != null
            && stack.getTag().getBoolean(NATURAL_WING_TAG);
    }

    public static ItemStack getEquippedNaturalWing(LivingEntity entity) {
        Optional<top.theillusivec4.curios.api.type.capability.ICuriosItemHandler> optional =
            CuriosApi.getCuriosInventory(entity).resolve();

        if (optional.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (Map.Entry<String, ICurioStacksHandler> entry : optional.get().getCurios().entrySet()) {
            IDynamicStackHandler stacks = entry.getValue().getStacks();
            for (int index = 0; index < stacks.getSlots(); index++) {
                ItemStack stack = stacks.getStackInSlot(index);
                if (isNaturalWing(stack)) {
                    return stack;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    public static void ensureCorrectWing(ServerPlayer player) {
        if (player == null || !IcarusCompat.isLoaded()) {
            return;
        }

        ResourceLocation desired = desiredWing(player);
        ItemStack existing = getEquippedNaturalWing(player);

        if (desired == null) {
            if (!existing.isEmpty()) {
                removeNaturalWings(player);
            }
            return;
        }

        ResourceLocation current = existing.isEmpty()
            ? null
            : ForgeRegistries.ITEMS.getKey(existing.getItem());

        if (desired.equals(current)) {
            repairAndMark(existing, desired.toString());
            return;
        }

        removeNaturalWings(player);

        Item wingItem = ForgeRegistries.ITEMS.getValue(desired);
        if (wingItem == null || wingItem == Items.AIR) {
            CyberRaces.LOGGER.error("Could not find Icarus natural wing item {}", desired);
            return;
        }

        ItemStack wings = new ItemStack(wingItem);
        repairAndMark(wings, desired.toString());

        CuriosApi.getCuriosInventory(player).resolve().ifPresent(handler ->
            handler.getStacksHandler(BACK_SLOT).ifPresentOrElse(
                backHandler -> equipIntoBackSlot(player, backHandler, wings),
                () -> CyberRaces.LOGGER.error(
                    "Player {} has no Curios '{}' slot for natural wings",
                    player.getGameProfile().getName(),
                    BACK_SLOT
                )
            )
        );
    }

    public static void removeNaturalWings(ServerPlayer player) {
        if (player == null) {
            return;
        }

        CuriosApi.getCuriosInventory(player).resolve().ifPresent(handler -> {
            for (ICurioStacksHandler curioHandler : handler.getCurios().values()) {
                IDynamicStackHandler stacks = curioHandler.getStacks();
                for (int index = 0; index < stacks.getSlots(); index++) {
                    if (isNaturalWing(stacks.getStackInSlot(index))) {
                        stacks.setStackInSlot(index, ItemStack.EMPTY);
                    }
                }
            }
        });
    }

    private static ResourceLocation desiredWing(ServerPlayer player) {
        Race race = RaceManager.getRace(player).orElse(null);
        RaceEvolution evolution = RaceManager.getEvolution(player).orElse(null);
        CharacterAppearance appearance = CharacterManager.getAppearance(player);

        if (race == Race.BIRDFOLK) {
            int index = Math.floorMod(appearance.featureStyle(), DYE_NAMES.length);
            return icarus(DYE_NAMES[index] + "_feathered_wings");
        }

        if (race == Race.DRAGONBORN
                && evolution == RaceEvolution.SKYBORN) {
            int rgb = appearance.featureColor() == CharacterAppearance.AUTO_COLOR
                ? 0x76AFA1
                : appearance.featureColor();
            return icarus(DYE_NAMES[nearestDye(rgb)] + "_dragon_wings");
        }

        if (race == Race.AASIMAR && evolution != null) {
            return switch (evolution) {
                case SERAPHIC -> icarus("white_light_wings");
                case FALLEN -> icarus("black_light_wings");
                case CELESTIAL_GUARDIAN -> icarus("yellow_light_wings");
                default -> null;
            };
        }

        return null;
    }

    private static ResourceLocation icarus(String path) {
        return new ResourceLocation("icarus", path);
    }

    private static int nearestDye(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        long bestDistance = Long.MAX_VALUE;
        int best = 0;

        for (int i = 0; i < DYE_RGB.length; i++) {
            int dr = r - ((DYE_RGB[i] >> 16) & 0xFF);
            int dg = g - ((DYE_RGB[i] >> 8) & 0xFF);
            int db = b - (DYE_RGB[i] & 0xFF);
            long distance = (long) dr * dr + (long) dg * dg + (long) db * db;

            if (distance < bestDistance) {
                bestDistance = distance;
                best = i;
            }
        }

        return best;
    }

    private static void equipIntoBackSlot(
        ServerPlayer player,
        ICurioStacksHandler backHandler,
        ItemStack wings
    ) {
        IDynamicStackHandler stacks = backHandler.getStacks();

        if (stacks.getSlots() <= 0) {
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

            if (!displaced.isEmpty()
                    && !FairyWingCompat.isRacialFairyWing(displaced)
                    && !isNaturalWing(displaced)) {
                player.drop(displaced, false);
            }
        }

        stacks.setStackInSlot(target, wings);
    }

    private static void repairAndMark(ItemStack wings, String kind) {
        if (wings.getEnchantmentLevel(Enchantments.BINDING_CURSE) <= 0) {
            wings.enchant(Enchantments.BINDING_CURSE, 1);
        }

        wings.getOrCreateTag().putBoolean(NATURAL_WING_TAG, true);
        wings.getOrCreateTag().putString(NATURAL_WING_KIND, kind);
        wings.getOrCreateTag().putBoolean("Unbreakable", true);
        wings.setDamageValue(0);
    }
}
