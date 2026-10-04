package com.cyberspectraa.cyberraces.race;

import com.cyberspectraa.cyberraces.compat.IronSpellsCompat;
import com.cyberspectraa.cyberraces.compat.PehkuiCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public final class RaceAttributeApplier {
    private static final UUID HEALTH_ID = UUID.fromString("81df0db5-3709-4743-bae0-7c9f17e5e5bf");
    private static final UUID SPEED_ID = UUID.fromString("26b9515c-7ff3-4dc1-8b94-8bd3eb4cb21c");
    private static final UUID KNOCKBACK_ID = UUID.fromString("cb0414bd-1949-435b-9473-cb5193c28e45");
    private static final UUID ARMOR_ID = UUID.fromString("8e1459d0-e246-42f5-bf43-c999957c9bc4");

    private RaceAttributeApplier() {
    }

    public static void apply(ServerPlayer player, Race race) {
        clearVanilla(player);

        add(player.getAttribute(Attributes.MAX_HEALTH), HEALTH_ID,
            "CyberRaces racial health", race.maxHealth() - 20.0, AttributeModifier.Operation.ADDITION);

        add(player.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_ID,
            "CyberRaces racial movement", race.movementMultiplier() - 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);

        // Vanilla knockback resistance cannot meaningfully represent values below zero,
        // so negative race values are treated as no bonus rather than creating a broken modifier.
        if (race.knockbackResistanceBonus() > 0.0) {
            add(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE), KNOCKBACK_ID,
                "CyberRaces racial knockback resistance", race.knockbackResistanceBonus(), AttributeModifier.Operation.ADDITION);
        }

        if (race.armorBonus() != 0.0) {
            add(player.getAttribute(Attributes.ARMOR), ARMOR_ID,
                "CyberRaces racial natural armor", race.armorBonus(), AttributeModifier.Operation.ADDITION);
        }

        PehkuiCompat.applyScale(player, race.scale());
        IronSpellsCompat.apply(player, race);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    public static void clear(ServerPlayer player) {
        clearVanilla(player);
        PehkuiCompat.resetScale(player);
        IronSpellsCompat.clear(player);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void clearVanilla(ServerPlayer player) {
        remove(player.getAttribute(Attributes.MAX_HEALTH), HEALTH_ID);
        remove(player.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_ID);
        remove(player.getAttribute(Attributes.KNOCKBACK_RESISTANCE), KNOCKBACK_ID);
        remove(player.getAttribute(Attributes.ARMOR), ARMOR_ID);
    }

    private static void add(
        AttributeInstance instance,
        UUID id,
        String name,
        double amount,
        AttributeModifier.Operation operation
    ) {
        if (instance == null || Math.abs(amount) < 0.00001) {
            return;
        }

        instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id, name, amount, operation));
    }

    private static void remove(AttributeInstance instance, UUID id) {
        if (instance != null) {
            instance.removeModifier(id);
        }
    }
}
