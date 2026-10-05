package com.cyberspectraa.cyberraces.network.packet;

import com.cyberspectraa.cyberraces.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record OpenRaceEvolutionPacket(String baseRaceId) {
    public static void encode(OpenRaceEvolutionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.baseRaceId(), 32);
    }

    public static OpenRaceEvolutionPacket decode(FriendlyByteBuf buffer) {
        return new OpenRaceEvolutionPacket(buffer.readUtf(32));
    }

    public static void handle(
        OpenRaceEvolutionPacket packet,
        Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();

        DistExecutor.unsafeRunWhenOn(
            Dist.CLIENT,
            () -> () -> ClientPacketHandlers.openRaceEvolution(packet.baseRaceId())
        );

        context.setPacketHandled(true);
    }
}
