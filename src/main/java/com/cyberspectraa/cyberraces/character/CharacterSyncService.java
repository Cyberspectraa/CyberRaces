package com.cyberspectraa.cyberraces.character;

import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.CharacterSyncPacket;
import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public final class CharacterSyncService {
    private CharacterSyncService() {
    }

    public static void syncTo(ServerPlayer subject, ServerPlayer receiver) {
        CyberRacesNetwork.sendToPlayer(receiver, packetFor(subject));
    }

    public static void syncAllTo(ServerPlayer receiver) {
        if (receiver.getServer() == null) {
            return;
        }

        for (ServerPlayer subject : receiver.getServer().getPlayerList().getPlayers()) {
            syncTo(subject, receiver);
        }
    }

    public static void broadcast(ServerPlayer subject) {
        CyberRacesNetwork.CHANNEL.send(
            PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> subject),
            packetFor(subject)
        );
    }

    private static CharacterSyncPacket packetFor(ServerPlayer player) {
        boolean created = CharacterManager.isCharacterCreated(player);
        Race race = RaceManager.getRace(player).orElse(Race.HUMAN);
        CharacterAppearance appearance = CharacterManager.getAppearance(player);

        return new CharacterSyncPacket(
            player.getUUID(),
            created,
            race.id(),
            appearance.featureStyle(),
            appearance.featureColor(),
            appearance.earHeight(),
            appearance.earSpread(),
            appearance.earTilt()
        );
    }
}
