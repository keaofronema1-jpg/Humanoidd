package com.humanoid.horror.client.renderer;

import com.humanoid.horror.client.model.HumanoidModel;
import com.humanoid.horror.entity.Humanoid;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HumanoidRenderer extends MobRenderer<Humanoid, HumanoidModel<Humanoid>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(
                    "humanoid",
                    "textures/entity/humanoid.png"
            );

    public HumanoidRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new HumanoidModel<>(
                        context.bakeLayer(
                                HumanoidModel.LAYER_LOCATION
                        )
                ),
                0.5F
        );
    }

    @Override
    public ResourceLocation getTextureLocation(Humanoid entity) {
        return TEXTURE;
    }
}
