package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.CyberRaces;
import com.cyberspectraa.cyberraces.character.CharacterAppearance;
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
    private static final ResourceLocation HORN_TEXTURE =
        new ResourceLocation(CyberRaces.MOD_ID, "textures/entity/horn.png");
    private static final ResourceLocation TIEFLING_TEXTURE =
        new ResourceLocation(CyberRaces.MOD_ID, "textures/entity/tiefling.png");
    private static final ResourceLocation DRAGON_TEXTURE =
        new ResourceLocation(CyberRaces.MOD_ID, "textures/entity/dragon.png");

    private final ModelPart skinRoot;
    private final ModelPart hardRoot;
    private final ModelPart catRoot;
    private final ModelPart wolfRoot;
    private final ModelPart foxRoot;

    public RaceCosmeticLayer(
        RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
        ModelPart skinRoot,
        ModelPart hardRoot,
        ModelPart catRoot,
        ModelPart wolfRoot,
        ModelPart foxRoot
    ) {
        super(parent);
        this.skinRoot = skinRoot;
        this.hardRoot = hardRoot;
        this.catRoot = catRoot;
        this.wolfRoot = wolfRoot;
        this.foxRoot = foxRoot;
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
                visual.earHeight(),
                visual.earSpread(),
                visual.earTilt(),
                visual.appearance().bodyTargetColor(),
                limbSwing,
                limbSwingAmount,
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
        int earHeight,
        int earSpread,
        int earTilt,
        int bodyTargetColor,
        float limbSwing,
        float limbSwingAmount,
        float ageInTicks
    ) {
        switch (race) {
            case ELF -> renderEars(
                poseStack, buffer, packedLight, player, race, featureColor,
                "elf_" + switch (feature) {
                    case 1 -> "long";
                    case 2 -> "high";
                    default -> "short";
                },
                earHeight, earSpread, earTilt
            );

            case HALFLING -> renderEars(
                poseStack, buffer, packedLight, player, race, featureColor,
                "halfling_" + switch (feature) {
                    case 1 -> "soft";
                    case 2 -> "pointed";
                    default -> "round";
                },
                earHeight, earSpread, earTilt
            );

            case ORC -> renderEars(
                poseStack, buffer, packedLight, player, race, featureColor,
                "orc_" + switch (feature) {
                    case 1 -> "broad";
                    case 2 -> "swept";
                    default -> "short";
                },
                earHeight, earSpread, earTilt
            );

            case GOBLIN -> renderEars(
                poseStack, buffer, packedLight, player, race, featureColor,
                "goblin_" + switch (feature) {
                    case 1 -> "long";
                    case 2 -> "swept";
                    default -> "wide";
                },
                earHeight, earSpread, earTilt
            );

            case TIEFLING -> {
                renderHeadPair(
                    poseStack, buffer, packedLight, HORN_TEXTURE,
                    "horns_" + switch (feature) {
                        case 1 -> "swept";
                        case 2 -> "tall";
                        default -> "curved";
                    },
                    1.0F, 1.0F, 1.0F
                );

                float[] color = FeatureColourPalette.rgb(race, featureColor);
                renderTail(
                    poseStack,
                    buffer,
                    packedLight,
                    TIEFLING_TEXTURE,
                    "tiefling_tail",
                    TailStyle.TIEFLING,
                    limbSwing,
                    limbSwingAmount,
                    ageInTicks,
                    color[0],
                    color[1],
                    color[2]
                );
            }

            case DRAGONBORN -> {
                float[] color = FeatureColourPalette.rgb(race, featureColor);

                renderHeadPair(
                    poseStack, buffer, packedLight, DRAGON_TEXTURE,
                    "dragon_" + switch (feature) {
                        case 1 -> "crowned";
                        case 2 -> "swept";
                        default -> "horned";
                    },
                    color[0], color[1], color[2]
                );

                renderTail(
                    poseStack,
                    buffer,
                    packedLight,
                    DRAGON_TEXTURE,
                    "dragon_tail",
                    TailStyle.DRAGON,
                    limbSwing,
                    limbSwingAmount,
                    ageInTicks,
                    color[0],
                    color[1],
                    color[2]
                );
            }

            case FAIRY -> renderEars(
                poseStack, buffer, packedLight, player, race, featureColor,
                "fairy_" + switch (feature) {
                    case 1 -> "sharp";
                    case 2 -> "soft";
                    default -> "classic";
                },
                earHeight, earSpread, earTilt
            );

            case CATFOLK, DOGFOLK, FOXFOLK -> renderBeastfolk(
                poseStack,
                buffer,
                packedLight,
                race,
                feature,
                earHeight,
                earSpread,
                earTilt,
                limbSwing,
                limbSwingAmount,
                ageInTicks
            );

            case HUMAN, DWARF -> {
            }
        }
    }

    private void renderBeastfolk(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        Race race,
        int variant,
        int earHeight,
        int earSpread,
        int earTilt,
        float limbSwing,
        float limbSwingAmount,
        float ageInTicks
    ) {
        ModelPart root = switch (race) {
            case CATFOLK -> catRoot;
            case DOGFOLK -> wolfRoot;
            case FOXFOLK -> foxRoot;
            default -> throw new IllegalArgumentException("Not Beastfolk: " + race);
        };

        ResourceLocation texture = BeastVariantTextures.texture(race, variant);
        VertexConsumer consumer = buffer.getBuffer(
            RenderType.entityCutoutNoCull(texture)
        );

        renderBeastEars(
            poseStack,
            consumer,
            packedLight,
            root,
            earHeight,
            earSpread,
            earTilt
        );

        renderBeastTail(
            poseStack,
            consumer,
            packedLight,
            root,
            race,
            limbSwing,
            limbSwingAmount,
            ageInTicks
        );
    }

    private void renderBeastEars(
        PoseStack poseStack,
        VertexConsumer consumer,
        int packedLight,
        ModelPart root,
        int earHeight,
        int earSpread,
        int earTilt
    ) {
        ModelPart left = root.getChild("left_ear");
        ModelPart right = root.getChild("right_ear");

        float leftX = left.x;
        float rightX = right.x;
        float leftY = left.y;
        float rightY = right.y;
        float leftRoll = left.zRot;
        float rightRoll = right.zRot;

        float heightOffset = earHeight * 0.25F;
        float spreadOffset = earSpread * 0.22F;
        float tiltRadians = (float) Math.toRadians(earTilt * 3.0F);

        left.y -= heightOffset;
        right.y -= heightOffset;
        left.x += spreadOffset;
        right.x -= spreadOffset;
        left.zRot -= tiltRadians;
        right.zRot += tiltRadians;

        poseStack.pushPose();
        getParentModel().head.translateAndRotate(poseStack);

        left.render(
            poseStack,
            consumer,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            1.0F,
            1.0F,
            1.0F,
            1.0F
        );
        right.render(
            poseStack,
            consumer,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            1.0F,
            1.0F,
            1.0F,
            1.0F
        );

        poseStack.popPose();

        left.x = leftX;
        right.x = rightX;
        left.y = leftY;
        right.y = rightY;
        left.zRot = leftRoll;
        right.zRot = rightRoll;
    }

    private void renderBeastTail(
        PoseStack poseStack,
        VertexConsumer consumer,
        int packedLight,
        ModelPart root,
        Race race,
        float limbSwing,
        float limbSwingAmount,
        float ageInTicks
    ) {
        ModelPart tail = root.getChild("tail");
        ModelPart[] chain = beastTailChain(tail, race);

        float[] oldX = new float[chain.length];
        float[] oldY = new float[chain.length];
        float[] oldZ = new float[chain.length];

        for (int i = 0; i < chain.length; i++) {
            oldX[i] = chain[i].xRot;
            oldY[i] = chain[i].yRot;
            oldZ[i] = chain[i].zRot;
        }

        float movement = Math.max(0.0F, Math.min(1.0F, limbSwingAmount));

        float idleSpeed = switch (race) {
            case CATFOLK -> 0.085F;
            case DOGFOLK -> 0.075F;
            case FOXFOLK -> 0.060F;
            default -> 0.070F;
        };

        float idleAmount = switch (race) {
            case CATFOLK -> 0.040F;
            case DOGFOLK -> 0.025F;
            case FOXFOLK -> 0.025F;
            default -> 0.030F;
        };

        float walkAmount = switch (race) {
            case CATFOLK -> 0.065F;
            case DOGFOLK -> 0.145F;
            case FOXFOLK -> 0.055F;
            default -> 0.060F;
        };

        for (int i = 0; i < chain.length; i++) {
            float progress = chain.length <= 1
                ? 0.0F
                : i / (float) (chain.length - 1);

            float idle =
                (float) Math.sin(ageInTicks * idleSpeed - i * 0.34F)
                    * idleAmount
                    * (0.55F + progress * 0.65F);

            float walk =
                (float) Math.sin(limbSwing * 0.6662F - i * 0.40F)
                    * movement
                    * walkAmount
                    * (0.55F + progress * 0.70F);

            chain[i].yRot = oldY[i] + idle + walk;

            if (race == Race.CATFOLK) {
                // Cat tail gets a small travelling curl rather than a whip.
                chain[i].xRot =
                    oldX[i]
                        + (float) Math.cos(
                            ageInTicks * 0.045F - i * 0.22F
                        )
                        * 0.018F
                        * (0.4F + progress);
            } else if (race == Race.DOGFOLK) {
                // Keep humanoid Dogfolk wag restrained; previous 1.15 rad
                // swing was far too exaggerated.
                chain[i].xRot =
                    oldX[i]
                        + (float) Math.sin(ageInTicks * 0.040F)
                        * 0.010F;
            } else {
                chain[i].xRot =
                    oldX[i]
                        + (float) Math.cos(
                            ageInTicks * 0.035F - i * 0.18F
                        )
                        * 0.010F;
            }
        }

        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);

        tail.render(
            poseStack,
            consumer,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            1.0F,
            1.0F,
            1.0F,
            1.0F
        );

        poseStack.popPose();

        for (int i = 0; i < chain.length; i++) {
            chain[i].xRot = oldX[i];
            chain[i].yRot = oldY[i];
            chain[i].zRot = oldZ[i];
        }
    }

    private ModelPart[] beastTailChain(ModelPart tail, Race race) {
        return switch (race) {
            case CATFOLK, FOXFOLK -> {
                ModelPart mid1 = tail.getChild("mid1");
                ModelPart mid2 = mid1.getChild("mid2");
                ModelPart mid3 = mid2.getChild("mid3");
                ModelPart tip = mid3.getChild("tip");

                yield new ModelPart[] {
                    tail,
                    mid1,
                    mid2,
                    mid3,
                    tip
                };
            }

            case DOGFOLK -> {
                ModelPart mid1 = tail.getChild("mid1");
                ModelPart mid2 = mid1.getChild("mid2");
                ModelPart tip = mid2.getChild("tip");

                yield new ModelPart[] {
                    tail,
                    mid1,
                    mid2,
                    tip
                };
            }

            default -> new ModelPart[] {tail};
        };
    }

    private void renderEars(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        AbstractClientPlayer player,
        Race race,
        int featureColor,
        String baseName,
        int earHeight,
        int earSpread,
        int earTilt
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

        ModelPart left = skinRoot.getChild(baseName + "_left");
        ModelPart right = skinRoot.getChild(baseName + "_right");

        float leftX = left.x;
        float rightX = right.x;
        float leftY = left.y;
        float rightY = right.y;
        float leftRoll = left.zRot;
        float rightRoll = right.zRot;

        float heightOffset = earHeight * 0.25F;
        float spreadOffset = earSpread * 0.22F;
        float tiltRadians = (float) Math.toRadians(earTilt * 3.0F);

        // Positive Height moves the ears upward; positive Spread moves them outward.
        left.y -= heightOffset;
        right.y -= heightOffset;
        left.x += spreadOffset;
        right.x -= spreadOffset;

        // Positive Tilt makes the pair sweep upward symmetrically.
        left.zRot -= tiltRadians;
        right.zRot += tiltRadians;

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));

        left.render(
            poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F
        );
        right.render(
            poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F
        );

        left.x = leftX;
        right.x = rightX;
        left.y = leftY;
        right.y = rightY;
        left.zRot = leftRoll;
        right.zRot = rightRoll;

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

    private void renderTail(
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        ResourceLocation texture,
        String name,
        TailStyle style,
        float limbSwing,
        float limbSwingAmount,
        float ageInTicks,
        float r,
        float g,
        float b
    ) {
        ModelPart root = hardRoot.getChild(name);
        ModelPart[] chain = tailChain(root, style);

        float[] previousX = new float[chain.length];
        float[] previousY = new float[chain.length];
        float[] previousZ = new float[chain.length];

        for (int index = 0; index < chain.length; index++) {
            previousX[index] = chain[index].xRot;
            previousY[index] = chain[index].yRot;
            previousZ[index] = chain[index].zRot;
        }

        float movement = Math.max(0.0F, Math.min(1.0F, limbSwingAmount));
        float idlePhase = ageInTicks * tailIdleSpeed(style);
        float walkPhase = limbSwing * 0.6662F;
        float baseSway = tailSway(style);

        for (int index = 0; index < chain.length; index++) {
            float progress = chain.length <= 1
                ? 0.0F
                : index / (float) (chain.length - 1);

            // Each segment receives the motion slightly later than the one
            // before it. That produces a travelling bend instead of rotating
            // the entire tail like one stiff object.
            float idle =
                (float) Math.sin(idlePhase - index * 0.42F)
                    * baseSway
                    * (0.55F + progress * 0.75F);

            float walk =
                (float) Math.sin(walkPhase - index * 0.52F)
                    * baseSway
                    * movement
                    * (0.80F + progress * 0.90F);

            float lift =
                (float) Math.cos(idlePhase * 0.72F - index * 0.30F)
                    * baseSway
                    * 0.12F
                    * (0.45F + progress);

            float roll =
                (float) Math.sin(idlePhase * 0.58F + index * 0.36F)
                    * baseSway
                    * 0.10F
                    * progress;

            chain[index].yRot = previousY[index] + idle + walk;
            chain[index].xRot = previousX[index] + lift;
            chain[index].zRot = previousZ[index] + roll;
        }

        poseStack.pushPose();
        getParentModel().body.translateAndRotate(poseStack);

        VertexConsumer consumer = buffer.getBuffer(
            RenderType.entityCutoutNoCull(texture)
        );

        root.render(
            poseStack,
            consumer,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            r,
            g,
            b,
            1.0F
        );

        poseStack.popPose();

        for (int index = 0; index < chain.length; index++) {
            chain[index].xRot = previousX[index];
            chain[index].yRot = previousY[index];
            chain[index].zRot = previousZ[index];
        }
    }

    private ModelPart[] tailChain(ModelPart root, TailStyle style) {
        return switch (style) {
            case TIEFLING -> {
                ModelPart mid1 = root.getChild("mid1");
                ModelPart mid2 = mid1.getChild("mid2");
                ModelPart mid3 = mid2.getChild("mid3");
                ModelPart tipStem = mid3.getChild("tip_stem");

                yield new ModelPart[] {
                    root,
                    mid1,
                    mid2,
                    mid3,
                    tipStem
                };
            }

            case DRAGON -> {
                ModelPart mid1 = root.getChild("mid1");
                ModelPart mid2 = mid1.getChild("mid2");
                ModelPart mid3 = mid2.getChild("mid3");
                ModelPart lower = mid3.getChild("lower");
                ModelPart tip = lower.getChild("tip");

                yield new ModelPart[] {
                    root,
                    mid1,
                    mid2,
                    mid3,
                    lower,
                    tip
                };
            }

            case CAT, FOX -> {
                ModelPart mid1 = root.getChild("mid1");
                ModelPart mid2 = mid1.getChild("mid2");
                ModelPart mid3 = mid2.getChild("mid3");
                ModelPart tip = mid3.getChild("tip");

                yield new ModelPart[] {
                    root,
                    mid1,
                    mid2,
                    mid3,
                    tip
                };
            }

            case DOG -> {
                ModelPart mid1 = root.getChild("mid1");
                ModelPart mid2 = mid1.getChild("mid2");
                ModelPart tip = mid2.getChild("tip");

                yield new ModelPart[] {
                    root,
                    mid1,
                    mid2,
                    tip
                };
            }
        };
    }

    private float tailIdleSpeed(TailStyle style) {
        return switch (style) {
            case TIEFLING -> 0.085F;
            case DRAGON -> 0.070F;
            case CAT -> 0.105F;
            case DOG -> 0.120F;
            case FOX -> 0.078F;
        };
    }

    private float tailSway(TailStyle style) {
        return switch (style) {
            case TIEFLING -> 0.105F;
            case DRAGON -> 0.072F;
            case CAT -> 0.125F;
            case DOG -> 0.155F;
            case FOX -> 0.095F;
        };
    }

    private enum TailStyle {
        TIEFLING,
        DRAGON,
        CAT,
        DOG,
        FOX
    }

}