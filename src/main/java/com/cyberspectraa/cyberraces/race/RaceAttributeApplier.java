package com.cyberspectraa.cyberraces.race;

import com.cyberspectraa.cyberraces.compat.IronSpellsCompat;
import com.cyberspectraa.cyberraces.compat.PehkuiCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public final class RaceAttributeApplier {
    private static final UUID HEALTH_ID = UUID.fromString("81df0db5-3709-4743-bae0-7c9f17e5e5bf");
    private static final UUID SPEED_ID = UUID.fromString("26b9515c-7ff3-4dc1-8b94-8bd3eb4cb21c");
    private static final UUID KNOCKBACK_ID = UUID.fromString("cb0414bd-1949-435b-9473-cb5193c28e45");
    private static final UUID ARMOR_ID = UUID.fromString("8e1459d0-e246-42f5-bf43-c999957c9bc4");
    private static final UUID LUCK_ID = UUID.fromString("05e2a1fb-a0de-4a38-b985-f2980c921e62");

    private RaceAttributeApplier() {
    }

    public static void apply(ServerPlayer player, Race race) {
        apply((LivingEntity) player, race, null);
    }

    public static void apply(
        ServerPlayer player,
        Race race,
        RaceEvolution evolution
    ) {
        apply((LivingEntity) player, race, evolution);
    }

    public static void apply(LivingEntity entity, Race race) {
        apply(entity, race, null);
    }

    public static void apply(
        LivingEntity entity,
        Race race,
        RaceEvolution evolution
    ) {
        if (entity == null || race == null) {
            return;
        }

        clearVanilla(entity);

        RaceEvolutionStats stats = evolution == null
            ? RaceEvolutionStats.NEUTRAL
            : evolution.stats();

        double maxHealth =
            race.maxHealth() + stats.healthBonus();
        double movement =
            race.movementMultiplier()
                * stats.movementMultiplier();
        double knockback =
            race.knockbackResistanceBonus()
                + stats.knockbackResistanceBonus();
        double armor =
            race.armorBonus() + stats.armorBonus();

        add(entity.getAttribute(Attributes.MAX_HEALTH), HEALTH_ID,
            "CyberRaces racial health", maxHealth - 20.0, AttributeModifier.Operation.ADDITION);

        add(entity.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_ID,
            "CyberRaces racial movement", movement - 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);

        if (knockback > 0.0) {
            add(entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE), KNOCKBACK_ID,
                "CyberRaces racial knockback resistance", knockback, AttributeModifier.Operation.ADDITION);
        }

        if (armor != 0.0) {
            add(entity.getAttribute(Attributes.ARMOR), ARMOR_ID,
                "CyberRaces racial natural armor", armor, AttributeModifier.Operation.ADDITION);
        }

        double luckBonus = switch (race) {
            case HALFLING -> 1.0;
            case GOBLIN -> 0.5;
            default -> 0.0;
        };

        luckBonus += stats.luckBonus();

        if (luckBonus != 0.0) {
            add(entity.getAttribute(Attributes.LUCK), LUCK_ID,
                "CyberRaces racial luck", luckBonus, AttributeModifier.Operation.ADDITION);
        }

        PehkuiCompat.applyScale(
            entity,
            (float) (
                race.scale()
                    * stats.scaleMultiplier()
            )
        );
        IronSpellsCompat.apply(entity, race, evolution);

        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }

    public static void clear(ServerPlayer player) {
        clear((LivingEntity) player);
    }

    public static void clear(LivingEntity entity) {
        if (entity == null) {
            return;
        }

        clearVanilla(entity);
        PehkuiCompat.resetScale(entity);
        IronSpellsCompat.clear(entity);

        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }
    }

    private static void clearVanilla(LivingEntity entity) {
        remove(entity.getAttribute(Attributes.MAX_HEALTH), HEALTH_ID);
        remove(entity.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_ID);
        remove(entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE), KNOCKBACK_ID);
        remove(entity.getAttribute(Attributes.ARMOR), ARMOR_ID);
        remove(entity.getAttribute(Attributes.LUCK), LUCK_ID);
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
