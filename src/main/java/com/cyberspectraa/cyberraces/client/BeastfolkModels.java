package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.CyberRaces;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public final class BeastfolkModels {
    public static final ModelLayerLocation CAT =
        new ModelLayerLocation(
            new ResourceLocation(CyberRaces.MOD_ID, "beastfolk_cat"),
            "main"
        );

    public static final ModelLayerLocation WOLF =
        new ModelLayerLocation(
            new ResourceLocation(CyberRaces.MOD_ID, "beastfolk_wolf"),
            "main"
        );

    public static final ModelLayerLocation FOX =
        new ModelLayerLocation(
            new ResourceLocation(CyberRaces.MOD_ID, "beastfolk_fox"),
            "main"
        );

    private BeastfolkModels() {
    }

    public static LayerDefinition createCatLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        /*
         * Rebuilt from the supplied Ice_Game Figura Cat_ears.bbmodel:
         * paired 3x3x1 main ear cubes, 3x1x1 bases, 1x1x1 tips,
         * and a four-segment 1x1x3 articulated tail.
         *
         * Original Figura coordinates use positive Y upward. We anchor
         * the ear group to the vanilla head and tail to the body.
         * Each 64x64 atlas is repacked from the matching Figura files.
         */
        PartDefinition rightEar = root.addOrReplaceChild(
            "right_ear",
            CubeListBuilder.create()
                .texOffs(20, 0)
                .addBox(-1.5F, 0.0F, -0.5F, 3.0F, 1.0F, 1.0F)
                .texOffs(0, 0)
                .addBox(-1.5F, -3.0F, -0.5F, 3.0F, 3.0F, 1.0F)
                .texOffs(40, 0)
                .addBox(-0.5F, -4.0F, -0.5F, 1.0F, 1.0F, 1.0F),
            PartPose.offset(-2.3F, -7.0F, -2.5F)
        );
        // A small thin patch gives depth and soft inner-ear colour.
        rightEar.addOrReplaceChild(
            "inner", CubeListBuilder.create()
                .texOffs(52, 0)
                .addBox(-0.48F, -2.65F, -0.56F, 0.96F, 1.70F, 0.06F),
            PartPose.ZERO
        );

        PartDefinition leftEar = root.addOrReplaceChild(
            "left_ear",
            CubeListBuilder.create()
                .texOffs(30, 0)
                .addBox(-1.5F, 0.0F, -0.5F, 3.0F, 1.0F, 1.0F)
                .texOffs(10, 0)
                .addBox(-1.5F, -3.0F, -0.5F, 3.0F, 3.0F, 1.0F)
                .texOffs(46, 0)
                .addBox(-0.5F, -4.0F, -0.5F, 1.0F, 1.0F, 1.0F),
            PartPose.offset(2.3F, -7.0F, -2.5F)
        );
        leftEar.addOrReplaceChild(
            "inner", CubeListBuilder.create()
                .texOffs(58, 0)
                .addBox(-0.48F, -2.65F, -0.56F, 0.96F, 1.70F, 0.06F),
            PartPose.ZERO
        );

        // The four original straight Figura tail sections are nested
        // rather than independent, so slight motion never opens a gap.
        PartDefinition tail1 = root.addOrReplaceChild(
            "tail",
            CubeListBuilder.create()
                .texOffs(0, 10)
                .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F),
            PartPose.offsetAndRotation(0.0F, -3.0F, 2.0F,
                    0.0F, 0.0F, 0.0F)
        );
        PartDefinition tail2 = tail1.addOrReplaceChild(
            "tail2",
            CubeListBuilder.create()
                .texOffs(12, 10)
                .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F),
            PartPose.offset(0.0F, 0.0F, 2.85F)
        );
        PartDefinition tail3 = tail2.addOrReplaceChild(
            "tail3",
            CubeListBuilder.create()
                .texOffs(24, 10)
                .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F),
            PartPose.offset(0.0F, 0.0F, 2.85F)
        );
        tail3.addOrReplaceChild(
            "tail4",
            CubeListBuilder.create()
                .texOffs(36, 10)
                .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F),
            PartPose.offset(0.0F, 0.0F, 2.85F)
        );

        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition createWolfLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition rightEar = root.addOrReplaceChild(
            "right_ear",
            CubeListBuilder.create()
                .texOffs(16, 14)
                .addBox(-1.0F, -2.0F, -0.5F, 2.0F, 2.0F, 1.0F),
            PartPose.offset(-2.0F, -8.0F, 0.5F)
        );

        PartDefinition leftEar = root.addOrReplaceChild(
            "left_ear",
            CubeListBuilder.create()
                .texOffs(16, 14)
                .addBox(-1.0F, -2.0F, -0.5F, 2.0F, 2.0F, 1.0F),
            PartPose.offset(2.0F, -8.0F, 0.5F)
        );

        // Exact vanilla Wolf real_tail geometry and UVs.
        root.addOrReplaceChild(
            "tail",
            CubeListBuilder.create()
                .texOffs(9, 18)
                .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F),
            PartPose.offsetAndRotation(
                0.0F,
                8.0F,
                2.0F,
                (float) Math.PI / 5.0F,
                0.0F,
                0.0F
            )
        );

        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition createFoxLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition rightEar = root.addOrReplaceChild(
            "right_ear",
            CubeListBuilder.create()
                .texOffs(8, 1)
                .addBox(-1.0F, -2.0F, -0.5F, 2.0F, 2.0F, 1.0F),
            PartPose.offset(-2.8F, -8.0F, -0.15F)
        );

        PartDefinition leftEar = root.addOrReplaceChild(
            "left_ear",
            CubeListBuilder.create()
                .texOffs(15, 1)
                .addBox(-1.0F, -2.0F, -0.5F, 2.0F, 2.0F, 1.0F),
            PartPose.offset(2.8F, -8.0F, -0.15F)
        );

        // Exact vanilla Fox tail geometry and UVs.
        root.addOrReplaceChild(
            "tail",
            CubeListBuilder.create()
                .texOffs(30, 0)
                .addBox(-2.0F, 0.0F, -2.5F, 4.0F, 9.0F, 5.0F),
            PartPose.offsetAndRotation(0.0F, 8.0F, 2.0F, 0.56F, 0.0F, 0.0F)
        );

        return LayerDefinition.create(mesh, 48, 32);
    }

}