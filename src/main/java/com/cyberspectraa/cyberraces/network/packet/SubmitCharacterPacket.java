package com.cyberspectraa.cyberraces.network.packet;

import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import com.cyberspectraa.cyberraces.character.CharacterManager;
import com.cyberspectraa.cyberraces.race.Race;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SubmitCharacterPacket(
    String raceId,
    int eyeStyle,
    int eyeColor,
    int featureStyle
) {
    public static void encode(SubmitCharacterPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.raceId, 32);
        buffer.writeVarInt(packet.eyeStyle);
        buffer.writeVarInt(packet.eyeColor);
        buffer.writeVarInt(packet.featureStyle);
    }

    public static SubmitCharacterPacket decode(FriendlyByteBuf buffer) {
        return new SubmitCharacterPacket(
            buffer.readUtf(32),
            buffer.readVarInt(),
            buffer.readVarInt(),
            buffer.readVarInt()
        );
    }

    public static void handle(SubmitCharacterPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        Race.byId(packet.raceId).ifPresent(race ->
            CharacterManager.completeCharacter(
                player,
                race,
                new CharacterAppearance(packet.eyeStyle, packet.eyeColor, packet.featureStyle)
            )
        );

        context.setPacketHandled(true);
    }
}
