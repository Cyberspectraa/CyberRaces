package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.CyberRaces;
import com.cyberspectraa.cyberraces.compat.FairyWingCompat;
import dev.cammiescorner.icarus.api.client.IcarusAPIClient;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CyberRaces.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
            IcarusAPIClient.addRenderPredicate(entity -> !FairyWingCompat.hasEquippedRacialWings(entity))
        );
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            try {
                LivingEntityRenderer<Player, EntityModel<Player>> renderer = event.getSkin(skin);
                if (renderer != null) {
                    renderer.addLayer(new FairyElytraLayer<>(renderer, event.getEntityModels()));
                }
            } catch (Exception exception) {
                CyberRaces.LOGGER.warn("Could not attach Fairy Elytra compatibility layer to skin {}", skin, exception);
            }
        }
    }
}
