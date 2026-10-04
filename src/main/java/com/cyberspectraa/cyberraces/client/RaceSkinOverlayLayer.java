package com.cyberspectraa.cyberraces.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class RaceSkinOverlayLayer
    extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public RaceSkinOverlayLayer(
        RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent
    ) {
        super(parent);
    }

    @Override
    public void render(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        AbstractClientPlayer player,
        float limbSwing,
        float limbSwingAmount,
        float partialTick,
        float ageInTicks,
        float netHeadYaw,
        float headPitch
    ) {
        ClientCharacterState.resolve(player).ifPresent(visual -> {
            ResourceLocation texture = RaceSkinOverlayManager.get(
                player,
                visual.race(),
                visual.appearance()
            );

            if (texture == null) {
                return;
            }

            VertexConsumer consumer = buffer.getBuffer(
                RenderType.entityTranslucent(texture)
            );

            getParentModel().renderToBuffer(
                poseStack,
                consumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                1.0F,
                1.0F,
                1.0F,
                1.0F
            );
        });
    }
}
