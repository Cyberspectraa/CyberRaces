package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.compat.FairyWingCompat;
import com.cyberspectraa.cyberraces.race.Race;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.cammiescorner.icarus.client.IcarusModels;
import dev.cammiescorner.icarus.client.models.ZanzasWingsModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
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

    private final ZanzasWingsModel<T> wingModel;

    public FairyElytraLayer(RenderLayerParent<T, M> parent, EntityModelSet modelSet) {
        super(parent);
        this.wingModel = new ZanzasWingsModel<>(
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
        boolean previewFairy = entity instanceof AbstractClientPlayer player
            && ClientCharacterState.isPreviewing(Race.FAIRY, player);

        if (racialWings.isEmpty() && !previewFairy) {
            return;
        }

        poseStack.pushPose();

        /*
         * FA+Player's elytra model explicitly follows the animated body
         * rx/ry/rz and tx/ty/tz values. Attaching our Zanza model to the
         * actual humanoid body ModelPart gives it the same behaviour and
         * also works with other animation packs that move the torso.
         */
        if (getParentModel() instanceof HumanoidModel<?> humanoidModel) {
            humanoidModel.body.translateAndRotate(poseStack);
        }

        // Zanza's root is naturally about five model pixels low. Pull it up
        // onto the shoulder-blade area after inheriting the torso transform.
        poseStack.translate(0.0D, -0.22D, 0.125D);

        getParentModel().copyPropertiesTo(wingModel);
        wingModel.setupAnim(
            entity,
            limbSwing,
            limbSwingAmount,
            ageInTicks,
            netHeadYaw,
            headPitch
        );

        boolean foil = !racialWings.isEmpty() && racialWings.hasFoil();
        renderPass(poseStack, buffer, packedLight, foil, ZANZA_LAYER_2);
        renderPass(poseStack, buffer, packedLight, foil, ZANZA_LAYER_1);

        poseStack.popPose();
    }

    private void renderPass(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        boolean foil,
        ResourceLocation texture
    ) {
        VertexConsumer vertexConsumer = ItemRenderer.getArmorFoilBuffer(
            buffer,
            RenderType.entityTranslucent(texture),
            false,
            foil
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
