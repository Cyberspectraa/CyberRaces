package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.CyberRaces;
import com.cyberspectraa.cyberraces.event.FairyHoverPhysics;
import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.RacialAbilityPacket;
import com.cyberspectraa.cyberraces.race.Race;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
    modid = CyberRaces.MOD_ID,
    bus = Mod.EventBusSubscriber.Bus.FORGE,
    value = Dist.CLIENT
)
public final class ClientForgeEvents {
    private ClientForgeEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            ClientAbilityState.disableFairyHover();
            return;
        }

        Race race =
            ClientCharacterState.resolve(minecraft.player.getUUID())
                .map(ClientCharacterState.VisualCharacter::race)
                .orElse(null);

        if (race != Race.FAIRY) {
            ClientAbilityState.disableFairyHover();
        } else if (ClientAbilityState.isFairyHoverEnabled()) {
            FairyHoverPhysics.apply(minecraft.player);
        }

        if (minecraft.screen != null) {
            return;
        }

        while (ClientKeyMappings.RACIAL_ABILITY.consumeClick()) {
            if (race == null) {
                minecraft.player.displayClientMessage(
                    Component.literal(
                        "Choose a race before using a racial ability."
                    ),
                    true
                );
                continue;
            }

            boolean toggleState = false;

            if (race == Race.FAIRY) {
                toggleState =
                    ClientAbilityState.toggleFairyHover();
            }

            CyberRacesNetwork.sendToServer(
                new RacialAbilityPacket(toggleState)
            );
        }
    }
}
