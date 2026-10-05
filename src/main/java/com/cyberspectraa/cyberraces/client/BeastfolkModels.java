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
        CubeDeformation deformation = CubeDeformation.NONE;
        CubeDeformation tailTipDeformation = new CubeDeformation(-0.02F);

        // Exact vanilla 1.20.1 Ocelot/Cat ear cube sizes and UVs.
        root.addOrReplaceChild(
            "right_ear",
            CubeListBuilder.create()
                .texOffs(0, 10)
                .addBox(-0.5F, -1.0F, -1.0F, 1.0F, 1.0F, 2.0F, deformation),
            PartPose.offset(-2.0F, -8.0F, 0.0F)
        );

        root.addOrReplaceChild(
            "left_ear",
            CubeListBuilder.create()
                .texOffs(6, 10)
                .addBox(-0.5F, -1.0F, -1.0F, 1.0F, 1.0F, 2.0F, deformation),
            PartPose.offset(2.0F, -8.0F, 0.0F)
        );

        // Vanilla Cat/Ocelot tail: two 1x8x1 sections using the original UVs.
        root.addOrReplaceChild(
            "tail1",
            CubeListBuilder.create()
                .texOffs(0, 15)
                .addBox(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, deformation),
            PartPose.offsetAndRotation(0.0F, 8.0F, 2.0F, 0.90F, 0.0F, 0.0F)
        );

        root.addOrReplaceChild(
            "tail2",
            CubeListBuilder.create()
                .texOffs(4, 15)
                .addBox(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, tailTipDeformation),
            PartPose.offsetAndRotation(0.0F, 13.0F, 8.2F, 1.7278761F, 0.0F, 0.0F)
        );

        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition createWolfLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Exact vanilla Wolf ear cube size/UV from real_head.
        root.addOrReplaceChild(
            "right_ear",
            CubeListBuilder.create()
                .texOffs(16, 14)
                .addBox(-1.0F, -2.0F, -0.5F, 2.0F, 2.0F, 1.0F),
            PartPose.offset(-2.0F, -8.0F, 0.5F)
        );

        root.addOrReplaceChild(
            "left_ear",
            CubeListBuilder.create()
                .texOffs(16, 14)
                .addBox(-1.0F, -2.0F, -0.5F, 2.0F, 2.0F, 1.0F),
            PartPose.offset(2.0F, -8.0F, 0.5F)
        );

        // Exact vanilla Wolf real_tail: 2x8x2 with UV 9,18.
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

        // Exact vanilla Fox ear cube sizes and UVs.
        root.addOrReplaceChild(
            "right_ear",
            CubeListBuilder.create()
                .texOffs(8, 1)
                .addBox(-1.0F, -2.0F, -0.5F, 2.0F, 2.0F, 1.0F),
            PartPose.offset(-2.8F, -8.0F, -0.15F)
        );

        root.addOrReplaceChild(
            "left_ear",
            CubeListBuilder.create()
                .texOffs(15, 1)
                .addBox(-1.0F, -2.0F, -0.5F, 2.0F, 2.0F, 1.0F),
            PartPose.offset(2.8F, -8.0F, -0.15F)
        );

        // Exact vanilla Fox tail box: 4x9x5 with UV 30,0.
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
