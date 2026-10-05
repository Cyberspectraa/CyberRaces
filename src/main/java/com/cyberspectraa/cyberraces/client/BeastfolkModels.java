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

        // Keep the exact vanilla Cat/Ocelot ear core, then build a very
        // light stepped fur silhouette around it.
        PartDefinition rightEar = root.addOrReplaceChild(
            "right_ear",
            CubeListBuilder.create()
                .texOffs(0, 10)
                .addBox(-0.5F, -1.0F, -1.0F, 1.0F, 1.0F, 2.0F, deformation),
            PartPose.offset(-2.0F, -8.0F, 0.0F)
        );
        addCatEarFluff(rightEar, true);

        PartDefinition leftEar = root.addOrReplaceChild(
            "left_ear",
            CubeListBuilder.create()
                .texOffs(6, 10)
                .addBox(-0.5F, -1.0F, -1.0F, 1.0F, 1.0F, 2.0F, deformation),
            PartPose.offset(2.0F, -8.0F, 0.0F)
        );
        addCatEarFluff(leftEar, false);

        // Vanilla Cat/Ocelot tail remains the core shape.
        PartDefinition tail1 = root.addOrReplaceChild(
            "tail1",
            CubeListBuilder.create()
                .texOffs(0, 15)
                .addBox(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, deformation),
            PartPose.offsetAndRotation(0.0F, 8.0F, 2.0F, 0.90F, 0.0F, 0.0F)
        );
        addCatTailFluff(tail1, false);

        PartDefinition tail2 = root.addOrReplaceChild(
            "tail2",
            CubeListBuilder.create()
                .texOffs(4, 15)
                .addBox(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F, tailTipDeformation),
            PartPose.offsetAndRotation(0.0F, 13.0F, 8.2F, 1.7278761F, 0.0F, 0.0F)
        );
        addCatTailFluff(tail2, true);

        return LayerDefinition.create(mesh, 64, 32);
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
        addWolfEarFluff(rightEar, true);

        PartDefinition leftEar = root.addOrReplaceChild(
            "left_ear",
            CubeListBuilder.create()
                .texOffs(16, 14)
                .addBox(-1.0F, -2.0F, -0.5F, 2.0F, 2.0F, 1.0F),
            PartPose.offset(2.0F, -8.0F, 0.5F)
        );
        addWolfEarFluff(leftEar, false);

        PartDefinition tail = root.addOrReplaceChild(
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
        addWolfTailFluff(tail);

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
        addFoxEarFluff(rightEar, true);

        PartDefinition leftEar = root.addOrReplaceChild(
            "left_ear",
            CubeListBuilder.create()
                .texOffs(15, 1)
                .addBox(-1.0F, -2.0F, -0.5F, 2.0F, 2.0F, 1.0F),
            PartPose.offset(2.8F, -8.0F, -0.15F)
        );
        addFoxEarFluff(leftEar, false);

        PartDefinition tail = root.addOrReplaceChild(
            "tail",
            CubeListBuilder.create()
                .texOffs(30, 0)
                .addBox(-2.0F, 0.0F, -2.5F, 4.0F, 9.0F, 5.0F),
            PartPose.offsetAndRotation(0.0F, 8.0F, 2.0F, 0.56F, 0.0F, 0.0F)
        );
        addFoxTailFluff(tail);

        return LayerDefinition.create(mesh, 48, 32);
    }

    private static void addCatEarFluff(PartDefinition ear, boolean right) {
        float side = right ? -1.0F : 1.0F;
        int u = right ? 0 : 6;

        ear.addOrReplaceChild(
            "outer_fur",
            CubeListBuilder.create()
                .texOffs(u, 10)
                .addBox(
                    -0.48F,
                    -1.0F,
                    -0.92F,
                    0.96F,
                    1.0F,
                    1.84F,
                    new CubeDeformation(0.12F)
                ),
            PartPose.offsetAndRotation(
                side * 0.22F,
                -0.28F,
                0.0F,
                0.0F,
                0.0F,
                side * -0.10F
            )
        );

        ear.addOrReplaceChild(
            "tip_fur",
            CubeListBuilder.create()
                .texOffs(u, 10)
                .addBox(
                    -0.34F,
                    -0.78F,
                    -0.72F,
                    0.68F,
                    0.78F,
                    1.44F,
                    new CubeDeformation(0.08F)
                ),
            PartPose.offsetAndRotation(
                side * 0.12F,
                -1.02F,
                0.0F,
                -0.05F,
                0.0F,
                side * -0.16F
            )
        );
    }

    private static void addWolfEarFluff(PartDefinition ear, boolean right) {
        float side = right ? -1.0F : 1.0F;

        ear.addOrReplaceChild(
            "outer_fur",
            CubeListBuilder.create()
                .texOffs(16, 14)
                .addBox(
                    -1.05F,
                    -1.45F,
                    -0.56F,
                    2.10F,
                    1.55F,
                    1.12F,
                    new CubeDeformation(0.14F)
                ),
            PartPose.offsetAndRotation(
                side * 0.24F,
                -0.78F,
                0.0F,
                0.02F,
                0.0F,
                side * -0.11F
            )
        );

        ear.addOrReplaceChild(
            "tip_fur",
            CubeListBuilder.create()
                .texOffs(16, 14)
                .addBox(
                    -0.68F,
                    -1.25F,
                    -0.48F,
                    1.36F,
                    1.30F,
                    0.96F,
                    new CubeDeformation(0.10F)
                ),
            PartPose.offsetAndRotation(
                side * 0.14F,
                -2.0F,
                0.0F,
                -0.03F,
                0.0F,
                side * -0.18F
            )
        );
    }

    private static void addFoxEarFluff(PartDefinition ear, boolean right) {
        float side = right ? -1.0F : 1.0F;
        int u = right ? 8 : 15;

        ear.addOrReplaceChild(
            "outer_fur",
            CubeListBuilder.create()
                .texOffs(u, 1)
                .addBox(
                    -1.12F,
                    -1.55F,
                    -0.56F,
                    2.24F,
                    1.65F,
                    1.12F,
                    new CubeDeformation(0.16F)
                ),
            PartPose.offsetAndRotation(
                side * 0.28F,
                -0.82F,
                0.0F,
                0.0F,
                0.0F,
                side * -0.13F
            )
        );

        ear.addOrReplaceChild(
            "mid_fur",
            CubeListBuilder.create()
                .texOffs(u, 1)
                .addBox(
                    -0.80F,
                    -1.34F,
                    -0.50F,
                    1.60F,
                    1.40F,
                    1.0F,
                    new CubeDeformation(0.12F)
                ),
            PartPose.offsetAndRotation(
                side * 0.16F,
                -1.92F,
                0.0F,
                -0.02F,
                0.0F,
                side * -0.19F
            )
        );

        ear.addOrReplaceChild(
            "tip_fur",
            CubeListBuilder.create()
                .texOffs(u, 1)
                .addBox(
                    -0.43F,
                    -0.95F,
                    -0.40F,
                    0.86F,
                    1.0F,
                    0.80F,
                    new CubeDeformation(0.08F)
                ),
            PartPose.offsetAndRotation(
                side * 0.08F,
                -3.0F,
                0.0F,
                -0.04F,
                0.0F,
                side * -0.25F
            )
        );
    }

    private static void addCatTailFluff(
        PartDefinition tail,
        boolean tipSection
    ) {
        tail.addOrReplaceChild(
            "fur_a",
            CubeListBuilder.create()
                .texOffs(tipSection ? 4 : 0, 15)
                .addBox(
                    -0.64F,
                    0.0F,
                    -0.64F,
                    1.28F,
                    3.25F,
                    1.28F,
                    new CubeDeformation(0.02F)
                ),
            PartPose.offset(0.0F, tipSection ? 0.65F : 1.1F, 0.48F)
        );

        tail.addOrReplaceChild(
            "fur_b",
            CubeListBuilder.create()
                .texOffs(tipSection ? 4 : 0, 15)
                .addBox(
                    -0.72F,
                    0.0F,
                    -0.72F,
                    1.44F,
                    2.75F,
                    1.44F,
                    new CubeDeformation(0.02F)
                ),
            PartPose.offset(0.0F, tipSection ? 3.2F : 3.8F, 0.52F)
        );

        tail.addOrReplaceChild(
            "fur_c",
            CubeListBuilder.create()
                .texOffs(tipSection ? 4 : 0, 15)
                .addBox(
                    -0.58F,
                    0.0F,
                    -0.58F,
                    1.16F,
                    2.0F,
                    1.16F,
                    new CubeDeformation(0.01F)
                ),
            PartPose.offset(0.0F, tipSection ? 5.65F : 6.1F, 0.45F)
        );
    }

    private static void addWolfTailFluff(PartDefinition tail) {
        addTailVolume(
            tail,
            "fur_base",
            9, 18,
            2.65F, 2.25F, 2.65F,
            0.0F, 0.35F, 0.0F,
            0.08F
        );
        addTailVolume(
            tail,
            "fur_mid_a",
            9, 18,
            3.10F, 2.65F, 3.05F,
            0.0F, 2.15F, 0.0F,
            0.09F
        );
        addTailVolume(
            tail,
            "fur_mid_b",
            9, 18,
            2.95F, 2.55F, 2.90F,
            0.0F, 4.35F, 0.0F,
            0.08F
        );
        addTailVolume(
            tail,
            "fur_tip",
            9, 18,
            2.30F, 2.20F, 2.30F,
            0.0F, 6.25F, 0.0F,
            0.05F
        );
    }

    private static void addFoxTailFluff(PartDefinition tail) {
        // Deliberately builds a bell-shaped silhouette: narrow root, very
        // full centre, then a smaller tip. This is the main visual trick
        // borrowed from the reference mod's layered fluffy tail idea.
        addTailVolume(
            tail,
            "fur_base",
            30, 0,
            4.70F, 2.50F, 5.55F,
            0.0F, 0.15F, 0.0F,
            0.08F
        );
        addTailVolume(
            tail,
            "fur_upper",
            30, 0,
            5.35F, 3.10F, 6.10F,
            0.0F, 1.95F, 0.0F,
            0.10F
        );
        addTailVolume(
            tail,
            "fur_middle",
            30, 0,
            5.70F, 3.45F, 6.45F,
            0.0F, 4.25F, 0.0F,
            0.11F
        );
        addTailVolume(
            tail,
            "fur_lower",
            30, 0,
            5.05F, 2.85F, 5.85F,
            0.0F, 6.55F, 0.0F,
            0.09F
        );
        addTailVolume(
            tail,
            "fur_tip",
            30, 0,
            4.10F, 1.90F, 4.85F,
            0.0F, 8.0F, 0.0F,
            0.06F
        );
    }

    private static void addTailVolume(
        PartDefinition parent,
        String name,
        int u,
        int v,
        float width,
        float height,
        float depth,
        float x,
        float y,
        float z,
        float deformation
    ) {
        parent.addOrReplaceChild(
            name,
            CubeListBuilder.create()
                .texOffs(u, v)
                .addBox(
                    -width / 2.0F,
                    0.0F,
                    -depth / 2.0F,
                    width,
                    height,
                    depth,
                    new CubeDeformation(deformation)
                ),
            PartPose.offset(x, y, z)
        );
    }
}
