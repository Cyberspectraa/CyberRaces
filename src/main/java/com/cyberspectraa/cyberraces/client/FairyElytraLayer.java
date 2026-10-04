package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.compat.FairyWingCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.cammiescorner.icarus.client.IcarusModels;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class FairyElytraLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation ZANZA_LAYER_1 =
        new ResourceLocation("icarus", "textures/entity/zanzas_wings.png");

    private static final ResourceLocation ZANZA_LAYER_2 =
        new ResourceLocation("icarus", "textures/entity/zanzas_wings_2.png");

    private final FairyZanzaElytraModel<T> wingModel;

    public FairyElytraLayer(RenderLayerParent<T, M> parent, EntityModelSet modelSet) {
        super(parent);
        this.wingModel = new FairyZanzaElytraModel<>(
            modelSet.bakeLayer(ModelLayers.ELYTRA),
            modelSet.bakeLayer(IcarusModels.ZANZA)
        );
    }

    @Override
    public void render(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        T entity,
        float limbSwing,
        float limbSwingAmount,
        float partialTick,
        float ageInTicks,
        float netHeadYaw,
        float headPitch
    ) {
        ItemStack racialWings = FairyWingCompat.getEquippedRacialWings(entity);
        if (racialWings.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.125D);

        getParentModel().copyPropertiesTo(wingModel);
        wingModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        renderPass(
            poseStack,
            buffer,
            packedLight,
            racialWings,
            ZANZA_LAYER_2
        );

        renderPass(
            poseStack,
            buffer,
            packedLight,
            racialWings,
            ZANZA_LAYER_1
        );

        poseStack.popPose();
    }

    private void renderPass(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        ItemStack racialWings,
        ResourceLocation texture
    ) {
        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(
            buffer,
            RenderType.entityTranslucent(texture),
            false,
            racialWings.hasFoil()
        );

        wingModel.renderToBuffer(
            poseStack,
            vertexConsumer,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            1.0F,
            1.0F,
            1.0F,
            1.0F
        );
    }
}
