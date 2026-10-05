package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.CyberRaces;
import dev.cammiescorner.icarus.api.client.IcarusAPIClient;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CyberRaces.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() ->
            // CyberRaces renders all Icarus wings on players so they inherit
            // the animated FA+Player torso transform. Keep Icarus's normal
            // renderer for any non-player living entities.
            IcarusAPIClient.addRenderPredicate(
                entity -> !(entity instanceof AbstractClientPlayer)
            )
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
        event.registerLayerDefinition(
            BeastfolkModels.CAT,
            BeastfolkModels::createCatLayer
        );
        event.registerLayerDefinition(
            BeastfolkModels.WOLF,
            BeastfolkModels::createWolfLayer
        );
        event.registerLayerDefinition(
            BeastfolkModels.FOX,
            BeastfolkModels::createFoxLayer
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

                renderer.addLayer(new RaceCosmeticLayer(
                    renderer,
                    event.getEntityModels().bakeLayer(RaceCosmeticModels.SKIN_FEATURES),
                    event.getEntityModels().bakeLayer(RaceCosmeticModels.HARD_FEATURES),
                    event.getEntityModels().bakeLayer(BeastfolkModels.CAT),
                    event.getEntityModels().bakeLayer(BeastfolkModels.WOLF),
                    event.getEntityModels().bakeLayer(BeastfolkModels.FOX)
                ));

                renderer.addLayer(new IcarusPlayerWingLayer<>(renderer, event.getEntityModels()));
            } catch (Exception exception) {
                CyberRaces.LOGGER.warn("Could not attach CyberRaces player layers to skin {}", skin, exception);
            }
        }

        attachCyberNpcLayers(event);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void attachCyberNpcLayers(
        EntityRenderersEvent.AddLayers event
    ) {
        attachCyberNpcRenderer(
            event,
            new ResourceLocation("cybernpc", "cyber_npc"),
            "Wild NPC"
        );

        attachCyberNpcRenderer(
            event,
            new ResourceLocation("cybernpc", "zombie_cyber_npc"),
            "Zombie NPC"
        );
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void attachCyberNpcRenderer(
        EntityRenderersEvent.AddLayers event,
        ResourceLocation entityId,
        String label
    ) {
        EntityType<?> entityType =
            ForgeRegistries.ENTITY_TYPES.getValue(entityId);

        if (entityType == null) {
            return;
        }

        try {
            LivingEntityRenderer renderer =
                event.getRenderer((EntityType) entityType);

            if (renderer == null
                || !(renderer.getModel() instanceof HumanoidModel<?>)) {
                return;
            }

            renderer.addLayer(new RaceCosmeticLayer(
                renderer,
                event.getEntityModels().bakeLayer(
                    RaceCosmeticModels.SKIN_FEATURES
                ),
                event.getEntityModels().bakeLayer(
                    RaceCosmeticModels.HARD_FEATURES
                ),
                event.getEntityModels().bakeLayer(
                    BeastfolkModels.CAT
                ),
                event.getEntityModels().bakeLayer(
                    BeastfolkModels.WOLF
                ),
                event.getEntityModels().bakeLayer(
                    BeastfolkModels.FOX
                )
            ));

            /*
             * This also lets converted Fairy NPCs keep their racial wings.
             * IcarusPlayerWingLayer already resolves non-player Fairy races
             * through ClientCharacterState.
             */
            renderer.addLayer(
                new IcarusPlayerWingLayer(
                    renderer,
                    event.getEntityModels()
                )
            );

            CyberRaces.LOGGER.info(
                "Attached CyberRaces visuals to CyberNpc {} renderer",
                label
            );
        } catch (Exception exception) {
            CyberRaces.LOGGER.warn(
                "Could not attach CyberRaces layers to CyberNpc {}",
                label,
                exception
            );
        }
    }

}