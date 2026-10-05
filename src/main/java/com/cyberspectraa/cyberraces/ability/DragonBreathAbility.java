package com.cyberspectraa.cyberraces.ability;

import com.cyberspectraa.cyberraces.character.CharacterManager;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.capabilities.magic.CooldownInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DragonBreathAbility {
    public static final int SPELL_LEVEL = 10;
    public static final int COOLDOWN_TICKS = 18 * 20;

    private static final String READY_TICK_KEY =
        "DragonBreathReadyTick";

    private static final Map<UUID, ActiveCast> ACTIVE =
        new HashMap<>();

    private DragonBreathAbility() {
    }

    public static void tryCast(ServerPlayer player) {
        long now = player.level().getGameTime();
        long readyAt = getReadyTick(player);

        if (readyAt > now) {
            double seconds = (readyAt - now) / 20.0D;
            player.displayClientMessage(
                Component.literal(
                    String.format(
                        "Fire Breath: %.1fs cooldown",
                        seconds
                    )
                ),
                true
            );
            return;
        }

        MagicData magicData =
            MagicData.getPlayerMagicData(player);

        if (magicData.isCasting()) {
            player.displayClientMessage(
                Component.literal(
                    "Finish your current spell before using Fire Breath."
                ),
                true
            );
            return;
        }

        AbstractSpell spell =
            SpellRegistry.FIRE_BREATH_SPELL.get();

        int level = Math.min(
            SPELL_LEVEL,
            Math.max(1, spell.getMaxLevel())
        );

        int effectiveCastTime =
            spell.getEffectiveCastTime(level, player);

        var cooldowns = magicData.getPlayerCooldowns();
        CooldownInstance existing =
            cooldowns.getSpellCooldowns().get(spell.getSpellId());

        PreviousCooldown previous = existing == null
            ? null
            : new PreviousCooldown(
                existing.getSpellCooldown(),
                existing.getCooldownRemaining()
            );

        boolean started = spell.attemptInitiateCast(
            ItemStack.EMPTY,
            level,
            player.level(),
            player,
            CastSource.NONE,
            false,
            "cyberraces_dragon_breath"
        );

        if (!started) {
            return;
        }

        /*
         * Start the racial timer immediately and include the channel duration,
         * which makes the ability available 18 seconds after a normal
         * five-second Fire Breath finishes. It also prevents relogging during
         * the channel from bypassing the cooldown.
         */
        setReadyTick(
            player,
            now + effectiveCastTime + COOLDOWN_TICKS
        );

        ACTIVE.put(
            player.getUUID(),
            new ActiveCast(
                spell.getSpellId(),
                now,
                effectiveCastTime,
                previous
            )
        );
    }

    public static void tick(ServerPlayer player) {
        ActiveCast active = ACTIVE.get(player.getUUID());
        if (active == null) {
            return;
        }

        MagicData magicData =
            MagicData.getPlayerMagicData(player);

        boolean sameCast =
            magicData.isCasting()
                && active.spellId().equals(
                    magicData.getCastingSpellId()
                );

        long elapsed =
            player.level().getGameTime() - active.startedAt();

        if (sameCast
            && elapsed <= active.expectedCastTicks() + 20L) {
            return;
        }

        restoreIronCooldown(player, active, elapsed);
        ACTIVE.remove(player.getUUID());
    }

    public static void cleanup(ServerPlayer player) {
        ActiveCast active = ACTIVE.remove(player.getUUID());
        if (active == null) {
            return;
        }

        long elapsed =
            player.level().getGameTime() - active.startedAt();

        restoreIronCooldown(player, active, elapsed);
    }

    public static long getRemainingTicks(ServerPlayer player) {
        return Math.max(
            0L,
            getReadyTick(player) - player.level().getGameTime()
        );
    }

    private static void restoreIronCooldown(
        ServerPlayer player,
        ActiveCast active,
        long elapsed
    ) {
        MagicData magicData =
            MagicData.getPlayerMagicData(player);

        var cooldowns = magicData.getPlayerCooldowns();
        cooldowns.removeCooldown(active.spellId());

        if (active.previousCooldown() != null) {
            int remaining = Math.max(
                0,
                active.previousCooldown().remaining()
                    - (int) Math.min(Integer.MAX_VALUE, elapsed)
            );

            if (remaining > 0) {
                cooldowns.addCooldown(
                    active.spellId(),
                    active.previousCooldown().duration(),
                    remaining
                );
            }
        }

        cooldowns.syncToPlayer(player);
    }

    private static long getReadyTick(ServerPlayer player) {
        CompoundTag root =
            player.getPersistentData().getCompound(
                CharacterManager.ROOT_KEY
            );

        return root.getLong(READY_TICK_KEY);
    }

    private static void setReadyTick(
        ServerPlayer player,
        long readyTick
    ) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag root =
            persistent.contains(CharacterManager.ROOT_KEY)
                ? persistent.getCompound(CharacterManager.ROOT_KEY)
                : new CompoundTag();

        root.putLong(READY_TICK_KEY, readyTick);
        persistent.put(CharacterManager.ROOT_KEY, root);
    }

    private record PreviousCooldown(
        int duration,
        int remaining
    ) {
    }

    private record ActiveCast(
        String spellId,
        long startedAt,
        int expectedCastTicks,
        PreviousCooldown previousCooldown
    ) {
    }
}
