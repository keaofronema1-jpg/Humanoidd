package com.humanoid.horror.client.renderer;

import com.humanoid.horror.client.model.ModelTakeCare;
import com.humanoid.horror.entity.Humanoid;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class TakeCareRenderer extends MobRenderer<Humanoid, ModelTakeCare<Humanoid>> {

    private static final ResourceLocation TEXTURE = 
            new ResourceLocation("humanoid", "textures/entity/texture_3.png");

    public TakeCareRenderer(EntityRendererProvider.Context context) {
        super(context, new ModelTakeCare<>(context.bakeLayer(ModelTakeCare.LAYER_LOCATION)), 0.5F);

        // İşte o "TakeCareRenderer$1" dediği katman tam olarak burası:
        this.addLayer(new RenderLayer<Humanoid, ModelTakeCare<Humanoid>>(this) {
            private final ResourceLocation LAYER_TEXTURE = 
                    new ResourceLocation("humanoid", "textures/entity/texture_3.png");

            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, 
                               Humanoid entity, float limbSwing, float limbSwingAmount, 
                               float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

                VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(LAYER_TEXTURE));
                
                // Model katmanının çizim hesaplamaları
                ModelTakeCare<Humanoid> layerModel = new ModelTakeCare<>(
                        Minecraft.getInstance().getEntityModels().bakeLayer(ModelTakeCare.LAYER_LOCATION)
                );

                layerModel.copyPropertiesTo(this.getParentModel());
                layerModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                
                int overlayCoords = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);
                layerModel.renderToBuffer(poseStack, vertexConsumer, packedLight, overlayCoords, 1.0F, 1.0F, 1.0F, 1.0F);
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(Humanoid entity) {
        return TEXTURE;
    }
}
