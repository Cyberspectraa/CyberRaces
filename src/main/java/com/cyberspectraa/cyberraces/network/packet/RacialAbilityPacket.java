package com.cyberspectraa.cyberraces.network.packet;

import com.cyberspectraa.cyberraces.ability.AasimarRadianceAbility;
import com.cyberspectraa.cyberraces.ability.BirdfolkWingBurstAbility;
import com.cyberspectraa.cyberraces.ability.DogfolkScentAbility;
import com.cyberspectraa.cyberraces.ability.FoxfolkQuickstepAbility;
import com.cyberspectraa.cyberraces.ability.GoblinScavengerSenseAbility;
import com.cyberspectraa.cyberraces.ability.OrcWarCryAbility;
import com.cyberspectraa.cyberraces.ability.NymphNatureGraceAbility;
import com.cyberspectraa.cyberraces.ability.DragonBreathAbility;
import com.cyberspectraa.cyberraces.ability.FairyHoverAbility;
import com.cyberspectraa.cyberraces.compat.IronSpellsCompat;
import com.cyberspectraa.cyberraces.race.Race;
import com.cyberspectraa.cyberraces.race.RaceManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RacialAbilityPacket(
    boolean toggleState
) {
    public static void encode(
        RacialAbilityPacket packet,
        FriendlyByteBuf buffer
    ) {
        buffer.writeBoolean(packet.toggleState);
    }

    public static RacialAbilityPacket decode(
        FriendlyByteBuf buffer
    ) {
        return new RacialAbilityPacket(
            buffer.readBoolean()
        );
    }

    public static void handle(
        RacialAbilityPacket packet,
        Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();

        if (player == null) {
            context.setPacketHandled(true);
            return;
        }

        Race race = RaceManager.getRace(player).orElse(null);
        if (race == null) {
            context.setPacketHandled(true);
            return;
        }

        switch (race) {
            case DRAGONBORN -> {
                if (!IronSpellsCompat.isLoaded()) {
                    player.displayClientMessage(
                        Component.literal(
                            "Fire Breath requires Iron's Spells 'n Spellbooks."
                        ),
                        true
                    );
                } else {
                    DragonBreathAbility.tryCast(player);
                }
            }

            case FAIRY ->
                FairyHoverAbility.setEnabled(
                    player,
                    packet.toggleState()
                );

            case DOGFOLK ->
                DogfolkScentAbility.tryActivate(player);

            case FOXFOLK ->
                FoxfolkQuickstepAbility.tryActivate(player);

            case ORC ->
                OrcWarCryAbility.tryActivate(player);

            case GOBLIN ->
                GoblinScavengerSenseAbility.tryActivate(player);

            case NYMPH ->
                NymphNatureGraceAbility.tryActivate(player);

            case AASIMAR -> {
                if (!IronSpellsCompat.isLoaded()) {
                    player.displayClientMessage(
                        Component.literal(
                            "Aasimar Radiance requires Iron's Spells 'n Spellbooks."
                        ),
                        true
                    );
                } else {
                    AasimarRadianceAbility.tryCast(player);
                }
            }

            case BIRDFOLK ->
                BirdfolkWingBurstAbility.tryActivate(player);

            default ->
                player.displayClientMessage(
                    Component.literal(
                        race.displayName()
                            + " does not have an active racial ability yet."
                    ),
                    true
                );
        }

        context.setPacketHandled(true);
    }
}
