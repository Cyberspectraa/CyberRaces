package com.cyberspectraa.cyberraces.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.cammiescorner.icarus.client.models.ZanzasWingsModel;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

/**
 * Uses vanilla Elytra rotations for FA+Player compatibility while preserving
 * the Zanza model's own anchor and geometry.
 */
public final class FairyZanzaElytraModel<T extends LivingEntity> extends ElytraModel<T> {
    private final ModelPart animatedLeftWing;
    private final ModelPart animatedRightWing;
    private final ZanzasWingsModel<T> zanzaModel;

    public FairyZanzaElytraModel(ModelPart elytraRoot, ModelPart zanzaRoot) {
        super(elytraRoot);

        this.animatedLeftWing = elytraRoot.getChild("left_wing");
        this.animatedRightWing = elytraRoot.getChild("right_wing");
        this.zanzaModel = new ZanzasWingsModel<>(zanzaRoot);
    }

    @Override
    public void setupAnim(
        T entity,
        float limbSwing,
        float limbSwingAmount,
        float ageInTicks,
        float netHeadYaw,
        float headPitch
    ) {
        // Let Icarus establish the correct Zanza anchor first.
        zanzaModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // Then use Elytra's rotations so animation replacers that understand
        // vanilla Elytra continue to drive the wings.
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        copyRotation(animatedLeftWing, zanzaModel.leftWing);
        copyRotation(animatedRightWing, zanzaModel.rightWing);
    }

    @Override
    public void renderToBuffer(
        PoseStack poseStack,
        VertexConsumer vertexConsumer,
        int packedLight,
        int packedOverlay,
        float red,
        float green,
        float blue,
        float alpha
    ) {
        zanzaModel.renderToBuffer(
            poseStack,
            vertexConsumer,
            packedLight,
            packedOverlay,
            red,
            green,
            blue,
            alpha
        );
    }

    private static void copyRotation(ModelPart source, ModelPart target) {
        target.xRot = source.xRot;
        target.yRot = source.yRot;
        target.zRot = source.zRot;
    }
}
