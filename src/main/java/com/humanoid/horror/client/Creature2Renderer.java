package com.humanoid.horror.client;

import com.humanoid.horror.entity.Creature2;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class Creature2Renderer extends MobRenderer<Creature2, Creature2Model<Creature2>> {

    // Yaratığın kaplama (PNG) dosya yolu
    private static final ResourceLocation TEXTURE = new ResourceLocation("humanoid", "textures/entity/creature2.png");
    
    // Client kayıtlarında oluşturduğun Model Layer konumu
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("humanoid", "creature2"), "main");

    public Creature2Renderer(EntityRendererProvider.Context context) {
        super(context, new Creature2Model<>(context.bakeLayer(LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(Creature2 entity) {
        return TEXTURE;
    }
}
