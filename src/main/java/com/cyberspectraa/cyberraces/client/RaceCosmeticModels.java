package com.cyberspectraa.cyberraces.client;

import com.cyberspectraa.cyberraces.CyberRaces;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public final class RaceCosmeticModels {
    public static final ModelLayerLocation SKIN_FEATURES =
        new ModelLayerLocation(new ResourceLocation(CyberRaces.MOD_ID, "skin_race_features"), "main");

    public static final ModelLayerLocation HARD_FEATURES =
        new ModelLayerLocation(new ResourceLocation(CyberRaces.MOD_ID, "hard_race_features"), "main");

    private RaceCosmeticModels() {
    }

    public static LayerDefinition createSkinLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        addEarPair(root, "elf_short", 2.4F, 1.8F, 1.8F, -0.08F, -0.12F);
        addEarPair(root, "elf_long", 3.3F, 2.4F, 1.7F, -0.12F, -0.18F);
        addEarPair(root, "elf_high", 2.8F, 2.3F, 1.8F, -0.24F, -0.34F);

        addEarPair(root, "halfling_round", 1.8F, 1.6F, 2.6F, -0.03F, -0.06F);
        addEarPair(root, "halfling_soft", 2.1F, 1.8F, 2.4F, -0.08F, -0.10F);
        addEarPair(root, "halfling_pointed", 2.5F, 2.0F, 1.9F, -0.12F, -0.18F);

        addEarPair(root, "orc_ears", 2.4F, 2.0F, 2.1F, -0.08F, -0.12F);

        addEarPair(root, "goblin_wide", 3.7F, 2.7F, 2.9F, 0.00F, -0.05F);
        addEarPair(root, "goblin_long", 4.4F, 3.0F, 2.5F, -0.07F, -0.10F);
        addEarPair(root, "goblin_swept", 3.9F, 2.8F, 2.5F, 0.08F, 0.18F);

        addEarPair(root, "fairy_classic", 2.0F, 2.0F, 1.8F, -0.14F, -0.18F);
        addEarPair(root, "fairy_sharp", 2.7F, 2.3F, 1.5F, -0.22F, -0.28F);
        addEarPair(root, "fairy_soft", 1.9F, 1.7F, 2.2F, -0.06F, -0.08F);

        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition createHardLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        addSegmentedTuskPair(root, "tusks_small", 1.5F, 0.75F, 0.18F);
        addSegmentedTuskPair(root, "tusks_broad", 2.0F, 0.95F, 0.30F);
        addSegmentedTuskPair(root, "tusks_long", 2.7F, 0.85F, 0.46F);

        addSegmentedHornPair(root, "horns_curved", 2.1F, 1.7F, 1.25F, 0.15F, 0.24F, -0.34F);
        addSegmentedHornPair(root, "horns_swept", 2.0F, 1.8F, 1.35F, -0.18F, 0.42F, -0.18F);
        addSegmentedHornPair(root, "horns_tall", 2.5F, 2.0F, 1.55F, 0.02F, 0.08F, -0.10F);

        addTieflingTail(root);

        addSegmentedHornPair(root, "dragon_horned", 1.8F, 1.55F, 1.15F, 0.04F, 0.18F, -0.26F);
        addSegmentedHornPair(root, "dragon_crowned", 2.1F, 1.8F, 1.3F, 0.20F, 0.10F, -0.12F);
        addSegmentedHornPair(root, "dragon_swept", 2.0F, 1.7F, 1.3F, -0.20F, 0.40F, -0.16F);

        addDragonTail(root);

        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void addEarPair(
        PartDefinition root,
        String name,
        float baseLength,
        float tipLength,
        float height,
        float pitch,
        float roll
    ) {
        PartDefinition left = root.addOrReplaceChild(
            name + "_left",
            CubeListBuilder.create()
                .texOffs(16, 8)
                .addBox(0.0F, -height / 2.0F, -0.55F, baseLength, height, 1.1F),
            PartPose.offsetAndRotation(3.75F, -4.8F, 0.0F, pitch, 0.0F, roll)
        );

        left.addOrReplaceChild(
            "tip",
            CubeListBuilder.create()
                .texOffs(18, 10)
                .addBox(0.0F, -height * 0.34F, -0.40F, tipLength, height * 0.68F, 0.8F),
            PartPose.offsetAndRotation(baseLength - 0.15F, 0.0F, 0.0F, pitch * 0.35F, 0.0F, roll * 0.45F)
        );

        PartDefinition right = root.addOrReplaceChild(
            name + "_right",
            CubeListBuilder.create()
                .texOffs(0, 8)
                .mirror()
                .addBox(-baseLength, -height / 2.0F, -0.55F, baseLength, height, 1.1F)
                .mirror(false),
            PartPose.offsetAndRotation(-3.75F, -4.8F, 0.0F, pitch, 0.0F, -roll)
        );

        right.addOrReplaceChild(
            "tip",
            CubeListBuilder.create()
                .texOffs(2, 10)
                .mirror()
                .addBox(-tipLength, -height * 0.34F, -0.40F, tipLength, height * 0.68F, 0.8F)
                .mirror(false),
            PartPose.offsetAndRotation(-baseLength + 0.15F, 0.0F, 0.0F, pitch * 0.35F, 0.0F, -roll * 0.45F)
        );
    }

    private static void addSegmentedTuskPair(
        PartDefinition root,
        String name,
        float baseLength,
        float baseWidth,
        float outward
    ) {
        addTusk(root, name + "_left", 1.85F, baseLength, baseWidth, -outward);
        addTusk(root, name + "_right", -1.85F, baseLength, baseWidth, outward);
    }

    private static void addTusk(
        PartDefinition root,
        String name,
        float x,
        float baseLength,
        float baseWidth,
        float roll
    ) {
        PartDefinition base = root.addOrReplaceChild(
            name,
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-baseWidth / 2.0F, 0.0F, -baseWidth / 2.0F, baseWidth, baseLength, baseWidth),
            PartPose.offsetAndRotation(x, -0.55F, -4.20F, -0.20F, 0.0F, roll)
        );

        float midWidth = Math.max(0.45F, baseWidth * 0.72F);
        PartDefinition mid = base.addOrReplaceChild(
            "mid",
            CubeListBuilder.create()
                .texOffs(6, 0)
                .addBox(-midWidth / 2.0F, 0.0F, -midWidth / 2.0F, midWidth, baseLength * 0.72F, midWidth),
            PartPose.offsetAndRotation(0.0F, baseLength - 0.10F, -0.08F, -0.14F, 0.0F, roll * 0.26F)
        );

        float tipWidth = Math.max(0.28F, baseWidth * 0.42F);
        mid.addOrReplaceChild(
            "tip",
            CubeListBuilder.create()
                .texOffs(10, 0)
                .addBox(-tipWidth / 2.0F, 0.0F, -tipWidth / 2.0F, tipWidth, baseLength * 0.55F, tipWidth),
            PartPose.offsetAndRotation(0.0F, baseLength * 0.68F, -0.06F, -0.10F, 0.0F, roll * 0.20F)
        );
    }

    private static void addSegmentedHornPair(
        PartDefinition root,
        String name,
        float baseLength,
        float midLength,
        float tipLength,
        float pitch,
        float roll,
        float curve
    ) {
        addHorn(root, name + "_left", 2.55F, baseLength, midLength, tipLength, pitch, roll, curve);
        addHorn(root, name + "_right", -2.55F, baseLength, midLength, tipLength, pitch, -roll, -curve);
    }

    private static void addHorn(
        PartDefinition root,
        String name,
        float x,
        float baseLength,
        float midLength,
        float tipLength,
        float pitch,
        float roll,
        float curve
    ) {
        PartDefinition base = root.addOrReplaceChild(
            name,
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-0.85F, -baseLength, -0.85F, 1.7F, baseLength, 1.7F),
            PartPose.offsetAndRotation(x, -7.0F, 0.55F, pitch, 0.0F, roll)
        );

        PartDefinition mid = base.addOrReplaceChild(
            "mid",
            CubeListBuilder.create()
                .texOffs(8, 0)
                .addBox(-0.62F, -midLength, -0.62F, 1.24F, midLength, 1.24F),
            PartPose.offsetAndRotation(0.0F, -baseLength + 0.12F, 0.0F, curve, 0.0F, curve * 0.20F)
        );

        mid.addOrReplaceChild(
            "tip",
            CubeListBuilder.create()
                .texOffs(14, 0)
                .addBox(-0.36F, -tipLength, -0.36F, 0.72F, tipLength, 0.72F),
            PartPose.offsetAndRotation(0.0F, -midLength + 0.10F, 0.0F, curve * 0.85F, 0.0F, curve * 0.16F)
        );
    }

    private static void addTieflingTail(PartDefinition root) {
        PartDefinition tail = root.addOrReplaceChild(
            "tiefling_tail",
            CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-0.75F, 0.0F, -0.75F, 1.5F, 4.0F, 1.5F),
            PartPose.offsetAndRotation(0.0F, 8.2F, 2.0F, 0.64F, 0.0F, 0.0F)
        );

        PartDefinition mid = tail.addOrReplaceChild(
            "mid",
            CubeListBuilder.create()
                .texOffs(8, 0)
                .addBox(-0.62F, 0.0F, -0.62F, 1.24F, 4.0F, 1.24F),
            PartPose.offsetAndRotation(0.0F, 3.6F, 0.8F, 0.45F, 0.0F, 0.0F)
        );

        PartDefinition lower = mid.addOrReplaceChild(
            "lower",
            CubeListBuilder.create()
                .texOffs(14, 0)
                .addBox(-0.48F, 0.0F, -0.48F, 0.96F, 3.8F, 0.96F),
            PartPose.offsetAndRotation(0.0F, 3.6F, 0.75F, 0.38F, 0.0F, 0.0F)
        );

        lower.addOrReplaceChild(
            "spade",
            CubeListBuilder.create()
                .texOffs(20, 0)
                .addBox(-1.35F, -1.35F, -0.38F, 2.7F, 2.7F, 0.76F),
            PartPose.offsetAndRotation(0.0F, 4.0F, 0.45F, 0.0F, 0.0F, 0.7854F)
        );
    }

    private static void addDragonTail(PartDefinition root) {
        PartDefinition tail = root.addOrReplaceChild(
            "dragon_tail",
            CubeListBuilder.create()
                .texOffs(0, 32)
                .addBox(-1.35F, 0.0F, -1.15F, 2.7F, 4.6F, 2.3F),
            PartPose.offsetAndRotation(0.0F, 8.1F, 2.0F, 0.60F, 0.0F, 0.0F)
        );

        PartDefinition mid = tail.addOrReplaceChild(
            "mid",
            CubeListBuilder.create()
                .texOffs(12, 32)
                .addBox(-1.08F, 0.0F, -0.90F, 2.16F, 4.5F, 1.8F),
            PartPose.offsetAndRotation(0.0F, 4.1F, 0.9F, 0.42F, 0.0F, 0.0F)
        );

        PartDefinition lower = mid.addOrReplaceChild(
            "lower",
            CubeListBuilder.create()
                .texOffs(22, 32)
                .addBox(-0.78F, 0.0F, -0.68F, 1.56F, 4.2F, 1.36F),
            PartPose.offsetAndRotation(0.0F, 4.0F, 0.75F, 0.34F, 0.0F, 0.0F)
        );

        lower.addOrReplaceChild(
            "tip",
            CubeListBuilder.create()
                .texOffs(30, 32)
                .addBox(-0.46F, 0.0F, -0.42F, 0.92F, 3.7F, 0.84F),
            PartPose.offsetAndRotation(0.0F, 3.8F, 0.55F, 0.26F, 0.0F, 0.0F)
        );
    }
}
