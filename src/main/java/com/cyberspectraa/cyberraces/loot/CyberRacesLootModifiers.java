package com.cyberspectraa.cyberraces.loot;

import com.cyberspectraa.cyberraces.CyberRaces;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Global loot modifier serializer registration, required by Forge 1.20.1. */
public final class CyberRacesLootModifiers {
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> MODIFIERS =
            DeferredRegister.create(
                    ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                    CyberRaces.MOD_ID);

    public static final RegistryObject<Codec<DwarfFortuneModifier>> DWARF_FORTUNE =
            MODIFIERS.register("dwarf_fortune", () -> DwarfFortuneModifier.CODEC);

    private CyberRacesLootModifiers() {}

    public static void register(IEventBus modBus) {
        MODIFIERS.register(modBus);
    }
}
