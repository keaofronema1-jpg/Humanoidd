package com.satansproject.sadsatan.client.model;

import com.satansproject.sadsatan.entity.helper;

import net.minecraft.client.animation.AnimationUtils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;


public class HelperModel extends EntityModel<helper> {

    // Entity renderer tarafından bake edilecek model layer
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    new ResourceLocation("satansproject", "helper"),
                    "main"
            );

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart left_arm;
    private final ModelPart right_arm;
    private final ModelPart left_leg;
    private final ModelPart right_leg;
    private final ModelPart bone;


    public HelperModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.left_arm = root.getChild("left_arm");
        this.right_arm = root.getChild("right_arm");
        this.left_leg = root.getChild("left_leg");
        this.right_leg = root.getChild("right_leg");
        this.bone = root.getChild("bone");
    }


    public static LayerDefinition createBodyLayer() {

        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();


        PartDefinition head = partdefinition.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 19)
                        .addBox(
                                -4.2653F,
                                -4.0044F,
                                -2.0F,
                                8.0F,
                                8.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(0.0F, -8.0F, 0.0F)
        );


        PartDefinition body = partdefinition.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(
                                -4.0F,
                                -3.0F,
                                -2.0F,
                                8.0F,
                                15.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(0.0F, 0.0F, 0.0F)
        );


        PartDefinition left_arm = partdefinition.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                        .texOffs(30, 35)
                        .addBox(
                                -1.0F,
                                5.0F,
                                -2.0F,
                                3.0F,
                                5.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        )
                        .texOffs(16, 35)
                        .addBox(
                                -1.0F,
                                -5.0F,
                                -2.0F,
                                3.0F,
                                10.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(5.0F, 2.0F, 0.0F)
        );


        PartDefinition right_arm = partdefinition.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                        .texOffs(24, 0)
                        .addBox(
                                -2.0F,
                                -5.0F,
                                -2.0F,
                                3.0F,
                                15.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(-5.0F, 2.0F, 0.0F)
        );


        PartDefinition left_leg = partdefinition.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                        .texOffs(24, 19)
                        .addBox(
                                -2.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(2.0F, 12.0F, 0.0F)
        );


        PartDefinition right_leg = partdefinition.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 31)
                        .addBox(
                                -2.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(-2.0F, 12.0F, 0.0F)
        );


        PartDefinition bone = partdefinition.addOrReplaceChild(
                "bone",
                CubeListBuilder.create()
                        .texOffs(38, 0)
                        .addBox(
                                -3.0F,
                                -1.0F,
                                -3.0F,
                                4.0F,
                                1.0F,
                                3.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(1.0F, -3.0F, 1.0F)
        );


        // Blockbench model texture atlası: 512 x 512
        return LayerDefinition.create(meshdefinition, 512, 512);
    }


    @Override
    public void setupAnim(
            helper entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {

        /*
         * Modeli her frame önce Blockbench'in temel pozisyonuna döndür.
         * Böylece önceki frame'den kalan animasyon değerleri kalmaz.
         */

        this.head.xRot = 0.0F;
        this.head.yRot = 0.0F;
        this.head.zRot = 0.0F;

        this.head.x = 0.0F;
        this.head.y = -8.0F;
        this.head.z = 0.0F;


        this.left_arm.xRot = 0.0F;
        this.left_arm.yRot = 0.0F;
        this.left_arm.zRot = 0.0F;

        this.left_arm.x = 5.0F;
        this.left_arm.y = 2.0F;
        this.left_arm.z = 0.0F;


        this.right_arm.xRot = 0.0F;
        this.right_arm.yRot = 0.0F;
        this.right_arm.zRot = 0.0F;

        this.right_arm.x = -5.0F;
        this.right_arm.y = 2.0F;
        this.right_arm.z = 0.0F;


        this.left_leg.xRot = 0.0F;
        this.left_leg.yRot = 0.0F;
        this.left_leg.zRot = 0.0F;

        this.left_leg.x = 2.0F;
        this.left_leg.y = 12.0F;
        this.left_leg.z = 0.0F;


        this.right_leg.xRot = 0.0F;
        this.right_leg.yRot = 0.0F;
        this.right_leg.zRot = 0.0F;

        this.right_leg.x = -2.0F;
        this.right_leg.y = 12.0F;
        this.right_leg.z = 0.0F;


        this.bone.xRot = 0.0F;
        this.bone.yRot = 0.0F;
        this.bone.zRot = 0.0F;

        this.bone.x = 1.0F;
        this.bone.y = -3.0F;
        this.bone.z = 1.0F;


        /*
         * YÜRÜMÜYORSA:
         *
         * Hiçbir walk animasyonu uygulanmaz.
         *
         * Böylece Helper yerinde dururken
         * Normalwalk veya Soulwalk çalışmaz.
         */

        if (limbSwingAmount <= 0.01F) {
            return;
        }


        /*
         * Şu anda Helper yürüyorsa Normalwalk oynuyor.
         *
         * Soulwalk bağlantısını helper.java oluşturduktan sonra
         * burada:
         *
         * entity.isSoulwalking()
         *
         * üzerinden bağlayacağız.
         */

        AnimationUtils.animateWalk(
                HelperAnimation.Normalwalk,
                limbSwing,
                limbSwingAmount,
                1.0F,
                1.0F
        );
    }


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

        left_arm.render(
                poseStack,
                vertexConsumer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );

        right_arm.render(
                poseStack,
                vertexConsumer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );

        left_leg.render(
                poseStack,
                vertexConsumer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );

        right_leg.render(
                poseStack,
                vertexConsumer,
                packedLight,
                packedOverlay,
                red,
                green,
                blue,
                alpha
        );

        bone.render(
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
