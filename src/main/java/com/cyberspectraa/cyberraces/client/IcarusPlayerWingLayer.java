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

        boolean fairyPreview =
            entity instanceof AbstractClientPlayer player
                && ClientCharacterState.isPreviewing(Race.FAIRY, player);

        if (wings.isEmpty() && fairyPreview) {
            wings = new ItemStack(IcarusItems.ZANZAS_WINGS.get());
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
