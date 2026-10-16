package com.cyberspectraa.cyberraces.network.packet;

import com.cyberspectraa.cyberraces.character.CharacterAppearance;
import com.cyberspectraa.cyberraces.character.CharacterManager;
import com.cyberspectraa.cyberraces.item.AppearanceMirrorItem;
import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public record SaveAppearanceMirrorPacket(CharacterAppearance appearance) {
    public static void encode(SaveAppearanceMirrorPacket packet, FriendlyByteBuf buf) {
        buf.writeNbt(packet.appearance().save());
    }

    public static SaveAppearanceMirrorPacket decode(FriendlyByteBuf buf) {
        var tag = buf.readNbt();
        return new SaveAppearanceMirrorPacket(tag == null
                ? CharacterAppearance.defaults() : CharacterAppearance.load(tag));
    }

    public static void handle(SaveAppearanceMirrorPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();
        if (player != null && CharacterManager.isCharacterCreated(player)
                && (player.getMainHandItem().is(AppearanceMirrorItem.MIRROR.get())
                    || player.getOffhandItem().is(AppearanceMirrorItem.MIRROR.get()))) {
            CharacterManager.changeAppearance(player, packet.appearance());
            CyberRacesNetwork.sendToPlayer(player, new CloseCharacterCreatorPacket());
        }
        context.setPacketHandled(true);
    }
}
