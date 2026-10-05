package com.cyberspectraa.cyberraces.network.packet;

import com.cyberspectraa.cyberraces.ability.DragonBreathAbility;
import com.cyberspectraa.cyberraces.compat.IronSpellsCompat;
import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class DragonBreathPacket {
    public static void encode(
        DragonBreathPacket packet,
        FriendlyByteBuf buffer
    ) {
    }

    public static DragonBreathPacket decode(FriendlyByteBuf buffer) {
        return new DragonBreathPacket();
    }

    public static void handle(
        DragonBreathPacket packet,
        Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();

        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        if (RaceManager.getRace(player).orElse(null) != Race.DRAGONBORN) {
            player.displayClientMessage(
                Component.literal("Fire Breath is a Dragonborn racial ability."),
                true
            );
            context.setPacketHandled(true);
            return;
        }

        if (!IronSpellsCompat.isLoaded()) {
            player.displayClientMessage(
                Component.literal("Fire Breath requires Iron's Spells 'n Spellbooks."),
                true
            );
            context.setPacketHandled(true);
            return;
        }

        DragonBreathAbility.tryCast(player);
        context.setPacketHandled(true);
    }
}
