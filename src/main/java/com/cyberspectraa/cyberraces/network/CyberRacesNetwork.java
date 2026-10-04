package com.cyberspectraa.cyberraces.network;

import com.cyberspectraa.cyberraces.CyberRaces;
import com.cyberspectraa.cyberraces.network.packet.CharacterSyncPacket;
import com.cyberspectraa.cyberraces.network.packet.CloseCharacterCreatorPacket;
import com.cyberspectraa.cyberraces.network.packet.OpenCharacterCreatorPacket;
import com.cyberspectraa.cyberraces.network.packet.SubmitCharacterPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class CyberRacesNetwork {
    private static final String PROTOCOL = "4";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(CyberRaces.MOD_ID, "main"),
        () -> PROTOCOL,
        PROTOCOL::equals,
        PROTOCOL::equals
    );

    private static int messageId;

    private CyberRacesNetwork() {
    }

    public static void init() {
        CHANNEL.messageBuilder(OpenCharacterCreatorPacket.class, messageId++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(OpenCharacterCreatorPacket::encode)
            .decoder(OpenCharacterCreatorPacket::decode)
            .consumerMainThread(OpenCharacterCreatorPacket::handle)
            .add();

        CHANNEL.messageBuilder(SubmitCharacterPacket.class, messageId++, NetworkDirection.PLAY_TO_SERVER)
            .encoder(SubmitCharacterPacket::encode)
            .decoder(SubmitCharacterPacket::decode)
            .consumerMainThread(SubmitCharacterPacket::handle)
            .add();

        CHANNEL.messageBuilder(CloseCharacterCreatorPacket.class, messageId++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(CloseCharacterCreatorPacket::encode)
            .decoder(CloseCharacterCreatorPacket::decode)
            .consumerMainThread(CloseCharacterCreatorPacket::handle)
            .add();

        CHANNEL.messageBuilder(CharacterSyncPacket.class, messageId++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(CharacterSyncPacket::encode)
            .decoder(CharacterSyncPacket::decode)
            .consumerMainThread(CharacterSyncPacket::handle)
            .add();
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
