package com.humanoid.horror.client.model;

import com.humanoid.horror.entity.Creature3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.CubeDeformation;

import net.minecraft.resources.ResourceLocation;

public class Creature3Model<T extends Creature3> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    new ResourceLocation("humanoid", "creature3"),
                    "main"
            );

    private final ModelPart root;

    // ANA KATMAN
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    // OUTER / ÜST KATMAN
    private final ModelPart bodyOuter;
    private final ModelPart headOuter;
    private final ModelPart leftArmOuter;
    private final ModelPart rightArmOuter;
    private final ModelPart leftLegOuter;
    private final ModelPart rightLegOuter;

    public Creature3Model(ModelPart root) {

        this.root = root;

        // =====================================================
        // ANA KATMAN
        // =====================================================

        this.body = root.getChild("body");
        this.head = root.getChild("head");

        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");

        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");

        // =====================================================
        // OUTER KATMAN
        // =====================================================

        this.bodyOuter = root.getChild("body_outer");
        this.headOuter = root.getChild("head_outer");

        this.leftArmOuter = root.getChild("left_arm_outer");
        this.rightArmOuter = root.getChild("right_arm_outer");

        this.leftLegOuter = root.getChild("left_leg_outer");
        this.rightLegOuter = root.getChild("right_leg_outer");
    }

    // =========================================================
    // MODEL OLUŞTUR
    // =========================================================

    public static LayerDefinition createBodyLayer() {

        MeshDefinition mesh =
                new MeshDefinition();

        PartDefinition root =
                mesh.getRoot();

        // =====================================================
        // ANA GÖVDE
        // =====================================================

        root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(16, 16)
                        .addBox(
                                -4.0F,
                                0.0F,
                                -2.0F,
                                8.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.ZERO
        );

        // =====================================================
        // ANA KAFA
        // =====================================================

        root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(
                                -4.0F,
                                -8.0F,
                                -4.0F,
                                8.0F,
                                8.0F,
                                8.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.ZERO
        );

        // =====================================================
        // ANA SOL KOL
        // =====================================================

        root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16)
                        .addBox(
                                -1.0F,
                                -1.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(
                        5.0F,
                        1.0F,
                        0.0F
                )
        );

        // =====================================================
        // ANA SAĞ KOL
        // =====================================================

        root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16)
                        .mirror()
                        .addBox(
                                -3.0F,
                                -1.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(
                        -5.0F,
                        1.0F,
                        0.0F
                )
        );

        // =====================================================
        // ANA SOL BACAK
        // =====================================================

        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(
                                -2.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(
                        2.0F,
                        12.0F,
                        0.0F
                )
        );

        // =====================================================
        // ANA SAĞ BACAK
        // =====================================================

        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .mirror()
                        .addBox(
                                -2.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(
                        -2.0F,
                        12.0F,
                        0.0F
                )
        );

        // =====================================================
        // OUTER KAFA
        // =====================================================

        root.addOrReplaceChild(
                "head_outer",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(
                                -4.0F,
                                -8.0F,
                                -4.0F,
                                8.0F,
                                8.0F,
                                8.0F,
                                new CubeDeformation(0.5F)
                        ),
                PartPose.ZERO
        );

        // =====================================================
        // OUTER GÖVDE
        // =====================================================

        root.addOrReplaceChild(
                "body_outer",
                CubeListBuilder.create()
                        .texOffs(16, 16)
                        .addBox(
                                -4.0F,
                                0.0F,
                                -2.0F,
                                8.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.5F)
                        ),
                PartPose.ZERO
        );

        // =====================================================
        // OUTER SOL KOL
        // =====================================================

        root.addOrReplaceChild(
                "left_arm_outer",
                CubeListBuilder.create()
                        .texOffs(40, 16)
                        .addBox(
                                -1.0F,
                                -1.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.5F)
                        ),
                PartPose.offset(
                        5.0F,
                        1.0F,
                        0.0F
                )
        );

        // =====================================================
        // OUTER SAĞ KOL
        // =====================================================

        root.addOrReplaceChild(
                "right_arm_outer",
                CubeListBuilder.create()
                        .texOffs(40, 16)
                        .mirror()
                        .addBox(
                                -3.0F,
                                -1.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.5F)
                        ),
                PartPose.offset(
                        -5.0F,
                        1.0F,
                        0.0F
                )
        );

        // =====================================================
        // OUTER SOL BACAK
        // =====================================================

        root.addOrReplaceChild(
                "left_leg_outer",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(
                                -2.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.5F)
                        ),
                PartPose.offset(
                        2.0F,
                        12.0F,
                        0.0F
                )
        );

        // =====================================================
        // OUTER SAĞ BACAK
        // =====================================================

        root.addOrReplaceChild(
                "right_leg_outer",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .mirror()
                        .addBox(
                                -2.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.5F)
                        ),
                PartPose.offset(
                        -2.0F,
                        12.0F,
                        0.0F
                )
        );

        return LayerDefinition.create(
                mesh,
                64,
                64
        );
    }

    // =========================================================
    // ANİMASYON
    // =========================================================

    @Override
    public void setupAnim(
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {

        // =====================================================
        // KAFA
        // =====================================================

        float headYaw =
                netHeadYaw *
                        ((float) Math.PI / 180F);

        float headPitchRadians =
                headPitch *
                        ((float) Math.PI / 180F);

        head.yRot = headYaw;
        head.xRot = headPitchRadians;

        // Outer kafa ana kafayla aynı hareketi yapmalı
        headOuter.yRot = headYaw;
        headOuter.xRot = headPitchRadians;

        // =====================================================
        // YÜRÜME ANİMASYONU
        // =====================================================

        float swing =
                (float) Math.cos(
                        limbSwing * 0.6662F
                )
                        * 1.4F
                        * limbSwingAmount;

        leftArm.xRot = swing;
        rightArm.xRot = -swing;

        leftLeg.xRot = -swing;
        rightLeg.xRot = swing;

        // Outer kollar
        leftArmOuter.xRot = swing;
        rightArmOuter.xRot = -swing;

        // Outer bacaklar
        leftLegOuter.xRot = -swing;
        rightLegOuter.xRot = swing;
    }

    // =========================================================
    // RENDER
    // =========================================================

    @Override
    public void renderToBuffer(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {

        /*
         * Önce ana model.
         */

        body.render(
                poseStack,
                vertexConsumer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );

        head.render(
                poseStack,
                vertexConsumer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );

        leftArm.render(
                poseStack,
                vertexConsumer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );

        rightArm.render(
                poseStack,
                vertexConsumer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );

        leftLeg.render(
                poseStack,
                vertexConsumer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );

        rightLeg.render(
                poseStack,
                vertexConsumer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );

        /*
         * Sonra dış/outer skin.
         */

        bodyOuter.render(
                poseStack,
                vertexConsumer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );

        headOuter.render(
                poseStack,
                vertexConsumer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );

        leftArmOuter.render(
                poseStack,
                vertexConsumer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );

        rightArmOuter.render(
                poseStack,
                vertexConsumer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );

        leftLegOuter.render(
                poseStack,
                vertexConsumer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );

        rightLegOuter.render(
                poseStack,
                vertexConsumer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );
    }
}
