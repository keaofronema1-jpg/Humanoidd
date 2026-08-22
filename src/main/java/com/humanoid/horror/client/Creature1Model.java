package com.humanoid.horror.client;

import com.humanoid.horror.entity.Creature1;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class Creature1Model<T extends Creature1> extends HierarchicalModel<T> {

    private final ModelPart root;

    public Creature1Model(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 2D Billboard / Düz Model Yapısı
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-8.0F, -24.0F, 0.0F, 16.0F, 24.0F, 0.0F), PartPose.ZERO);

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // İsteğe bağlı model animasyon değerleri
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
