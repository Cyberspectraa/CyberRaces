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
         * Connected fluffy tail chain. The vanilla cat tail is still the
         * proportion reference, but the middle grows slightly wider before
         * tapering again. Every piece is a child of the previous one, so no
         * animation can ever pull the tip away from the base.
         */
        PartDefinition tail = root.addOrReplaceChild(
            "tail",
            CubeListBuilder.create()
                .texOffs(0, 15)
                .addBox(-0.50F, 0.0F, -0.50F, 1.0F, 2.15F, 1.0F),
            PartPose.offsetAndRotation(0.0F, 8.0F, 2.0F, 0.68F, 0.0F, 0.0F)
        );

        PartDefinition catMid1 = tail.addOrReplaceChild(
            "mid1",
            CubeListBuilder.create()
                .texOffs(4, 15)
                .addBox(-0.62F, 0.0F, -0.62F, 1.24F, 2.35F, 1.24F),
            PartPose.offsetAndRotation(0.0F, 1.95F, 0.28F, 0.18F, 0.0F, 0.0F)
        );

        PartDefinition catMid2 = catMid1.addOrReplaceChild(
            "mid2",
            CubeListBuilder.create()
                .texOffs(0, 15)
                .addBox(-0.74F, 0.0F, -0.74F, 1.48F, 2.55F, 1.48F),
            PartPose.offsetAndRotation(0.0F, 2.12F, 0.24F, 0.12F, 0.0F, 0.0F)
        );

        PartDefinition catMid3 = catMid2.addOrReplaceChild(
            "mid3",
            CubeListBuilder.create()
                .texOffs(4, 15)
                .addBox(-0.64F, 0.0F, -0.64F, 1.28F, 2.35F, 1.28F),
            PartPose.offsetAndRotation(0.0F, 2.30F, 0.18F, 0.09F, 0.0F, 0.0F)
        );

        catMid3.addOrReplaceChild(
            "tip",
            CubeListBuilder.create()
                .texOffs(0, 15)
                .addBox(
                    -0.46F,
                    0.0F,
                    -0.46F,
                    0.92F,
                    2.05F,
                    0.92F,
                    new CubeDeformation(-0.01F)
                ),
            PartPose.offsetAndRotation(0.0F, 2.12F, 0.12F, 0.06F, 0.0F, 0.0F)
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

        PartDefinition tail = root.addOrReplaceChild(
            "tail",
            CubeListBuilder.create()
                .texOffs(9, 18)
                .addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.15F, 2.0F),
            PartPose.offsetAndRotation(0.0F, 8.0F, 2.0F, 0.56F, 0.0F, 0.0F)
        );

        PartDefinition dogMid1 = tail.addOrReplaceChild(
            "mid1",
            CubeListBuilder.create()
                .texOffs(9, 18)
                .addBox(-1.17F, 0.0F, -1.17F, 2.34F, 2.30F, 2.34F),
            PartPose.offsetAndRotation(0.0F, 1.92F, 0.34F, 0.10F, 0.0F, 0.0F)
        );

        PartDefinition dogMid2 = dogMid1.addOrReplaceChild(
            "mid2",
            CubeListBuilder.create()
                .texOffs(9, 18)
                .addBox(-1.22F, 0.0F, -1.22F, 2.44F, 2.25F, 2.44F),
            PartPose.offsetAndRotation(0.0F, 2.08F, 0.26F, 0.08F, 0.0F, 0.0F)
        );

        dogMid2.addOrReplaceChild(
            "tip",
            CubeListBuilder.create()
                .texOffs(9, 18)
                .addBox(-0.88F, 0.0F, -0.88F, 1.76F, 1.95F, 1.76F),
            PartPose.offsetAndRotation(0.0F, 2.02F, 0.18F, 0.04F, 0.0F, 0.0F)
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

        /*
         * Fox keeps the vanilla 4x9x5 tail as its maximum silhouette, but
         * splits that volume into a connected chain: narrow root, full middle,
         * tapered end. This avoids UV overflow and the inflated-shell look.
         */
        PartDefinition tail = root.addOrReplaceChild(
            "tail",
            CubeListBuilder.create()
                .texOffs(30, 0)
                .addBox(-1.55F, 0.0F, -1.95F, 3.10F, 1.70F, 3.90F),
            PartPose.offsetAndRotation(0.0F, 8.0F, 2.0F, 0.52F, 0.0F, 0.0F)
        );

        PartDefinition foxMid1 = tail.addOrReplaceChild(
            "mid1",
            CubeListBuilder.create()
                .texOffs(30, 0)
                .addBox(-1.82F, 0.0F, -2.24F, 3.64F, 1.95F, 4.48F),
            PartPose.offsetAndRotation(0.0F, 1.45F, 0.34F, 0.08F, 0.0F, 0.0F)
        );

        PartDefinition foxMid2 = foxMid1.addOrReplaceChild(
            "mid2",
            CubeListBuilder.create()
                .texOffs(30, 0)
                .addBox(-2.0F, 0.0F, -2.5F, 4.0F, 2.15F, 5.0F),
            PartPose.offsetAndRotation(0.0F, 1.70F, 0.26F, 0.07F, 0.0F, 0.0F)
        );

        PartDefinition foxMid3 = foxMid2.addOrReplaceChild(
            "mid3",
            CubeListBuilder.create()
                .texOffs(30, 0)
                .addBox(-1.78F, 0.0F, -2.18F, 3.56F, 1.90F, 4.36F),
            PartPose.offsetAndRotation(0.0F, 1.92F, 0.18F, 0.05F, 0.0F, 0.0F)
        );

        foxMid3.addOrReplaceChild(
            "tip",
            CubeListBuilder.create()
                .texOffs(30, 0)
                .addBox(-1.35F, 0.0F, -1.70F, 2.70F, 1.55F, 3.40F),
            PartPose.offsetAndRotation(0.0F, 1.68F, 0.10F, 0.03F, 0.0F, 0.0F)
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
