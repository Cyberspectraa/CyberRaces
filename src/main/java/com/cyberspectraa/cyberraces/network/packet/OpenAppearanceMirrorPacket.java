package com.cyberspectraa.cyberraces.network.packet;

import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import com.cyberspectraa.cyberraces.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record OpenAppearanceMirrorPacket(
        String raceId, String evolutionId, CharacterAppearance appearance) {

    public static void encode(OpenAppearanceMirrorPacket p, FriendlyByteBuf buf) {
        buf.writeUtf(p.raceId(), 32);
        buf.writeUtf(p.evolutionId(), 48);
        buf.writeNbt(p.appearance().save());
    }
    public static OpenAppearanceMirrorPacket decode(FriendlyByteBuf buf) {
        String race = buf.readUtf(32);
        String evolution = buf.readUtf(48);
        var tag = buf.readNbt();
        return new OpenAppearanceMirrorPacket(race, evolution,
                tag == null ? CharacterAppearance.defaults() : CharacterAppearance.load(tag));
    }
    public static void handle(OpenAppearanceMirrorPacket p,
            Supplier<NetworkEvent.Context> ctx) {
        ClientPacketHandlers.openAppearanceMirror(p.raceId(), p.evolutionId(), p.appearance());
        ctx.get().setPacketHandled(true);
    }
}
