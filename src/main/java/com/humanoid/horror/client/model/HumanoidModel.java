package com.humanoid.horror.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class HumanoidModel<T extends Entity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    new ResourceLocation("humanoid", "humanoid"),
                    "main"
            );

    private final ModelPart humanoid;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public HumanoidModel(ModelPart root) {
        this.humanoid = root.getChild("Humanoid");
        this.head = this.humanoid.getChild("Head");
        this.body = this.humanoid.getChild("Body");
        this.leftArm = this.humanoid.getChild("LeftArm");
        this.rightLeg = this.humanoid.getChild("RightLeg");
        this.leftLeg = this.humanoid.getChild("LeftLeg");
    }

    public static LayerDefinition createBodyLayer() {

        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Humanoid = partdefinition.addOrReplaceChild(
                "Humanoid",
                CubeListBuilder.create()
                        .texOffs(0, 22)
                        .addBox(
                                6.0F,
                                -24.0F,
                                -2.0F,
                                2.0F,
                                18.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        )
                        .texOffs(0, 22)
                        .addBox(
                                4.0F,
                                -24.0F,
                                -2.0F,
                                2.0F,
                                18.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(0.0F, 24.0F, 0.0F)
        );

        PartDefinition Head = Humanoid.addOrReplaceChild(
                "Head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(
                                -4.0F,
                                -14.0F,
                                -4.0F,
                                8.0F,
                                14.0F,
                                8.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(0.0F, -24.0F, 0.0F)
        );

        PartDefinition Body = Humanoid.addOrReplaceChild(
                "Body",
                CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(
                                -4.0F,
                                -12.0F,
                                -2.0F,
                                8.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(0.0F, -12.0F, 0.0F)
        );

        PartDefinition LeftArm = Humanoid.addOrReplaceChild(
                "LeftArm",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(
                                -2.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                18.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(-6.0F, -24.0F, 0.0F)
        );

        PartDefinition RightLeg = Humanoid.addOrReplaceChild(
                "RightLeg",
                CubeListBuilder.create()
                        .texOffs(32, 22)
                        .addBox(
                                -2.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(2.0F, -12.0F, 0.0F)
        );

        PartDefinition LeftLeg = Humanoid.addOrReplaceChild(
                "LeftLeg",
                CubeListBuilder.create()
                        .texOffs(48, 22)
                        .addBox(
                                -2.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(-2.0F, -12.0F, 0.0F)
        );

        return LayerDefinition.create(
                meshdefinition,
                128,
                128
        );
    }

    @Override
    public void setupAnim(
            Entity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {

        /*
         * ============================================================
         * RESET
         * ============================================================
         */

        this.head.xRot = 0.0F;
        this.head.yRot = 0.0F;
        this.head.zRot = 0.0F;

        this.body.xRot = 0.0F;
        this.body.yRot = 0.0F;
        this.body.zRot = 0.0F;

        this.leftArm.xRot = 0.0F;
        this.leftArm.yRot = 0.0F;
        this.leftArm.zRot = 0.0F;

        this.rightLeg.xRot = 0.0F;
        this.rightLeg.yRot = 0.0F;
        this.rightLeg.zRot = 0.0F;

        this.leftLeg.xRot = 0.0F;
        this.leftLeg.yRot = 0.0F;
        this.leftLeg.zRot = 0.0F;


        /*
         * ============================================================
         * HEAD LOOK
         * ============================================================
         */

        this.head.yRot =
                netHeadYaw * Mth.DEG_TO_RAD;

        this.head.xRot =
                headPitch * Mth.DEG_TO_RAD;


        /*
         * ============================================================
         * IDLE
         * ============================================================
         */

        float idle =
                ageInTicks * 0.08F;

        this.body.xRot +=
                Mth.sin(idle) * 0.025F;

        this.head.zRot +=
                Mth.sin(idle * 0.7F) * 0.015F;

        this.leftArm.zRot +=
                Mth.sin(idle) * 0.015F;


        /*
         * ============================================================
         * WALK / RUN
         * ============================================================
         */

        float movement =
                Mth.clamp(
                        limbSwingAmount,
                        0.0F,
                        1.0F
                );

        boolean running =
                movement > 0.65F;

        float swingSpeed;
        float swingPower;

        if (running) {
            swingSpeed = 0.95F;
            swingPower = 1.65F;
        } else {
            swingSpeed = 0.6662F;
            swingPower = 1.4F;
        }

        float walkCycle =
                limbSwing * swingSpeed;


        /*
         * ============================================================
         * LEGS
         * ============================================================
         */

        this.rightLeg.xRot =
                Mth.cos(walkCycle)
                        * swingPower
                        * movement;

        this.leftLeg.xRot =
                Mth.cos(
                        walkCycle + Mth.PI
                )
                        * swingPower
                        * movement;


        /*
         * ============================================================
         * SINGLE ARM
         * ============================================================
         *
         * Bu modelde yalnızca LeftArm var.
         * O yüzden mevcut kolu doğal şekilde sallıyoruz.
         */

        this.leftArm.xRot =
                Mth.cos(walkCycle + Mth.PI)
                        * swingPower
                        * 0.75F
                        * movement;


        /*
         * ============================================================
         * RUN
         * ============================================================
         */

        if (running) {

            this.body.xRot +=
                    0.16F;

            this.head.xRot -=
                    0.08F;

            this.leftArm.xRot *=
                    1.20F;

            this.rightLeg.xRot *=
                    1.10F;

            this.leftLeg.xRot *=
                    1.10F;
        }


        /*
         * ============================================================
         * ATTACK
         * ============================================================
         */

        if (this.attackTime > 0.0F) {

            float attackProgress =
                    this.attackTime;

            float attackSwing =
                    Mth.sin(
                            attackProgress * Mth.PI
                    );

            /*
             * Gövde hafifçe öne.
             */
            this.body.xRot -=
                    attackSwing * 0.25F;

            /*
             * Mevcut kolu saldırı pozisyonuna getir.
             */
            this.leftArm.xRot -=
                    attackSwing * 2.0F;

            this.leftArm.zRot +=
                    attackSwing * 0.15F;

            /*
             * Kafa da saldırıya eşlik ediyor.
             */
            this.head.xRot -=
                    attackSwing * 0.15F;
        }


        /*
         * ============================================================
         * HORROR SWAY
         * ============================================================
         */

        if (
                movement > 0.05F
                        && this.attackTime <= 0.0F
        ) {

            float sway =
                    Mth.sin(
                            limbSwing * 0.45F
                    ) * movement;

            this.body.zRot +=
                    sway * 0.035F;

            this.head.zRot -=
                    sway * 0.05F;
        }
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
        humanoid.render(
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
