package com.humanoid.horror.client;

import com.humanoid.horror.entity.Creature2;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class Creature2Model<T extends Creature2>
        extends HierarchicalModel<T> {

    // =========================================================
    // MODEL PARTS
    // =========================================================

    private final ModelPart root;

    private final ModelPart head;
    private final ModelPart body;

    private final ModelPart leftArm;
    private final ModelPart rightArm;

    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Creature2Model(ModelPart root) {

        this.root = root;

        this.head =
                root.getChild("head");

        this.body =
                root.getChild("body");

        this.leftArm =
                root.getChild("left_arm");

        this.rightArm =
                root.getChild("right_arm");

        this.leftLeg =
                root.getChild("left_leg");

        this.rightLeg =
                root.getChild("right_leg");
    }

    // =========================================================
    // MODEL
    // =========================================================

    public static LayerDefinition createBodyLayer() {

        MeshDefinition meshdefinition =
                new MeshDefinition();

        PartDefinition partdefinition =
                meshdefinition.getRoot();

        // =====================================================
        // KAFA
        // =====================================================

        partdefinition.addOrReplaceChild(
                "head",

                CubeListBuilder.create()
                        .texOffs(0, 0)

                        // Ana kafa
                        .addBox(
                                -4.0F,
                                -8.0F,
                                -4.0F,
                                8.0F,
                                8.0F,
                                8.0F
                        )

                        // ÜST / OUTER SKIN
                        .texOffs(32, 0)
                        .addBox(
                                -4.0F,
                                -8.0F,
                                -4.0F,
                                8.0F,
                                8.0F,
                                8.0F,
                                new CubeDeformation(0.25F)
                        ),

                PartPose.offset(
                        0.0F,
                        0.0F,
                        0.0F
                )
        );

        // =====================================================
        // GÖVDE
        // =====================================================

        partdefinition.addOrReplaceChild(
                "body",

                CubeListBuilder.create()
                        .texOffs(16, 16)

                        // Ana gövde
                        .addBox(
                                -4.0F,
                                0.0F,
                                -2.0F,
                                8.0F,
                                12.0F,
                                4.0F
                        )

                        // ÜST / OUTER SKIN
                        .texOffs(16, 32)
                        .addBox(
                                -4.0F,
                                0.0F,
                                -2.0F,
                                8.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.25F)
                        ),

                PartPose.ZERO
        );

        // =====================================================
        // SOL KOL
        // =====================================================

        partdefinition.addOrReplaceChild(
                "left_arm",

                CubeListBuilder.create()
                        .texOffs(32, 48)

                        // Ana kol
                        .addBox(
                                -1.0F,
                                -2.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F
                        )

                        // ÜST / OUTER SKIN
                        .texOffs(48, 48)
                        .addBox(
                                -1.0F,
                                -2.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.25F)
                        ),

                PartPose.offset(
                        5.0F,
                        2.0F,
                        0.0F
                )
        );

        // =====================================================
        // SAĞ KOL
        // =====================================================

        partdefinition.addOrReplaceChild(
                "right_arm",

                CubeListBuilder.create()
                        .texOffs(40, 16)

                        // Ana kol
                        .addBox(
                                -3.0F,
                                -2.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F
                        )

                        // ÜST / OUTER SKIN
                        .texOffs(40, 32)
                        .addBox(
                                -3.0F,
                                -2.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.25F)
                        ),

                PartPose.offset(
                        -5.0F,
                        2.0F,
                        0.0F
                )
        );

        // =====================================================
        // SOL BACAK
        // =====================================================

        partdefinition.addOrReplaceChild(
                "left_leg",

                CubeListBuilder.create()
                        .texOffs(16, 48)

                        // Ana bacak
                        .addBox(
                                -2.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F
                        )

                        // ÜST / OUTER SKIN
                        .texOffs(0, 48)
                        .addBox(
                                -2.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.25F)
                        ),

                PartPose.offset(
                        2.0F,
                        12.0F,
                        0.0F
                )
        );

        // =====================================================
        // SAĞ BACAK
        // =====================================================

        partdefinition.addOrReplaceChild(
                "right_leg",

                CubeListBuilder.create()
                        .texOffs(0, 16)

                        // Ana bacak
                        .addBox(
                                -2.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F
                        )

                        // ÜST / OUTER SKIN
                        .texOffs(0, 32)
                        .addBox(
                                -2.0F,
                                0.0F,
                                -2.0F,
                                4.0F,
                                12.0F,
                                4.0F,
                                new CubeDeformation(0.25F)
                        ),

                PartPose.offset(
                        -2.0F,
                        12.0F,
                        0.0F
                )
        );

        // =====================================================
        // TEXTURE SIZE
        // =====================================================

        return LayerDefinition.create(
                meshdefinition,
                64,
                64
        );
    }

    // =========================================================
    // ANIMATION
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
        // KAFA DÖNÜŞÜ
        // =====================================================

        if (entity.isHeadSpinning()) {

            /*
             * Özel kafa döndürme sistemi korunuyor.
             */
            this.head.yRot =
                    (float) Math.toRadians(
                            entity.spinningHeadYaw
                    );

            this.head.xRot =
                    0.0F;

        } else {

            /*
             * Normal Minecraft kafa hareketi.
             */
            this.head.yRot =
                    netHeadYaw *
                    ((float) Math.PI / 180F);

            this.head.xRot =
                    headPitch *
                    ((float) Math.PI / 180F);
        }

        // =====================================================
        // KOLLAR
        // =====================================================

        /*
         * Normal yürüyüş animasyonu.
         */

        this.rightArm.xRot =
                (float) Math.cos(
                        limbSwing * 0.6662F + Math.PI
                )
                * 2.0F
                * limbSwingAmount
                * 0.5F;

        this.leftArm.xRot =
                (float) Math.cos(
                        limbSwing * 0.6662F
                )
                * 2.0F
                * limbSwingAmount
                * 0.5F;

        // =====================================================
        // BACAKLAR
        // =====================================================

        this.rightLeg.xRot =
                (float) Math.cos(
                        limbSwing * 0.6662F
                )
                * 1.4F
                * limbSwingAmount;

        this.leftLeg.xRot =
                (float) Math.cos(
                        limbSwing * 0.6662F + Math.PI
                )
                * 1.4F
                * limbSwingAmount;
    }

    // =========================================================
    // ROOT
    // =========================================================

    @Override
    public ModelPart root() {
        return this.root;
    }
}
