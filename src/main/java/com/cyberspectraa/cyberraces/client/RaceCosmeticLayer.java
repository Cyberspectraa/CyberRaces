package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.race.Race;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class RaceCosmeticLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation BONE_TEXTURE =
        new ResourceLocation("minecraft", "textures/block/bone_block_side.png");
    private static final ResourceLocation HORN_TEXTURE =
        new ResourceLocation("minecraft", "textures/block/blackstone.png");
    private static final ResourceLocation TIEFLING_TAIL_TEXTURE =
        new ResourceLocation("minecraft", "textures/block/crimson_stem.png");
    private static final ResourceLocation DRAGON_TEXTURE =
        new ResourceLocation("minecraft", "textures/block/deepslate_tiles.png");

    private final ModelPart skinRoot;
    private final ModelPart hardRoot;

    public RaceCosmeticLayer(
        RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
        ModelPart skinRoot,
        ModelPart hardRoot
    ) {
        super(parent);
        this.skinRoot = skinRoot;
        this.hardRoot = hardRoot;
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
        ClientCharacterState.resolve(player).ifPresent(visual ->
            renderRace(
                poseStack,
                buffer,
                packedLight,
                player,
                visual.race(),
                visual.featureStyle(),
                ageInTicks
            )
        );
    }

    private void renderRace(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        AbstractClientPlayer player,
        Race race,
        int feature,
        float ageInTicks
    ) {
        switch (race) {
            case ELF -> renderSkinEars(poseStack, buffer, packedLight, player, "elf_" + switch (feature) {
                case 1 -> "long";
                case 2 -> "high";
                default -> "short";
            });

            case HALFLING -> renderSkinEars(poseStack, buffer, packedLight, player, "halfling_" + switch (feature) {
                case 1 -> "soft";
                case 2 -> "pointed";
                default -> "round";
            });

            case ORC -> {
                renderSkinEars(poseStack, buffer, packedLight, player, "orc_ears");
                renderHeadPair(poseStack, buffer, packedLight, BONE_TEXTURE, "tusks_" + switch (feature) {
                    case 1 -> "broad";
                    case 2 -> "long";
                    default -> "small";
                });
            }

            case GOBLIN -> renderSkinEars(poseStack, buffer, packedLight, player, "goblin_" + switch (feature) {
                case 1 -> "long";
                case 2 -> "swept";
                default -> "wide";
            });

            case TIEFLING -> {
                renderHeadPair(poseStack, buffer, packedLight, HORN_TEXTURE, "horns_" + switch (feature) {
                    case 1 -> "swept";
                    case 2 -> "tall";
                    default -> "curved";
                });
                renderBodyPart(poseStack, buffer, packedLight, TIEFLING_TAIL_TEXTURE, "tiefling_tail", ageInTicks, 0.10F);
            }

            case DRAGONBORN -> {
                renderHeadPart(poseStack, buffer, packedLight, DRAGON_TEXTURE, "dragon_snout");
                renderHeadPair(poseStack, buffer, packedLight, DRAGON_TEXTURE, "dragon_" + switch (feature) {
                    case 1 -> "crowned";
                    case 2 -> "swept";
                    default -> "horned";
                });
                renderBodyPart(poseStack, buffer, packedLight, DRAGON_TEXTURE, "dragon_tail", ageInTicks, 0.07F);
            }

            case FAIRY -> renderSkinEars(poseStack, buffer, packedLight, player, "fairy_" + switch (feature) {
                case 1 -> "sharp";
                case 2 -> "soft";
                default -> "classic";
            });

            case HUMAN, DWARF -> {
            }
        }
    }

    private void renderSkinEars(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        AbstractClientPlayer player,
        String baseName
    ) {
        poseStack.pushPose();
        getParentModel().head.translateAndRotate(poseStack);

        VertexConsumer consumer = buffer.getBuffer(
            RenderType.entityCutoutNoCull(player.getSkinTextureLocation())
        );

        skinRoot.getChild(baseName + "_left").render(
            poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY
        );
        skinRoot.getChild(baseName + "_right").render(
            poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }

    private void renderHeadPair(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        ResourceLocation texture,
        String baseName
    ) {
        poseStack.pushPose();
        getParentModel().head.translateAndRotate(poseStack);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        hardRoot.getChild(baseName + "_left").render(
            poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY
        );
        hardRoot.getChild(baseName + "_right").render(
            poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }

    private void renderHeadPart(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        ResourceLocation texture,
        String name
    ) {
        poseStack.pushPose();
        getParentModel().head.translateAndRotate(poseStack);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        hardRoot.getChild(name).render(
            poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }

    private void renderBodyPart(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        ResourceLocation texture,
        String name,
        float ageInTicks,
        float swayAmount
    ) {
        ModelPart part = hardRoot.getChild(name);
        float previousYRot = part.yRot;
        part.yRot = previousYRot + (float) Math.sin(ageInTicks * 0.12F) * swayAmount;

        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        part.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
        part.yRot = previousYRot;
    }
}
