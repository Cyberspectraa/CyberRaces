package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.compat.FairyWingCompat;
import com.cyberspectraa.cyberraces.race.Race;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.cammiescorner.icarus.api.client.IcarusAPIClient;
import dev.cammiescorner.icarus.client.IcarusModels;
import dev.cammiescorner.icarus.client.models.DiscordsWingsModel;
import dev.cammiescorner.icarus.client.models.FeatheredWingsModel;
import dev.cammiescorner.icarus.client.models.FlandresWingsModel;
import dev.cammiescorner.icarus.client.models.LeatherWingsModel;
import dev.cammiescorner.icarus.client.models.LightWingsModel;
import dev.cammiescorner.icarus.client.models.WingEntityModel;
import dev.cammiescorner.icarus.client.models.ZanzasWingsModel;
import dev.cammiescorner.icarus.init.IcarusItems;
import dev.cammiescorner.icarus.item.WingItem;
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

public final class IcarusPlayerWingLayer<T extends LivingEntity, M extends EntityModel<T>>
    extends RenderLayer<T, M> {

    private final FeatheredWingsModel<T> featheredWings;
    private final LeatherWingsModel<T> leatherWings;
    private final LightWingsModel<T> lightWings;
    private final FlandresWingsModel<T> flandresWings;
    private final DiscordsWingsModel<T> discordsWings;
    private final ZanzasWingsModel<T> zanzasWings;

    public IcarusPlayerWingLayer(
        RenderLayerParent<T, M> parent,
        EntityModelSet modelSet
    ) {
        super(parent);
        this.featheredWings = new FeatheredWingsModel<>(
            modelSet.bakeLayer(IcarusModels.FEATHERED)
        );
        this.leatherWings = new LeatherWingsModel<>(
            modelSet.bakeLayer(IcarusModels.LEATHER)
        );
        this.lightWings = new LightWingsModel<>(
            modelSet.bakeLayer(IcarusModels.LIGHT)
        );
        this.flandresWings = new FlandresWingsModel<>(
            modelSet.bakeLayer(IcarusModels.FLANDRE)
        );
        this.discordsWings = new DiscordsWingsModel<>(
            modelSet.bakeLayer(IcarusModels.DISCORD)
        );
        this.zanzasWings = new ZanzasWingsModel<>(
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
        ItemStack wings = IcarusAPIClient.getWingsForRendering(entity);

        ClientCharacterState.VisualCharacter visual =
            entity instanceof AbstractClientPlayer player
                ? ClientCharacterState.resolve(player).orElse(null)
                : ClientCharacterState.resolve(entity.getUUID()).orElse(null);

        if (wings.isEmpty() && visual != null) {
            if (visual.race() == Race.FAIRY) {
                wings = new ItemStack(IcarusItems.ZANZAS_WINGS.get());
            } else if (visual.race() == Race.BIRDFOLK) {
                wings = new ItemStack(
                    birdfolkWing(visual.featureStyle())
                );
            } else if (visual.race() == Race.DRAGONBORN
                    && "skyborn".equals(visual.evolutionId())) {
                wings = new ItemStack(
                    dragonWing(visual.featureColor())
                );
            } else if (visual.race() == Race.AASIMAR) {
                wings = switch (visual.evolutionId()) {
                    case "seraphic" ->
                        new ItemStack(IcarusItems.WHITE_LIGHT_WINGS.get());
                    case "fallen" ->
                        new ItemStack(IcarusItems.BLACK_LIGHT_WINGS.get());
                    case "guardian" ->
                        new ItemStack(IcarusItems.YELLOW_LIGHT_WINGS.get());
                    default -> ItemStack.EMPTY;
                };
            }
        }

        if (!(wings.getItem() instanceof WingItem wingItem)) {
            return;
        }

        WingEntityModel<T> model = resolveModel(wings, wingItem);
        if (model == null) {
            return;
        }

        WingPlacement placement = placementFor(wings, wingItem);

        float[] primary = wingItem
            .getPrimaryColor(wings)
            .getTextureDiffuseColors();
        float[] secondary = wingItem
            .getSecondaryColor(wings)
            .getTextureDiffuseColors();

        ResourceLocation layer1 =
            wingItem.getWingType().getTextureLayer1(wings);
        ResourceLocation layer2 =
            wingItem.getWingType().getTextureLayer2(wings);

        poseStack.pushPose();

        /*
         * FA+Player moves the humanoid torso itself. Rendering the Icarus
         * wings beneath that animated body transform makes every wing type
         * follow crouching, leaning and other torso animations instead of
         * hovering in vanilla player space.
         */
        if (getParentModel() instanceof HumanoidModel<?> humanoidModel) {
            humanoidModel.body.translateAndRotate(poseStack);
        }

        poseStack.translate(
            placement.x(),
            placement.y(),
            placement.z()
        );

        getParentModel().copyPropertiesTo(model);
        model.setupAnim(
            entity,
            limbSwing,
            limbSwingAmount,
            ageInTicks,
            netHeadYaw,
            headPitch
        );

        renderPass(
            model,
            poseStack,
            buffer,
            packedLight,
            wings,
            layer2,
            secondary[0],
            secondary[1],
            secondary[2]
        );

        renderPass(
            model,
            poseStack,
            buffer,
            packedLight,
            wings,
            layer1,
            primary[0],
            primary[1],
            primary[2]
        );

        poseStack.popPose();
    }

    private net.minecraft.world.item.Item birdfolkWing(int style) {
        return switch (Math.floorMod(style, 16)) {
            case 0 -> IcarusItems.WHITE_FEATHERED_WINGS.get();
            case 1 -> IcarusItems.ORANGE_FEATHERED_WINGS.get();
            case 2 -> IcarusItems.MAGENTA_FEATHERED_WINGS.get();
            case 3 -> IcarusItems.LIGHT_BLUE_FEATHERED_WINGS.get();
            case 4 -> IcarusItems.YELLOW_FEATHERED_WINGS.get();
            case 5 -> IcarusItems.LIME_FEATHERED_WINGS.get();
            case 6 -> IcarusItems.PINK_FEATHERED_WINGS.get();
            case 7 -> IcarusItems.GRAY_FEATHERED_WINGS.get();
            case 8 -> IcarusItems.LIGHT_GRAY_FEATHERED_WINGS.get();
            case 9 -> IcarusItems.CYAN_FEATHERED_WINGS.get();
            case 10 -> IcarusItems.PURPLE_FEATHERED_WINGS.get();
            case 11 -> IcarusItems.BLUE_FEATHERED_WINGS.get();
            case 12 -> IcarusItems.BROWN_FEATHERED_WINGS.get();
            case 13 -> IcarusItems.GREEN_FEATHERED_WINGS.get();
            case 14 -> IcarusItems.RED_FEATHERED_WINGS.get();
            default -> IcarusItems.BLACK_FEATHERED_WINGS.get();
        };
    }

    private net.minecraft.world.item.Item dragonWing(int packedRgb) {
        int index = nearestDye(
            packedRgb < 0 ? 0x76AFA1 : packedRgb
        );

        return switch (index) {
            case 0 -> IcarusItems.WHITE_DRAGON_WINGS.get();
            case 1 -> IcarusItems.ORANGE_DRAGON_WINGS.get();
            case 2 -> IcarusItems.MAGENTA_DRAGON_WINGS.get();
            case 3 -> IcarusItems.LIGHT_BLUE_DRAGON_WINGS.get();
            case 4 -> IcarusItems.YELLOW_DRAGON_WINGS.get();
            case 5 -> IcarusItems.LIME_DRAGON_WINGS.get();
            case 6 -> IcarusItems.PINK_DRAGON_WINGS.get();
            case 7 -> IcarusItems.GRAY_DRAGON_WINGS.get();
            case 8 -> IcarusItems.LIGHT_GRAY_DRAGON_WINGS.get();
            case 9 -> IcarusItems.CYAN_DRAGON_WINGS.get();
            case 10 -> IcarusItems.PURPLE_DRAGON_WINGS.get();
            case 11 -> IcarusItems.BLUE_DRAGON_WINGS.get();
            case 12 -> IcarusItems.BROWN_DRAGON_WINGS.get();
            case 13 -> IcarusItems.GREEN_DRAGON_WINGS.get();
            case 14 -> IcarusItems.RED_DRAGON_WINGS.get();
            default -> IcarusItems.BLACK_DRAGON_WINGS.get();
        };
    }

    private int nearestDye(int rgb) {
        int[] colours = {
            0xF9FFFE, 0xF9801D, 0xC74EBD, 0x3AB3DA,
            0xFED83D, 0x80C71F, 0xF38BAA, 0x474F52,
            0x9D9D97, 0x169C9C, 0x8932B8, 0x3C44AA,
            0x835432, 0x5E7C16, 0xB02E26, 0x1D1D21
        };

        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;
        long bestDistance = Long.MAX_VALUE;
        int best = 0;

        for (int i = 0; i < colours.length; i++) {
            int dr = red - ((colours[i] >> 16) & 0xFF);
            int dg = green - ((colours[i] >> 8) & 0xFF);
            int db = blue - (colours[i] & 0xFF);
            long distance =
                (long) dr * dr
                    + (long) dg * dg
                    + (long) db * db;

            if (distance < bestDistance) {
                bestDistance = distance;
                best = i;
            }
        }

        return best;
    }

    private WingEntityModel<T> resolveModel(
        ItemStack stack,
        WingItem wingItem
    ) {
        return switch (wingItem.getWingType()) {
            case FEATHERED, MECHANICAL_FEATHERED -> featheredWings;
            case DRAGON, MECHANICAL_LEATHER -> leatherWings;
            case LIGHT -> lightWings;
            case UNIQUE -> {
                if (stack.is(IcarusItems.FLANDRES_WINGS.get())) {
                    yield flandresWings;
                }
                if (stack.is(IcarusItems.DISCORDS_WINGS.get())) {
                    yield discordsWings;
                }
                if (stack.is(IcarusItems.ZANZAS_WINGS.get())) {
                    yield zanzasWings;
                }
                yield null;
            }
        };
    }

    private WingPlacement placementFor(
        ItemStack stack,
        WingItem wingItem
    ) {
        if (stack.is(IcarusItems.ZANZAS_WINGS.get())) {
            // Tuned against the Fairy scale + FA+Player. Previous -0.22
            // was visibly too high; -0.12 centres the roots on the upper back.
            return WingPlacement.ZANZA;
        }

        if (stack.is(IcarusItems.FLANDRES_WINGS.get())) {
            return WingPlacement.FLANDRE;
        }

        if (stack.is(IcarusItems.DISCORDS_WINGS.get())) {
            return WingPlacement.DISCORD;
        }

        return switch (wingItem.getWingType()) {
            case FEATHERED, MECHANICAL_FEATHERED ->
                WingPlacement.FEATHERED;
            case DRAGON, MECHANICAL_LEATHER ->
                WingPlacement.LEATHER;
            case LIGHT ->
                WingPlacement.LIGHT;
            case UNIQUE ->
                WingPlacement.DEFAULT;
        };
    }

    private void renderPass(
        WingEntityModel<T> model,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        ItemStack wings,
        ResourceLocation texture,
        float red,
        float green,
        float blue
    ) {
        VertexConsumer consumer =
            ItemRenderer.getArmorFoilBuffer(
                buffer,
                RenderType.entityTranslucent(texture),
                false,
                wings.hasFoil()
            );

        model.renderToBuffer(
            poseStack,
            consumer,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            red,
            green,
            blue,
            1.0F
        );
    }

    private record WingPlacement(
        double x,
        double y,
        double z
    ) {
        private static final WingPlacement DEFAULT =
            new WingPlacement(0.0D, -0.10D, 0.125D);

        private static final WingPlacement ZANZA =
            new WingPlacement(0.0D, -0.12D, 0.125D);

        private static final WingPlacement FEATHERED =
            new WingPlacement(0.0D, -0.10D, 0.125D);

        private static final WingPlacement LEATHER =
            new WingPlacement(0.0D, -0.10D, 0.125D);

        private static final WingPlacement LIGHT =
            new WingPlacement(0.0D, -0.10D, 0.125D);

        private static final WingPlacement FLANDRE =
            new WingPlacement(0.0D, -0.10D, 0.125D);

        private static final WingPlacement DISCORD =
            new WingPlacement(0.0D, -0.10D, 0.125D);
    }
}
