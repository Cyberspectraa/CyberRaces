package com.cyberspectraa.cyberraces.network.packet;

import com.cyberspectraa.cyberraces.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class CloseCharacterCreatorPacket {
    public static void encode(CloseCharacterCreatorPacket packet, FriendlyByteBuf buffer) {
    }

    public static CloseCharacterCreatorPacket decode(FriendlyByteBuf buffer) {
        return new CloseCharacterCreatorPacket();
    }

    public static void handle(CloseCharacterCreatorPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ClientPacketHandlers.closeCharacterCreator();
        context.setPacketHandled(true);
    }
}
