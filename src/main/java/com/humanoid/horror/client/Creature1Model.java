package com.humanoid.horror.client;

import com.humanoid.horror.entity.Creature1;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class Creature1Model<T extends Creature1> extends HierarchicalModel<T> {

    private final ModelPart root;

    private final ModelPart body;
    private final ModelPart head;

    private final ModelPart rightArm;
    private final ModelPart leftArm;

    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public Creature1Model(ModelPart root) {
        this.root = root;

        this.body = root.getChild("body");
        this.head = root.getChild("head");

        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");

        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {

        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        /*
         * ============================================================
         * HEAD
         * ============================================================
         */

        PartDefinition head = root.addOrReplaceChild(
                "head",

                CubeListBuilder.create()
                        // Ana kafa
                        .texOffs(0, 0)
                        .addBox(
                                -4.0F,
                                -8.0F,
                                -4.0F,
                                8.0F,
                                8.0F,
                                8.0F
                        )

                        // Outer kafa katmanı
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

                PartPose.offset(0.0F, -12.0F, 0.0F)
        );

        /*
         * ============================================================
         * BODY
         * ============================================================
         */

        root.addOrReplaceChild(
                "body",

                CubeListBuilder.create()
                        // Ana gövde
                        .texOffs(16, 16)
                        .addBox(
                                -4.0F,
                                0.0F,
                                -2.0F,
                                8.0F,
                                12.0F,
                                4.0F
                        )

                        // Gövde outer katmanı
                        .texOffs(16, 32)
                        .addBox(
                                -4.0F,
                                0.0F,
                                -2.0F,
                                8.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.5F)
                        ),

                PartPose.offset(0.0F, -12.0F, 0.0F)
        );

        /*
         * ============================================================
         * RIGHT ARM
         * ============================================================
         */

        root.addOrReplaceChild(
                "right_arm",

                CubeListBuilder.create()
                        // Ana sağ kol
                        .texOffs(40, 16)
                        .addBox(
                                -3.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F
                        )

                        // Sağ kol outer katmanı
                        .texOffs(40, 32)
                        .addBox(
                                -3.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.5F)
                        ),

                PartPose.offset(-5.0F, -12.0F, 0.0F)
        );

        /*
         * ============================================================
         * LEFT ARM
         * ============================================================
         */

        root.addOrReplaceChild(
                "left_arm",

                CubeListBuilder.create()
                        // Ana sol kol
                        .texOffs(32, 48)
                        .addBox(
                                -1.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F
                        )

                        // Sol kol outer katmanı
                        .texOffs(48, 48)
                        .addBox(
                                -1.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.5F)
                        ),

                PartPose.offset(5.0F, -12.0F, 0.0F)
        );

        /*
         * ============================================================
         * RIGHT LEG
         * ============================================================
         */

        root.addOrReplaceChild(
                "right_leg",

                CubeListBuilder.create()
                        // Ana sağ bacak
                        .texOffs(0, 16)
                        .addBox(
                                -2.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F
                        )

                        // Sağ bacak outer katmanı
                        .texOffs(0, 32)
                        .addBox(
                                -2.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.5F)
                        ),

                PartPose.offset(-2.0F, 0.0F, 0.0F)
        );

        /*
         * ============================================================
         * LEFT LEG
         * ============================================================
         */

        root.addOrReplaceChild(
                "left_leg",

                CubeListBuilder.create()
                        // Ana sol bacak
                        .texOffs(16, 48)
                        .addBox(
                                -2.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F
                        )

                        // Sol bacak outer katmanı
                        .texOffs(0, 48)
                        .addBox(
                                -2.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.5F)
                        ),

                PartPose.offset(2.0F, 0.0F, 0.0F)
        );

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {

        /*
         * ============================================================
         * HEAD ANIMATION
         * ============================================================
         */

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);

        /*
         * ============================================================
         * WALK ANIMATION
         * ============================================================
         */

        float speed = 0.9F;
        float amount = Math.min(limbSwingAmount, 1.0F);

        this.rightLeg.xRot =
                Mth.cos(limbSwing * speed) * 1.2F * amount;

        this.leftLeg.xRot =
                Mth.cos(limbSwing * speed + (float) Math.PI)
                        * 1.2F
                        * amount;

        this.rightArm.xRot =
                Mth.cos(limbSwing * speed + (float) Math.PI)
                        * 1.0F
                        * amount;

        this.leftArm.xRot =
                Mth.cos(limbSwing * speed)
                        * 1.0F
                        * amount;
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
