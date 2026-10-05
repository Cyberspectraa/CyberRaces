package com.cyberspectraa.cyberraces.network.packet;

import com.cyberspectraa.cyberraces.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CloseRaceEvolutionPacket() {
    public static void encode(CloseRaceEvolutionPacket packet, FriendlyByteBuf buffer) {
    }

    public static CloseRaceEvolutionPacket decode(FriendlyByteBuf buffer) {
        return new CloseRaceEvolutionPacket();
    }

    public static void handle(
        CloseRaceEvolutionPacket packet,
        Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();

        DistExecutor.unsafeRunWhenOn(
            Dist.CLIENT,
            () -> ClientPacketHandlers::closeRaceEvolution
        );

        context.setPacketHandled(true);
    }
}
