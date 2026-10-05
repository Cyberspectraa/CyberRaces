package com.cyberspectraa.cyberraces.ability;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class FairyHoverAbility {
    private static final Set<UUID> ENABLED = new HashSet<>();

    private FairyHoverAbility() {
    }

    public static void setEnabled(
        ServerPlayer player,
        boolean enabled
    ) {
        if (enabled) {
            ENABLED.add(player.getUUID());
        } else {
            ENABLED.remove(player.getUUID());
        }

        player.displayClientMessage(
            Component.literal(
                enabled
                    ? "Fairy Hover enabled"
                    : "Fairy Hover disabled"
            ),
            true
        );
    }

    public static boolean isEnabled(ServerPlayer player) {
        return ENABLED.contains(player.getUUID());
    }

    public static void disable(ServerPlayer player) {
        ENABLED.remove(player.getUUID());
    }
}
