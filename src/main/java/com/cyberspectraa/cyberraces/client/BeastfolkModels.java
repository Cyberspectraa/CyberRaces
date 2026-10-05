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

        PartDefinition rightEar = root.addOrReplaceChild(
            "right_ear",
            CubeListBuilder.create()
                .texOffs(0, 10)
                .addBox(-0.5F, -1.0F, -1.0F, 1.0F, 1.0F, 2.0F),
            PartPose.offset(-2.0F, -8.0F, 0.0F)
        );
        addCatEarTuft(rightEar, true);

        PartDefinition leftEar = root.addOrReplaceChild(
            "left_ear",
            CubeListBuilder.create()
                .texOffs(6, 10)
                .addBox(-0.5F, -1.0F, -1.0F, 1.0F, 1.0F, 2.0F),
            PartPose.offset(2.0F, -8.0F, 0.0F)
        );
        addCatEarTuft(leftEar, false);

        /*
         * Use the exact two vanilla Cat/Ocelot tail boxes and UVs.
         * Vanilla renders the pieces separately; we parent tail2 to tail1
         * using the equivalent local transform so it cannot disconnect.
         */
        PartDefinition tail = root.addOrReplaceChild(
            "tail",
            CubeListBuilder.create()
                .texOffs(0, 15)
                .addBox(-0.5F, 0.0F, 0.0F, 1.0F, 8.0F, 1.0F),
            PartPose.offsetAndRotation(0.0F, 8.0F, 2.0F, 0.90F, 0.0F, 0.0F)
        );

        tail.addOrReplaceChild(
            "tail2",
            CubeListBuilder.create()
                .texOffs(4, 15)
                .addBox(
                    -0.5F,
                    0.0F,
                    0.0F,
                    1.0F,
                    8.0F,
                    1.0F,
                    new CubeDeformation(-0.02F)
                ),
            PartPose.offsetAndRotation(
                0.0F,
                7.965F,
                -0.062F,
                0.8278761F,
                0.0F,
                0.0F
            )
        );

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
        addWolfEarTuft(rightEar, true);

        PartDefinition leftEar = root.addOrReplaceChild(
            "left_ear",
            CubeListBuilder.create()
                .texOffs(16, 14)
                .addBox(-1.0F, -2.0F, -0.5F, 2.0F, 2.0F, 1.0F),
            PartPose.offset(2.0F, -8.0F, 0.5F)
        );
        addWolfEarTuft(leftEar, false);

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
        addFoxEarTuft(rightEar, true);

        PartDefinition leftEar = root.addOrReplaceChild(
            "left_ear",
            CubeListBuilder.create()
                .texOffs(15, 1)
                .addBox(-1.0F, -2.0F, -0.5F, 2.0F, 2.0F, 1.0F),
            PartPose.offset(2.8F, -8.0F, -0.15F)
        );
        addFoxEarTuft(leftEar, false);

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

    private static void addCatEarTuft(PartDefinition ear, boolean right) {
        float side = right ? -1.0F : 1.0F;
        int u = right ? 0 : 6;

        ear.addOrReplaceChild(
            "tuft",
            CubeListBuilder.create()
                .texOffs(u, 10)
                .addBox(
                    -0.28F,
                    -0.72F,
                    -0.62F,
                    0.56F,
                    0.72F,
                    1.24F,
                    new CubeDeformation(0.01F)
                ),
            PartPose.offsetAndRotation(
                side * 0.38F,
                -0.88F,
                0.0F,
                0.0F,
                0.0F,
                side * -0.20F
            )
        );
    }

    private static void addWolfEarTuft(PartDefinition ear, boolean right) {
        float side = right ? -1.0F : 1.0F;

        ear.addOrReplaceChild(
            "tuft",
            CubeListBuilder.create()
                .texOffs(16, 14)
                .addBox(
                    -0.40F,
                    -0.95F,
                    -0.38F,
                    0.80F,
                    0.95F,
                    0.76F,
                    new CubeDeformation(0.01F)
                ),
            PartPose.offsetAndRotation(
                side * 0.82F,
                -1.62F,
                0.0F,
                0.0F,
                0.0F,
                side * -0.22F
            )
        );
    }

    private static void addFoxEarTuft(PartDefinition ear, boolean right) {
        float side = right ? -1.0F : 1.0F;
        int u = right ? 8 : 15;

        ear.addOrReplaceChild(
            "outer_tuft",
            CubeListBuilder.create()
                .texOffs(u, 1)
                .addBox(
                    -0.46F,
                    -1.10F,
                    -0.40F,
                    0.92F,
                    1.10F,
                    0.80F,
                    new CubeDeformation(0.01F)
                ),
            PartPose.offsetAndRotation(
                side * 0.78F,
                -1.56F,
                0.0F,
                0.0F,
                0.0F,
                side * -0.24F
            )
        );

        ear.addOrReplaceChild(
            "tip_tuft",
            CubeListBuilder.create()
                .texOffs(u, 1)
                .addBox(
                    -0.30F,
                    -0.72F,
                    -0.32F,
                    0.60F,
                    0.72F,
                    0.64F,
                    new CubeDeformation(0.01F)
                ),
            PartPose.offsetAndRotation(
                side * 0.26F,
                -2.20F,
                0.0F,
                0.0F,
                0.0F,
                side * -0.18F
            )
        );
    }
}
