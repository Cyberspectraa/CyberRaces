package com.cyberspectraa.cyberraces.network.packet;

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

        // Keep the old packet class for compatibility, but never cast
        // Dragonborn breath through a legacy client.
        player.displayClientMessage(
            Component.literal("Dragonborn racial abilities have been retired."),
            true
        );
        context.setPacketHandled(true);
    }
}
