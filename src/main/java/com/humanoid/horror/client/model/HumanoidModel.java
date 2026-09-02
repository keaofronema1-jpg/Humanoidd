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
        // Şimdilik animasyon yok.
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
