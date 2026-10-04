package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.CyberRaces;
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
    private static final ResourceLocation FEATURE_TEXTURE =
        new ResourceLocation(CyberRaces.MOD_ID, "textures/entity/feature.png");
    private static final ResourceLocation TUSK_TEXTURE =
        new ResourceLocation(CyberRaces.MOD_ID, "textures/entity/tusk.png");
    private static final ResourceLocation HORN_TEXTURE =
        new ResourceLocation(CyberRaces.MOD_ID, "textures/entity/horn.png");
    private static final ResourceLocation TIEFLING_TEXTURE =
        new ResourceLocation(CyberRaces.MOD_ID, "textures/entity/tiefling.png");
    private static final ResourceLocation DRAGON_TEXTURE =
        new ResourceLocation(CyberRaces.MOD_ID, "textures/entity/dragon.png");

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
                visual.featureColor(),
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
        int featureColor,
        float ageInTicks
    ) {
        switch (race) {
            case ELF -> renderEars(poseStack, buffer, packedLight, player, race, featureColor, "elf_" + switch (feature) {
                case 1 -> "long";
                case 2 -> "high";
                default -> "short";
            });

            case HALFLING -> renderEars(poseStack, buffer, packedLight, player, race, featureColor, "halfling_" + switch (feature) {
                case 1 -> "soft";
                case 2 -> "pointed";
                default -> "round";
            });

            case ORC -> {
                renderEars(poseStack, buffer, packedLight, player, race, featureColor, "orc_ears");
                renderHeadPair(poseStack, buffer, packedLight, TUSK_TEXTURE, "tusks_" + switch (feature) {
                    case 1 -> "broad";
                    case 2 -> "long";
                    default -> "small";
                }, 1.0F, 1.0F, 1.0F);
            }

            case GOBLIN -> renderEars(poseStack, buffer, packedLight, player, race, featureColor, "goblin_" + switch (feature) {
                case 1 -> "long";
                case 2 -> "swept";
                default -> "wide";
            });

            case TIEFLING -> {
                renderHeadPair(poseStack, buffer, packedLight, HORN_TEXTURE, "horns_" + switch (feature) {
                    case 1 -> "swept";
                    case 2 -> "tall";
                    default -> "curved";
                }, 1.0F, 1.0F, 1.0F);

                float[] color = FeatureColourPalette.rgb(race, featureColor);
                renderBodyPart(
                    poseStack, buffer, packedLight, TIEFLING_TEXTURE, "tiefling_tail",
                    ageInTicks, 0.15F, color[0], color[1], color[2]
                );
            }

            case DRAGONBORN -> {
                float[] color = FeatureColourPalette.rgb(race, featureColor);

                renderHeadPair(
                    poseStack, buffer, packedLight, DRAGON_TEXTURE, "dragon_" + switch (feature) {
                        case 1 -> "crowned";
                        case 2 -> "swept";
                        default -> "horned";
                    },
                    color[0], color[1], color[2]
                );

                renderBodyPart(
                    poseStack, buffer, packedLight, DRAGON_TEXTURE, "dragon_tail",
                    ageInTicks, 0.10F, color[0], color[1], color[2]
                );
            }

            case FAIRY -> renderEars(poseStack, buffer, packedLight, player, race, featureColor, "fairy_" + switch (feature) {
                case 1 -> "sharp";
                case 2 -> "soft";
                default -> "classic";
            });

            case HUMAN, DWARF -> {
            }
        }
    }

    private void renderEars(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        AbstractClientPlayer player,
        Race race,
        int featureColor,
        String baseName
    ) {
        poseStack.pushPose();
        getParentModel().head.translateAndRotate(poseStack);

        ResourceLocation texture;
        float r = 1.0F;
        float g = 1.0F;
        float b = 1.0F;

        if (FeatureColourPalette.usesPlayerSkin(race, featureColor)) {
            texture = player.getSkinTextureLocation();
        } else {
            texture = FEATURE_TEXTURE;
            float[] color = FeatureColourPalette.rgb(race, featureColor);
            r = color[0];
            g = color[1];
            b = color[2];
        }

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));

        skinRoot.getChild(baseName + "_left").render(
            poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F
        );
        skinRoot.getChild(baseName + "_right").render(
            poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F
        );

        poseStack.popPose();
    }

    private void renderHeadPair(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        ResourceLocation texture,
        String baseName,
        float r,
        float g,
        float b
    ) {
        poseStack.pushPose();
        getParentModel().head.translateAndRotate(poseStack);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        hardRoot.getChild(baseName + "_left").render(
            poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F
        );
        hardRoot.getChild(baseName + "_right").render(
            poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F
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
        float swayAmount,
        float r,
        float g,
        float b
    ) {
        ModelPart part = hardRoot.getChild(name);
        float previousYRot = part.yRot;
        float previousZRot = part.zRot;

        part.yRot = previousYRot + (float) Math.sin(ageInTicks * 0.10F) * swayAmount;
        part.zRot = previousZRot + (float) Math.sin(ageInTicks * 0.075F) * swayAmount * 0.35F;

        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        part.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);

        poseStack.popPose();

        part.yRot = previousYRot;
        part.zRot = previousZRot;
    }
}
