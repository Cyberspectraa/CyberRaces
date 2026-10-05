package com.cyberspectraa.cyberraces.network.packet;

import com.cyberspectraa.cyberraces.client.ClientCharacterState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record EntityRaceSyncPacket(
    UUID entityId,
    boolean active,
    String raceId,
    int featureStyle,
    int featureColor,
    int earHeight,
    int earSpread,
    int earTilt,
    int bodySourceColor,
    int bodyTargetColor,
    int bodyTolerance
) {
    public static EntityRaceSyncPacket clear(UUID entityId) {
        return new EntityRaceSyncPacket(
            entityId,
            false,
            "",
            0,
            -1,
            0,
            0,
            0,
            -1,
            -1,
            22
        );
    }

    public static void encode(
        EntityRaceSyncPacket packet,
        FriendlyByteBuf buffer
    ) {
        buffer.writeUUID(packet.entityId);
        buffer.writeBoolean(packet.active);
        buffer.writeUtf(packet.raceId, 32);
        buffer.writeVarInt(packet.featureStyle);
        buffer.writeInt(packet.featureColor);
        buffer.writeInt(packet.earHeight);
        buffer.writeInt(packet.earSpread);
        buffer.writeInt(packet.earTilt);
        buffer.writeInt(packet.bodySourceColor);
        buffer.writeInt(packet.bodyTargetColor);
        buffer.writeVarInt(packet.bodyTolerance);
    }

    public static EntityRaceSyncPacket decode(FriendlyByteBuf buffer) {
        return new EntityRaceSyncPacket(
            buffer.readUUID(),
            buffer.readBoolean(),
            buffer.readUtf(32),
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
        EntityRaceSyncPacket packet,
        Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();

        ClientCharacterState.apply(
            packet.entityId,
            packet.active,
            packet.raceId,
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
