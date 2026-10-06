package com.cyberspectraa.cyberraces.ability;

import com.cyberspectraa.cyberraces.character.CharacterManager;
import com.cyberspectraa.cyberraces.race.RaceEvolution;
import com.cyberspectraa.cyberraces.race.RaceManager;
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

public final class AasimarRadianceAbility {
    private static final String READY_TICK_KEY = "AasimarRadianceReadyTick";
    private static final Map<UUID, ActiveCast> ACTIVE = new HashMap<>();

    private AasimarRadianceAbility() {
    }

    public static void tryCast(ServerPlayer player) {
        AbilityProfile profile = profile(player);
        long now = player.level().getGameTime();
        long readyAt = getReadyTick(player);

        if (readyAt > now) {
            player.displayClientMessage(
                Component.literal(
                    String.format(
                        "%s: %.1fs cooldown",
                        profile.name(),
                        (readyAt - now) / 20.0D
                    )
                ),
                true
            );
            return;
        }

        MagicData magicData = MagicData.getPlayerMagicData(player);

        if (magicData.isCasting()) {
            player.displayClientMessage(
                Component.literal(
                    "Finish your current spell before using "
                        + profile.name() + "."
                ),
                true
            );
            return;
        }

        AbstractSpell spell = profile.spell();
        int level = Math.min(
            profile.level(),
            Math.max(1, spell.getMaxLevel())
        );
        int effectiveCastTime = spell.getEffectiveCastTime(level, player);

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
            "cyberraces_aasimar_innate"
        );

        if (!started) {
            return;
        }

        setReadyTick(
            player,
            now + effectiveCastTime + profile.cooldownTicks()
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

        MagicData magicData = MagicData.getPlayerMagicData(player);

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

    private static AbilityProfile profile(ServerPlayer player) {
        RaceEvolution evolution =
            RaceManager.getEvolution(player).orElse(null);

        if (evolution == RaceEvolution.FALLEN) {
            return new AbilityProfile(
                "Grave Bolt",
                SpellRegistry.WITHER_SKULL_SPELL.get(),
                5,
                18 * 20
            );
        }

        if (evolution == RaceEvolution.SERAPHIC) {
            return new AbilityProfile(
                "Radiant Bolt",
                SpellRegistry.GUIDING_BOLT_SPELL.get(),
                6,
                16 * 20
            );
        }

        return new AbilityProfile(
            "Radiant Bolt",
            SpellRegistry.GUIDING_BOLT_SPELL.get(),
            evolution == RaceEvolution.CELESTIAL_GUARDIAN ? 4 : 3,
            20 * 20
        );
    }

    private static void restoreIronCooldown(
        ServerPlayer player,
        ActiveCast active,
        long elapsed
    ) {
        MagicData magicData = MagicData.getPlayerMagicData(player);
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
        CompoundTag root = player.getPersistentData()
            .getCompound(CharacterManager.ROOT_KEY);
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

    private record AbilityProfile(
        String name,
        AbstractSpell spell,
        int level,
        int cooldownTicks
    ) {
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
