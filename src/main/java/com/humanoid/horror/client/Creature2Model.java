package com.humanoid.horror.client;

import com.humanoid.horror.entity.Creature2;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class Creature2Model<T extends Creature2>
        extends HierarchicalModel<T> {

    private final ModelPart root;
    private final ModelPart head;

    public Creature2Model(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {

        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // =====================================================
        // KAFA
        // =====================================================

        partdefinition.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(
                                -4.0F,
                                -8.0F,
                                -4.0F,
                                8.0F,
                                8.0F,
                                8.0F
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
                        .addBox(
                                -4.0F,
                                0.0F,
                                -2.0F,
                                8.0F,
                                12.0F,
                                4.0F
                        ),
                PartPose.ZERO
        );

        return LayerDefinition.create(
                meshdefinition,
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

        if (entity.isHeadSpinning()) {

            // Sürekli kafa dönüşü
            this.head.yRot =
                    (float) Math.toRadians(
                            entity.spinningHeadYaw
                    );

            this.head.xRot = 0.0F;

        } else {

            // Normal kafa hareketi
            this.head.yRot =
                    netHeadYaw *
                    ((float) Math.PI / 180F);

            this.head.xRot =
                    headPitch *
                    ((float) Math.PI / 180F);
        }
    }

    // =========================================================
    // ROOT
    // =========================================================

    @Override
    public ModelPart root() {
        return this.root;
    }
        }
