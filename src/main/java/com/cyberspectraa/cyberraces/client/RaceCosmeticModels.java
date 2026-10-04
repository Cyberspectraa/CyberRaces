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

        addEarPair(root, "elf_short", 3.0F, 2.5F, -0.10F, -0.15F);
        addEarPair(root, "elf_long", 5.0F, 2.5F, -0.14F, -0.20F);
        addEarPair(root, "elf_high", 4.0F, 2.5F, -0.28F, -0.42F);

        addEarPair(root, "halfling_round", 2.4F, 3.0F, -0.04F, -0.06F);
        addEarPair(root, "halfling_soft", 2.8F, 2.8F, -0.10F, -0.12F);
        addEarPair(root, "halfling_pointed", 3.4F, 2.3F, -0.18F, -0.25F);

        addEarPair(root, "orc_ears", 3.0F, 2.2F, -0.10F, -0.12F);

        addEarPair(root, "goblin_wide", 5.2F, 3.0F, 0.00F, -0.04F);
        addEarPair(root, "goblin_long", 6.2F, 2.7F, -0.10F, -0.12F);
        addEarPair(root, "goblin_swept", 5.6F, 2.6F, 0.12F, 0.22F);

        addEarPair(root, "fairy_classic", 2.8F, 2.0F, -0.18F, -0.20F);
        addEarPair(root, "fairy_sharp", 3.8F, 1.8F, -0.28F, -0.32F);
        addEarPair(root, "fairy_soft", 2.5F, 2.4F, -0.08F, -0.10F);

        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition createHardLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        addTusks(root, "tusks_small", 1.8F, 0.8F);
        addTusks(root, "tusks_broad", 2.6F, 1.0F);
        addTusks(root, "tusks_long", 3.6F, 0.9F);

        addHornPair(root, "horns_curved", 3.8F, 0.18F, 0.18F);
        addHornPair(root, "horns_swept", 4.2F, -0.22F, 0.36F);
        addHornPair(root, "horns_tall", 5.2F, 0.02F, 0.04F);

        root.addOrReplaceChild(
            "tiefling_tail",
            CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 5.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, 9.0F, 2.1F, 0.55F, 0.0F, 0.0F)
        ).addOrReplaceChild(
            "tip",
            CubeListBuilder.create().texOffs(0, 0).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 5.0F, 1.5F),
            PartPose.offsetAndRotation(0.0F, 4.4F, 0.7F, 0.55F, 0.0F, 0.0F)
        );

        root.addOrReplaceChild(
            "dragon_snout",
            CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.4F, -7.0F, 6.0F, 3.0F, 3.0F),
            PartPose.ZERO
        );

        addHornPair(root, "dragon_horned", 3.5F, 0.05F, 0.10F);
        addHornPair(root, "dragon_crowned", 4.5F, 0.28F, 0.04F);
        addHornPair(root, "dragon_swept", 4.0F, -0.25F, 0.38F);

        root.addOrReplaceChild(
            "dragon_tail",
            CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 6.0F, 3.0F),
            PartPose.offsetAndRotation(0.0F, 8.5F, 2.0F, 0.62F, 0.0F, 0.0F)
        ).addOrReplaceChild(
            "tip",
            CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F),
            PartPose.offsetAndRotation(0.0F, 5.2F, 0.8F, 0.48F, 0.0F, 0.0F)
        );

        return LayerDefinition.create(mesh, 16, 16);
    }

    private static void addEarPair(
        PartDefinition root,
        String name,
        float length,
        float height,
        float pitch,
        float roll
    ) {
        root.addOrReplaceChild(
            name + "_left",
            CubeListBuilder.create().texOffs(0, 8).addBox(0.0F, -height / 2.0F, -0.6F, length, height, 1.2F),
            PartPose.offsetAndRotation(3.8F, -4.8F, 0.0F, pitch, 0.0F, roll)
        );

        root.addOrReplaceChild(
            name + "_right",
            CubeListBuilder.create().texOffs(0, 8).mirror().addBox(-length, -height / 2.0F, -0.6F, length, height, 1.2F).mirror(false),
            PartPose.offsetAndRotation(-3.8F, -4.8F, 0.0F, pitch, 0.0F, -roll)
        );
    }

    private static void addTusks(PartDefinition root, String name, float length, float width) {
        root.addOrReplaceChild(
            name + "_left",
            CubeListBuilder.create().texOffs(0, 0).addBox(-width / 2.0F, -length, -width / 2.0F, width, length, width),
            PartPose.offsetAndRotation(1.8F, -0.3F, -4.3F, -0.12F, 0.0F, 0.0F)
        );

        root.addOrReplaceChild(
            name + "_right",
            CubeListBuilder.create().texOffs(0, 0).addBox(-width / 2.0F, -length, -width / 2.0F, width, length, width),
            PartPose.offsetAndRotation(-1.8F, -0.3F, -4.3F, -0.12F, 0.0F, 0.0F)
        );
    }

    private static void addHornPair(PartDefinition root, String name, float height, float pitch, float roll) {
        root.addOrReplaceChild(
            name + "_left",
            CubeListBuilder.create().texOffs(0, 0).addBox(-0.9F, -height, -0.9F, 1.8F, height, 1.8F),
            PartPose.offsetAndRotation(2.6F, -7.2F, 0.8F, pitch, 0.0F, roll)
        );

        root.addOrReplaceChild(
            name + "_right",
            CubeListBuilder.create().texOffs(0, 0).addBox(-0.9F, -height, -0.9F, 1.8F, height, 1.8F),
            PartPose.offsetAndRotation(-2.6F, -7.2F, 0.8F, pitch, 0.0F, -roll)
        );
    }
}
