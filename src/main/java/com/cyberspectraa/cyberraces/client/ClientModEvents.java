package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.CyberRaces;
import com.cyberspectraa.cyberraces.compat.FairyWingCompat;
import dev.cammiescorner.icarus.api.client.IcarusAPIClient;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
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
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
            RaceCosmeticModels.SKIN_FEATURES,
            RaceCosmeticModels::createSkinLayer
        );
        event.registerLayerDefinition(
            RaceCosmeticModels.HARD_FEATURES,
            RaceCosmeticModels::createHardLayer
        );
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            try {
                PlayerRenderer renderer = event.getSkin(skin);
                if (renderer == null) {
                    continue;
                }

                renderer.addLayer(new RaceSkinOverlayLayer(renderer));

                renderer.addLayer(new RaceCosmeticLayer(
                    renderer,
                    event.getEntityModels().bakeLayer(RaceCosmeticModels.SKIN_FEATURES),
                    event.getEntityModels().bakeLayer(RaceCosmeticModels.HARD_FEATURES)
                ));

                renderer.addLayer(new FairyElytraLayer<>(renderer, event.getEntityModels()));
            } catch (Exception exception) {
                CyberRaces.LOGGER.warn("Could not attach CyberRaces player layers to skin {}", skin, exception);
            }
        }
    }
}
