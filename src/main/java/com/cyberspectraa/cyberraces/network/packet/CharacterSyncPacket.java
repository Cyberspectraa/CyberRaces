package com.cyberspectraa.cyberraces.network.packet;

import com.cyberspectraa.cyberraces.client.ClientCharacterState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record CharacterSyncPacket(
    UUID playerId,
    boolean created,
    String raceId,
    int featureStyle,
    int featureColor,
    int earHeight,
    int earSpread,
    int earTilt
) {
    public static void encode(CharacterSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.playerId);
        buffer.writeBoolean(packet.created);
        buffer.writeUtf(packet.raceId, 32);
        buffer.writeVarInt(packet.featureStyle);
        buffer.writeInt(packet.featureColor);
        buffer.writeInt(packet.earHeight);
        buffer.writeInt(packet.earSpread);
        buffer.writeInt(packet.earTilt);
    }

    public static CharacterSyncPacket decode(FriendlyByteBuf buffer) {
        return new CharacterSyncPacket(
            buffer.readUUID(),
            buffer.readBoolean(),
            buffer.readUtf(32),
            buffer.readVarInt(),
            buffer.readInt(),
            buffer.readInt(),
            buffer.readInt(),
            buffer.readInt()
        );
    }

    public static void handle(CharacterSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ClientCharacterState.apply(
            packet.playerId,
            packet.created,
            packet.raceId,
            packet.featureStyle,
            packet.featureColor,
            packet.earHeight,
            packet.earSpread,
            packet.earTilt
        );
        context.setPacketHandled(true);
    }
}
