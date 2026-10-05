package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.CyberRaces;
import com.cyberspectraa.cyberraces.network.CyberRacesNetwork;
import com.cyberspectraa.cyberraces.network.packet.DragonBreathPacket;
import com.cyberspectraa.cyberraces.event.FairyHoverPhysics;
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
            return;
        }

        // Use the stored race rather than preview state so selecting Fairy in
        // the creator does not physically move an unfinished character.
        boolean fairy =
            ClientCharacterState.resolve(minecraft.player.getUUID())
                .map(visual -> visual.race() == Race.FAIRY)
                .orElse(false);

        if (fairy) {
            FairyHoverPhysics.apply(minecraft.player);
        }

        if (minecraft.screen != null) {
            return;
        }

        while (ClientKeyMappings.DRAGON_BREATH.consumeClick()) {
            boolean dragonborn =
                ClientCharacterState.resolve(minecraft.player)
                    .map(visual -> visual.race() == Race.DRAGONBORN)
                    .orElse(false);

            if (!dragonborn) {
                minecraft.player.displayClientMessage(
                    Component.literal(
                        "Fire Breath is a Dragonborn racial ability."
                    ),
                    true
                );
                continue;
            }

            CyberRacesNetwork.sendToServer(
                new DragonBreathPacket()
            );
        }
    }
}
