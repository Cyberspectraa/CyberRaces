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

        addEarPair(root, "orc_short", 2.2F, 1.7F, 2.0F, -0.08F, -0.10F);
        addEarPair(root, "orc_broad", 2.8F, 2.0F, 2.6F, -0.04F, -0.08F);
        addEarPair(root, "orc_swept", 3.0F, 2.2F, 2.1F, 0.06F, 0.16F);

        addEarPair(root, "goblin_wide", 3.7F, 2.7F, 2.9F, 0.00F, -0.05F);
        addEarPair(root, "goblin_long", 4.4F, 3.0F, 2.5F, -0.07F, -0.10F);
        addEarPair(root, "goblin_swept", 3.9F, 2.8F, 2.5F, 0.08F, 0.18F);

        addEarPair(root, "fairy_classic", 2.0F, 2.0F, 1.8F, -0.14F, -0.18F);
        addEarPair(root, "fairy_sharp", 2.7F, 2.3F, 1.5F, -0.22F, -0.28F);
        addEarPair(root, "fairy_soft", 1.9F, 1.7F, 2.2F, -0.06F, -0.08F);

        addTopEarPair(root, "cat_pointed", 2.05F, 2.15F, 1.15F, 1.55F, 2.15F, 0.08F);
        addTopEarPair(root, "cat_tufted", 2.15F, 2.35F, 1.10F, 1.85F, 2.20F, 0.12F);
        addTopEarPair(root, "cat_round", 2.35F, 1.65F, 1.30F, 1.05F, 2.10F, 0.04F);

        addTopEarPair(root, "dog_upright", 2.35F, 2.55F, 1.35F, 1.55F, 2.25F, 0.08F);
        addFloppyEarPair(root, "dog_floppy", 2.65F, 3.05F, 1.45F, 0.22F);
        addEarPair(root, "dog_round", 2.55F, 1.95F, 2.55F, -0.02F, -0.04F);

        addTopEarPair(root, "fox_tall", 2.10F, 3.00F, 1.20F, 2.10F, 2.45F, 0.10F);
        addTopEarPair(root, "fox_wide", 2.55F, 2.55F, 1.30F, 1.85F, 2.65F, 0.16F);
        addTopEarPair(root, "fox_swept", 2.20F, 2.70F, 1.15F, 1.95F, 2.50F, 0.28F);

        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition createHardLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        addSegmentedHornPair(root, "horns_curved", 2.1F, 1.7F, 1.25F, 0.15F, 0.24F, -0.34F);
        addSegmentedHornPair(root, "horns_swept", 2.0F, 1.8F, 1.35F, -0.18F, 0.42F, -0.18F);
        addSegmentedHornPair(root, "horns_tall", 2.5F, 2.0F, 1.55F, 0.02F, 0.08F, -0.10F);

        addTieflingTail(root);

        addSegmentedHornPair(root, "dragon_horned", 1.8F, 1.55F, 1.15F, 0.04F, 0.18F, -0.26F);
        addSegmentedHornPair(root, "dragon_crowned", 2.1F, 1.8F, 1.3F, 0.20F, 0.10F, -0.12F);
        addSegmentedHornPair(root, "dragon_swept", 2.0F, 1.7F, 1.3F, -0.20F, 0.40F, -0.16F);

        addDragonTail(root);

        addCatTail(root);
        addDogTail(root);
        addFoxTail(root);

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

    private static void addTopEarPair(
        PartDefinition root,
        String name,
        float width,
        float height,
        float depth,
        float tipHeight,
        float spacing,
        float roll
    ) {
        PartDefinition left = root.addOrReplaceChild(
            name + "_left",
            CubeListBuilder.create()
                .texOffs(20, 8)
                .addBox(-width / 2.0F, -height, -depth / 2.0F, width, height, depth),
            PartPose.offsetAndRotation(spacing, -7.8F, 0.0F, 0.0F, 0.0F, -roll)
        );

        left.addOrReplaceChild(
            "tip",
            CubeListBuilder.create()
                .texOffs(24, 8)
                .addBox(-width * 0.32F, -tipHeight, -depth * 0.36F, width * 0.64F, tipHeight, depth * 0.72F),
            PartPose.offsetAndRotation(0.0F, -height + 0.10F, 0.0F, -0.04F, 0.0F, -roll * 0.35F)
        );

        PartDefinition right = root.addOrReplaceChild(
            name + "_right",
            CubeListBuilder.create()
                .texOffs(20, 8)
                .mirror()
                .addBox(-width / 2.0F, -height, -depth / 2.0F, width, height, depth)
                .mirror(false),
            PartPose.offsetAndRotation(-spacing, -7.8F, 0.0F, 0.0F, 0.0F, roll)
        );

        right.addOrReplaceChild(
            "tip",
            CubeListBuilder.create()
                .texOffs(24, 8)
                .mirror()
                .addBox(-width * 0.32F, -tipHeight, -depth * 0.36F, width * 0.64F, tipHeight, depth * 0.72F)
                .mirror(false),
            PartPose.offsetAndRotation(0.0F, -height + 0.10F, 0.0F, -0.04F, 0.0F, roll * 0.35F)
        );
    }

    private static void addFloppyEarPair(
        PartDefinition root,
        String name,
        float width,
        float length,
        float depth,
        float outwardRoll
    ) {
        PartDefinition left = root.addOrReplaceChild(
            name + "_left",
            CubeListBuilder.create()
                .texOffs(28, 8)
                .addBox(-0.15F, 0.0F, -depth / 2.0F, width, length, depth),
            PartPose.offsetAndRotation(3.65F, -6.35F, 0.0F, 0.06F, 0.0F, outwardRoll)
        );

        left.addOrReplaceChild(
            "tip",
            CubeListBuilder.create()
                .texOffs(34, 8)
                .addBox(0.0F, 0.0F, -depth * 0.38F, width * 0.72F, length * 0.58F, depth * 0.76F),
            PartPose.offsetAndRotation(width * 0.18F, length - 0.25F, 0.0F, 0.18F, 0.0F, outwardRoll * 0.25F)
        );

        PartDefinition right = root.addOrReplaceChild(
            name + "_right",
            CubeListBuilder.create()
                .texOffs(28, 8)
                .mirror()
                .addBox(-width + 0.15F, 0.0F, -depth / 2.0F, width, length, depth)
                .mirror(false),
            PartPose.offsetAndRotation(-3.65F, -6.35F, 0.0F, 0.06F, 0.0F, -outwardRoll)
        );

        right.addOrReplaceChild(
            "tip",
            CubeListBuilder.create()
                .texOffs(34, 8)
                .mirror()
                .addBox(-width * 0.72F, 0.0F, -depth * 0.38F, width * 0.72F, length * 0.58F, depth * 0.76F)
                .mirror(false),
            PartPose.offsetAndRotation(-width * 0.18F, length - 0.25F, 0.0F, 0.18F, 0.0F, -outwardRoll * 0.25F)
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
        PartDefinition base = root.addOrReplaceChild(
            "tiefling_tail",
            CubeListBuilder.create()
                .texOffs(0, 16)
                .addBox(-0.72F, 0.0F, -0.72F, 1.44F, 3.0F, 1.44F),
            PartPose.offsetAndRotation(0.0F, 8.15F, 2.05F, 0.50F, 0.0F, 0.0F)
        );

        PartDefinition mid1 = base.addOrReplaceChild(
            "mid1",
            CubeListBuilder.create()
                .texOffs(6, 16)
                .addBox(-0.62F, 0.0F, -0.62F, 1.24F, 3.1F, 1.24F),
            PartPose.offsetAndRotation(0.0F, 2.75F, 0.72F, 0.30F, 0.0F, 0.0F)
        );

        PartDefinition mid2 = mid1.addOrReplaceChild(
            "mid2",
            CubeListBuilder.create()
                .texOffs(12, 16)
                .addBox(-0.52F, 0.0F, -0.52F, 1.04F, 3.0F, 1.04F),
            PartPose.offsetAndRotation(0.0F, 2.85F, 0.58F, 0.24F, 0.0F, 0.0F)
        );

        PartDefinition mid3 = mid2.addOrReplaceChild(
            "mid3",
            CubeListBuilder.create()
                .texOffs(18, 16)
                .addBox(-0.41F, 0.0F, -0.41F, 0.82F, 2.8F, 0.82F),
            PartPose.offsetAndRotation(0.0F, 2.75F, 0.48F, 0.18F, 0.0F, 0.0F)
        );

        PartDefinition tipStem = mid3.addOrReplaceChild(
            "tip_stem",
            CubeListBuilder.create()
                .texOffs(23, 16)
                .addBox(-0.30F, 0.0F, -0.30F, 0.60F, 2.1F, 0.60F),
            PartPose.offsetAndRotation(0.0F, 2.55F, 0.34F, 0.12F, 0.0F, 0.0F)
        );

        PartDefinition spade = tipStem.addOrReplaceChild(
            "spade",
            CubeListBuilder.create(),
            PartPose.offsetAndRotation(0.0F, 2.0F, 0.18F, -0.12F, 0.0F, 0.0F)
        );

        // A layered arrow/spade silhouette looks much cleaner than the old
        // single rotated square while staying extremely cheap to render.
        spade.addOrReplaceChild(
            "core",
            CubeListBuilder.create()
                .texOffs(28, 16)
                .addBox(-0.32F, -0.10F, -0.30F, 0.64F, 1.85F, 0.60F),
            PartPose.ZERO
        );

        spade.addOrReplaceChild(
            "left_blade",
            CubeListBuilder.create()
                .texOffs(32, 16)
                .addBox(-1.20F, -0.05F, -0.26F, 1.25F, 1.05F, 0.52F),
            PartPose.offsetAndRotation(-0.05F, 0.28F, 0.0F, 0.0F, 0.0F, -0.58F)
        );

        spade.addOrReplaceChild(
            "right_blade",
            CubeListBuilder.create()
                .texOffs(32, 20)
                .addBox(-0.05F, -0.05F, -0.26F, 1.25F, 1.05F, 0.52F),
            PartPose.offsetAndRotation(0.05F, 0.28F, 0.0F, 0.0F, 0.0F, 0.58F)
        );

        spade.addOrReplaceChild(
            "point",
            CubeListBuilder.create()
                .texOffs(40, 16)
                .addBox(-0.24F, 0.0F, -0.24F, 0.48F, 1.20F, 0.48F),
            PartPose.offsetAndRotation(0.0F, 1.55F, 0.0F, 0.0F, 0.0F, 0.0F)
        );
    }

    private static void addDragonTail(PartDefinition root) {
        PartDefinition base = root.addOrReplaceChild(
            "dragon_tail",
            CubeListBuilder.create()
                .texOffs(0, 32)
                .addBox(-1.48F, 0.0F, -1.28F, 2.96F, 3.4F, 2.56F),
            PartPose.offsetAndRotation(0.0F, 8.05F, 1.95F, 0.48F, 0.0F, 0.0F)
        );

        addDragonRidge(base, "ridge0", 0.82F, 0.0F, 0.15F);

        PartDefinition mid1 = base.addOrReplaceChild(
            "mid1",
            CubeListBuilder.create()
                .texOffs(12, 32)
                .addBox(-1.28F, 0.0F, -1.10F, 2.56F, 3.6F, 2.20F),
            PartPose.offsetAndRotation(0.0F, 3.05F, 0.78F, 0.27F, 0.0F, 0.0F)
        );

        addDragonRidge(mid1, "ridge1", 0.70F, 0.10F, 0.18F);

        PartDefinition mid2 = mid1.addOrReplaceChild(
            "mid2",
            CubeListBuilder.create()
                .texOffs(24, 32)
                .addBox(-1.04F, 0.0F, -0.90F, 2.08F, 3.6F, 1.80F),
            PartPose.offsetAndRotation(0.0F, 3.28F, 0.66F, 0.22F, 0.0F, 0.0F)
        );

        addDragonRidge(mid2, "ridge2", 0.58F, 0.15F, 0.20F);

        PartDefinition mid3 = mid2.addOrReplaceChild(
            "mid3",
            CubeListBuilder.create()
                .texOffs(34, 32)
                .addBox(-0.82F, 0.0F, -0.72F, 1.64F, 3.45F, 1.44F),
            PartPose.offsetAndRotation(0.0F, 3.30F, 0.54F, 0.18F, 0.0F, 0.0F)
        );

        addDragonRidge(mid3, "ridge3", 0.46F, 0.18F, 0.22F);

        PartDefinition lower = mid3.addOrReplaceChild(
            "lower",
            CubeListBuilder.create()
                .texOffs(42, 32)
                .addBox(-0.61F, 0.0F, -0.55F, 1.22F, 3.15F, 1.10F),
            PartPose.offsetAndRotation(0.0F, 3.18F, 0.43F, 0.14F, 0.0F, 0.0F)
        );

        lower.addOrReplaceChild(
            "tip",
            CubeListBuilder.create()
                .texOffs(48, 32)
                .addBox(-0.38F, 0.0F, -0.36F, 0.76F, 2.65F, 0.72F),
            PartPose.offsetAndRotation(0.0F, 2.95F, 0.30F, 0.10F, 0.0F, 0.0F)
        );
    }

    private static void addCatTail(PartDefinition root) {
        PartDefinition base = root.addOrReplaceChild(
            "cat_tail",
            CubeListBuilder.create()
                .texOffs(0, 48)
                .addBox(-0.46F, 0.0F, -0.46F, 0.92F, 3.0F, 0.92F),
            PartPose.offsetAndRotation(0.0F, 8.1F, 2.0F, 0.44F, 0.0F, 0.0F)
        );

        PartDefinition mid1 = base.addOrReplaceChild(
            "mid1",
            CubeListBuilder.create()
                .texOffs(4, 48)
                .addBox(-0.42F, 0.0F, -0.42F, 0.84F, 3.1F, 0.84F),
            PartPose.offsetAndRotation(0.0F, 2.8F, 0.52F, 0.18F, 0.0F, 0.0F)
        );

        PartDefinition mid2 = mid1.addOrReplaceChild(
            "mid2",
            CubeListBuilder.create()
                .texOffs(8, 48)
                .addBox(-0.37F, 0.0F, -0.37F, 0.74F, 3.0F, 0.74F),
            PartPose.offsetAndRotation(0.0F, 2.85F, 0.38F, 0.12F, 0.0F, 0.0F)
        );

        PartDefinition mid3 = mid2.addOrReplaceChild(
            "mid3",
            CubeListBuilder.create()
                .texOffs(12, 48)
                .addBox(-0.31F, 0.0F, -0.31F, 0.62F, 2.8F, 0.62F),
            PartPose.offsetAndRotation(0.0F, 2.75F, 0.28F, 0.08F, 0.0F, 0.0F)
        );

        mid3.addOrReplaceChild(
            "tip",
            CubeListBuilder.create()
                .texOffs(16, 48)
                .addBox(-0.24F, 0.0F, -0.24F, 0.48F, 2.4F, 0.48F),
            PartPose.offsetAndRotation(0.0F, 2.55F, 0.20F, 0.05F, 0.0F, 0.0F)
        );
    }

    private static void addDogTail(PartDefinition root) {
        PartDefinition base = root.addOrReplaceChild(
            "dog_tail",
            CubeListBuilder.create()
                .texOffs(20, 48)
                .addBox(-0.72F, 0.0F, -0.72F, 1.44F, 2.8F, 1.44F),
            PartPose.offsetAndRotation(0.0F, 8.0F, 1.95F, 0.30F, 0.0F, 0.0F)
        );

        PartDefinition mid1 = base.addOrReplaceChild(
            "mid1",
            CubeListBuilder.create()
                .texOffs(26, 48)
                .addBox(-0.62F, 0.0F, -0.62F, 1.24F, 2.7F, 1.24F),
            PartPose.offsetAndRotation(0.0F, 2.55F, 0.42F, -0.02F, 0.0F, 0.0F)
        );

        PartDefinition mid2 = mid1.addOrReplaceChild(
            "mid2",
            CubeListBuilder.create()
                .texOffs(32, 48)
                .addBox(-0.49F, 0.0F, -0.49F, 0.98F, 2.5F, 0.98F),
            PartPose.offsetAndRotation(0.0F, 2.45F, 0.25F, -0.08F, 0.0F, 0.0F)
        );

        mid2.addOrReplaceChild(
            "tip",
            CubeListBuilder.create()
                .texOffs(38, 48)
                .addBox(-0.34F, 0.0F, -0.34F, 0.68F, 2.1F, 0.68F),
            PartPose.offsetAndRotation(0.0F, 2.25F, 0.12F, -0.12F, 0.0F, 0.0F)
        );
    }

    private static void addFoxTail(PartDefinition root) {
        PartDefinition base = root.addOrReplaceChild(
            "fox_tail",
            CubeListBuilder.create()
                .texOffs(42, 48)
                .addBox(-1.05F, 0.0F, -0.92F, 2.10F, 3.0F, 1.84F),
            PartPose.offsetAndRotation(0.0F, 8.0F, 2.0F, 0.38F, 0.0F, 0.0F)
        );

        PartDefinition mid1 = base.addOrReplaceChild(
            "mid1",
            CubeListBuilder.create()
                .texOffs(50, 48)
                .addBox(-1.18F, 0.0F, -1.02F, 2.36F, 3.15F, 2.04F),
            PartPose.offsetAndRotation(0.0F, 2.65F, 0.60F, 0.17F, 0.0F, 0.0F)
        );

        PartDefinition mid2 = mid1.addOrReplaceChild(
            "mid2",
            CubeListBuilder.create()
                .texOffs(0, 56)
                .addBox(-1.08F, 0.0F, -0.94F, 2.16F, 3.10F, 1.88F),
            PartPose.offsetAndRotation(0.0F, 2.85F, 0.48F, 0.13F, 0.0F, 0.0F)
        );

        PartDefinition mid3 = mid2.addOrReplaceChild(
            "mid3",
            CubeListBuilder.create()
                .texOffs(8, 56)
                .addBox(-0.86F, 0.0F, -0.78F, 1.72F, 2.85F, 1.56F),
            PartPose.offsetAndRotation(0.0F, 2.82F, 0.38F, 0.09F, 0.0F, 0.0F)
        );

        mid3.addOrReplaceChild(
            "tip",
            CubeListBuilder.create()
                .texOffs(16, 56)
                .addBox(-0.56F, 0.0F, -0.52F, 1.12F, 2.35F, 1.04F),
            PartPose.offsetAndRotation(0.0F, 2.55F, 0.25F, 0.05F, 0.0F, 0.0F)
        );
    }

    private static void addDragonRidge(
        PartDefinition segment,
        String name,
        float width,
        float y,
        float z
    ) {
        segment.addOrReplaceChild(
            name,
            CubeListBuilder.create()
                .texOffs(54, 32)
                .addBox(-width / 2.0F, 0.0F, -0.20F, width, 1.15F, 0.40F),
            PartPose.offsetAndRotation(0.0F, y + 0.65F, -z - 1.05F, -0.16F, 0.0F, 0.0F)
        );
    }

}