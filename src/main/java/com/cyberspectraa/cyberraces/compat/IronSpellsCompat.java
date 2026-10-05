package com.cyberspectraa.cyberraces.compat;

import com.cyberspectraa.cyberraces.race.Race;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public final class IronSpellsCompat {
    private static final String MOD_ID = "irons_spellbooks";

    private static final ResourceLocation MAX_MANA = new ResourceLocation(MOD_ID, "max_mana");
    private static final ResourceLocation MANA_REGEN = new ResourceLocation(MOD_ID, "mana_regen");
    private static final ResourceLocation SPELL_RESIST = new ResourceLocation(MOD_ID, "spell_resist");

    private static final UUID MAX_MANA_ID = UUID.fromString("1d5d73d4-5d51-4871-b755-d9fe80748f08");
    private static final UUID MANA_REGEN_ID = UUID.fromString("d3069500-0e4e-4c9e-b27a-d2a9b506c3bb");
    private static final UUID SPELL_RESIST_ID = UUID.fromString("c6909862-87f5-490f-9da1-12ba0d71ca03");

    private IronSpellsCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static void apply(LivingEntity entity, Race race) {
        if (!isLoaded() || entity == null || race == null) {
            return;
        }

        applyMultiplier(entity, MAX_MANA, MAX_MANA_ID, "CyberRaces racial max mana", race.maxManaMultiplier());
        applyMultiplier(entity, MANA_REGEN, MANA_REGEN_ID, "CyberRaces racial mana regeneration", race.manaRegenMultiplier());
        applyMultiplier(entity, SPELL_RESIST, SPELL_RESIST_ID, "CyberRaces racial spell resistance", race.spellResistanceMultiplier());
    }

    public static void clear(LivingEntity entity) {
        if (!isLoaded() || entity == null) {
            return;
        }

        remove(entity, MAX_MANA, MAX_MANA_ID);
        remove(entity, MANA_REGEN, MANA_REGEN_ID);
        remove(entity, SPELL_RESIST, SPELL_RESIST_ID);
    }

    private static void applyMultiplier(
        LivingEntity entity,
        ResourceLocation attributeId,
        UUID modifierId,
        String name,
        double multiplier
    ) {
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(attributeId);
        if (attribute == null) {
            return;
        }

        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        instance.removeModifier(modifierId);

        double amount = multiplier - 1.0;
        if (Math.abs(amount) < 0.00001) {
            return;
        }

        instance.addTransientModifier(new AttributeModifier(
            modifierId,
            name,
            amount,
            AttributeModifier.Operation.MULTIPLY_BASE
        ));
    }

    private static void remove(
        LivingEntity entity,
        ResourceLocation attributeId,
        UUID modifierId
    ) {
        Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(attributeId);
        if (attribute == null) {
            return;
        }

        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(modifierId);
        }
    }
}
