package com.humanoid.horror.client.model;

import com.humanoid.horror.entity.Humanoid;
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

public class ModelTakeCare<T extends Humanoid> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            new ResourceLocation("humanoid", "model_take_care"), "main");

    public final ModelPart Head;
    public final ModelPart body;
    public final ModelPart body2;
    public final ModelPart bb_main;

    // 6 Bacak
    public final ModelPart[] legs = new ModelPart[6];

    // 36 Kol
    public final ModelPart[] arms = new ModelPart[36];

    public ModelTakeCare(ModelPart root) {
        this.bb_main = root.getChild("bb_main");
        this.body = root.getChild("body");
        this.body2 = root.getChild("body2");
        this.Head = root.getChild("Head");

        // Bacak Linkleme
        for (int i = 0; i < 6; i++) {
            this.legs[i] = root.getChild("leg" + i);
        }

        // Kol Linkleme
        for (int i = 0; i < 36; i++) {
            this.arms[i] = root.getChild("arm" + i);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 1. Kafa (Head)
        partdefinition.addOrReplaceChild("Head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -24.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        // 2. Gövde (body)
        partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16)
                        .addBox(-4.0F, -16.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                PartPose.ZERO);

        // 3. İkincil Kambur Yapı (body2)
        partdefinition.addOrReplaceChild("body2",
                CubeListBuilder.create()
                        .texOffs(16, 32)
                        .addBox(-5.0F, -18.0F, -3.0F, 10.0F, 6.0F, 6.0F),
                PartPose.ZERO);

        // 4. Alt Sinsi Merkez (bb_main)
        partdefinition.addOrReplaceChild("bb_main",
                CubeListBuilder.create()
                        .texOffs(0, 32)
                        .addBox(-6.0F, -4.0F, -6.0F, 12.0F, 4.0F, 12.0F),
                PartPose.ZERO);

        // 6 Bacak Enjeksiyonu
        for (int i = 0; i < 6; i++) {
            partdefinition.addOrReplaceChild("leg" + i,
                    CubeListBuilder.create()
                            .texOffs(0, 16)
                            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                    PartPose.ZERO);
        }

        // 36 Kol Enjeksiyonu
        for (int i = 0; i < 36; i++) {
            partdefinition.addOrReplaceChild("arm" + i,
                    CubeListBuilder.create()
                            .texOffs(40, 16)
                            .addBox(-1.0F, -2.0F, -1.0F, 2.0F, 14.0F, 2.0F),
                    PartPose.ZERO);
        }

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // Kafa rotasyonu
        this.Head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.Head.xRot = headPitch * ((float) Math.PI / 180F);

        // 6 Bacağın Hepsine Örümcek Yürüyüşü
        float legAnim = Mth.sin(limbSwing * 0.4F) * limbSwingAmount;
        for (int i = 0; i < 6; i++) {
            if (i % 2 == 0) {
                this.legs[i].xRot = legAnim;
            } else {
                this.legs[i].xRot = -legAnim;
            }
        }

        // 36 Kolun HEPSİNE Asenkron Dalgalanma Animasyonu
        for (int i = 0; i < 36; i++) {
            this.arms[i].zRot = Mth.sin(ageInTicks * 0.06F + (i * 0.15F)) * 0.15F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        // Temel parçalar
        this.bb_main.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.body2.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        this.Head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);

        // 6 Bacak Render
        for (ModelPart leg : this.legs) {
            leg.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }

        // 36 Kol Render
        for (ModelPart arm : this.arms) {
            arm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
}
