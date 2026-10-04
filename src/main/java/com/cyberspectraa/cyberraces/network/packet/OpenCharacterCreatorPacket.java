package com.cyberspectraa.cyberraces.network.packet;

import com.cyberspectraa.cyberraces.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class OpenCharacterCreatorPacket {
    public static void encode(OpenCharacterCreatorPacket packet, FriendlyByteBuf buffer) {
    }

    public static OpenCharacterCreatorPacket decode(FriendlyByteBuf buffer) {
        return new OpenCharacterCreatorPacket();
    }

    public static void handle(OpenCharacterCreatorPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ClientPacketHandlers.openCharacterCreator();
        context.setPacketHandled(true);
    }
}
