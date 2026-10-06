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
    String evolutionId,
    int featureStyle,
    int featureColor,
    int earHeight,
    int earSpread,
    int earTilt,
    int bodySourceColor,
    int bodyTargetColor,
    int bodyTolerance
) {
    public static void encode(CharacterSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.playerId);
        buffer.writeBoolean(packet.created);
        buffer.writeUtf(packet.raceId, 32);
        buffer.writeUtf(packet.evolutionId, 48);
        buffer.writeVarInt(packet.featureStyle);
        buffer.writeInt(packet.featureColor);
        buffer.writeInt(packet.earHeight);
        buffer.writeInt(packet.earSpread);
        buffer.writeInt(packet.earTilt);
        buffer.writeInt(packet.bodySourceColor);
        buffer.writeInt(packet.bodyTargetColor);
        buffer.writeVarInt(packet.bodyTolerance);
    }

    public static CharacterSyncPacket decode(FriendlyByteBuf buffer) {
        return new CharacterSyncPacket(
            buffer.readUUID(),
            buffer.readBoolean(),
            buffer.readUtf(32),
            buffer.readUtf(48),
            buffer.readVarInt(),
            buffer.readInt(),
            buffer.readInt(),
            buffer.readInt(),
            buffer.readInt(),
            buffer.readInt(),
            buffer.readInt(),
            buffer.readVarInt()
        );
    }

    public static void handle(
        CharacterSyncPacket packet,
        Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        ClientCharacterState.apply(
            packet.playerId,
            packet.created,
            packet.raceId,
            packet.evolutionId,
            packet.featureStyle,
            packet.featureColor,
            packet.earHeight,
            packet.earSpread,
            packet.earTilt,
            packet.bodySourceColor,
            packet.bodyTargetColor,
            packet.bodyTolerance
        );
        context.setPacketHandled(true);
    }
}
