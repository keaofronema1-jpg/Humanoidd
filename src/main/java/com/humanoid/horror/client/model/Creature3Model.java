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

public class Creature3Model<T extends Entity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    new ResourceLocation("humanoid", "creature3"),
                    "main"
            );

    private final ModelPart root;
    private final ModelPart waist;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public Creature3Model(ModelPart root) {
        this.root = root.getChild("root");
        this.waist = this.root.getChild("waist");
        this.body = this.waist.getChild("body");
        this.head = this.body.getChild("head");
        this.hat = this.head.getChild("hat");
        this.rightArm = this.body.getChild("rightArm");
        this.leftArm = this.body.getChild("leftArm");
        this.rightLeg = this.waist.getChild("rightLeg");
        this.leftLeg = this.waist.getChild("leftLeg");
    }

    public static LayerDefinition createBodyLayer() {

        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild(
                "root",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F)
        );

        PartDefinition waist = root.addOrReplaceChild(
                "waist",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, -12.0F, 0.0F)
        );

        PartDefinition body = waist.addOrReplaceChild(
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
                PartPose.offset(0.0F, -12.0F, 0.0F)
        );

        PartDefinition head = body.addOrReplaceChild(
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
                PartPose.offset(0.0F, 0.0F, 0.0F)
        );

        PartDefinition hat = head.addOrReplaceChild(
                "hat",
                CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(
                                -4.0F,
                                -8.0F,
                                -4.0F,
                                8.0F,
                                8.0F,
                                8.0F,
                                new CubeDeformation(0.5F)
                        ),
                PartPose.offset(0.0F, 0.0F, 0.0F)
        );

        PartDefinition rightArm = body.addOrReplaceChild(
                "rightArm",
                CubeListBuilder.create()
                        .texOffs(40, 16)
                        .addBox(
                                -3.0F,
                                -2.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(-5.0F, 2.0F, 0.0F)
        );

        PartDefinition leftArm = body.addOrReplaceChild(
                "leftArm",
                CubeListBuilder.create()
                        .texOffs(32, 48)
                        .addBox(
                                -1.0F,
                                -2.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(5.0F, 2.0F, 0.0F)
        );

        PartDefinition rightLeg = waist.addOrReplaceChild(
                "rightLeg",
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
                PartPose.offset(-1.9F, 0.0F, 0.0F)
        );

        PartDefinition leftLeg = waist.addOrReplaceChild(
                "leftLeg",
                CubeListBuilder.create()
                        .texOffs(16, 48)
                        .addBox(
                                -2.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.0F)
                        ),
                PartPose.offset(1.9F, 0.0F, 0.0F)
        );

        return LayerDefinition.create(
                meshdefinition,
                64,
                64
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

        this.waist.xRot = 0.0F;
        this.waist.yRot = 0.0F;
        this.waist.zRot = 0.0F;

        this.rightArm.xRot = 0.0F;
        this.rightArm.yRot = 0.0F;
        this.rightArm.zRot = 0.0F;

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

        this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        this.head.xRot = headPitch * Mth.DEG_TO_RAD;


        /*
         * ============================================================
         * IDLE
         * ============================================================
         */

        float idle = ageInTicks * 0.08F;

        this.body.xRot +=
                Mth.sin(idle) * 0.025F;

        this.head.zRot +=
                Mth.sin(idle * 0.7F) * 0.015F;

        this.rightArm.zRot +=
                Mth.sin(idle) * 0.01F;

        this.leftArm.zRot -=
                Mth.sin(idle) * 0.01F;


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

        boolean running = movement > 0.65F;

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
         * Bacaklar
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
         * Kollar
         */

        this.rightArm.xRot =
                Mth.cos(
                        walkCycle + Mth.PI
                )
                        * swingPower
                        * 0.75F
                        * movement;

        this.leftArm.xRot =
                Mth.cos(walkCycle)
                        * swingPower
                        * 0.75F
                        * movement;


        /*
         * ============================================================
         * RUN
         * ============================================================
         */

        if (running) {

            this.body.xRot += 0.16F;

            this.head.xRot -= 0.08F;

            this.rightArm.xRot *= 1.20F;
            this.leftArm.xRot *= 1.20F;

            this.rightLeg.xRot *= 1.10F;
            this.leftLeg.xRot *= 1.10F;
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

            this.body.xRot -=
                    attackSwing * 0.25F;

            this.rightArm.xRot -=
                    attackSwing * 2.0F;

            this.leftArm.xRot -=
                    attackSwing * 0.75F;

            this.rightArm.zRot +=
                    attackSwing * 0.12F;

            this.leftArm.zRot -=
                    attackSwing * 0.12F;

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

            float horrorSway =
                    Mth.sin(
                            limbSwing * 0.45F
                    ) * movement;

            this.body.zRot +=
                    horrorSway * 0.035F;

            this.head.zRot -=
                    horrorSway * 0.05F;
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
        root.render(
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
