package com.cyberspectraa.cyberraces.network.packet;

import com.cyberspectraa.cyberraces.race.RaceEvolution;
import com.cyberspectraa.cyberraces.race.RaceEvolutionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SubmitRaceEvolutionPacket(String evolutionId) {
    public static void encode(SubmitRaceEvolutionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.evolutionId(), 48);
    }

    public static SubmitRaceEvolutionPacket decode(FriendlyByteBuf buffer) {
        return new SubmitRaceEvolutionPacket(buffer.readUtf(48));
    }

    public static void handle(
        SubmitRaceEvolutionPacket packet,
        Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();

        if (player != null) {
            RaceEvolution.byId(packet.evolutionId())
                .ifPresent(evolution ->
                    RaceEvolutionManager.complete(player, evolution)
                );
        }

        context.setPacketHandled(true);
    }
}
