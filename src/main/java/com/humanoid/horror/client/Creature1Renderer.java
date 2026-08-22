package com.humanoid.horror.client;

import com.humanoid.horror.entity.Creature1;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class Creature1Renderer extends MobRenderer<Creature1, Creature1Model<Creature1>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("humanoid", "textures/entity/creature1.png");

    public Creature1Renderer(EntityRendererProvider.Context context) {
        super(context, new Creature1Model<>(context.bakeLayer(ClientRegister.CREATURE1_LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(Creature1 entity) {
        return TEXTURE;
    }
}
