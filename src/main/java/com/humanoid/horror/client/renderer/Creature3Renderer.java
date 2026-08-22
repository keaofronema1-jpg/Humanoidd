package com.humanoid.horror.client.renderer;

import com.humanoid.horror.client.Creature3Model;
import com.humanoid.horror.entity.Creature3;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class Creature3Renderer extends MobRenderer<Creature3, Creature3Model<Creature3>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("humanoid", "textures/entity/creature3.png");

    public Creature3Renderer(EntityRendererProvider.Context context) {
        super(
                context,
                new Creature3Model<>(context.bakeLayer(Creature3Model.LAYER_LOCATION)),
                0.5F
        );
    }

    @Override
    public ResourceLocation getTextureLocation(Creature3 entity) {
        return TEXTURE;
    }
}