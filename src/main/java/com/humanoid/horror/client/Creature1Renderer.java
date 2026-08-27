package com.humanoid.horror.client;

import com.humanoid.horror.entity.Creature1;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class Creature1Renderer extends MobRenderer<Creature1, Creature1Model<Creature1>> {

    // =========================================================
    // MODEL LAYER
    // =========================================================

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    new ResourceLocation("humanoid", "creature1"),
                    "main"
            );

    // =========================================================
    // TEXTURE
    // assets/humanoid/textures/entity/creature1.png
    // =========================================================

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(
                    "humanoid",
                    "textures/entity/creature1.png"
            );

    // =========================================================
    // RENDERER
    // =========================================================

    public Creature1Renderer(EntityRendererProvider.Context context) {

        super(
                context,

                new Creature1Model<>(
                        context.bakeLayer(LAYER_LOCATION)
                ),

                // Entity'nin gövdesinin etrafındaki shadow radius
                0.5F
        );
    }

    // =========================================================
    // TEXTURE
    // =========================================================

    @Override
    public ResourceLocation getTextureLocation(Creature1 entity) {
        return TEXTURE;
    }
}
