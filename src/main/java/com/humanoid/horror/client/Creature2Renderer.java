package com.humanoid.horror.client;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.entity.Creature2;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class Creature2Renderer
        extends MobRenderer<Creature2, Creature2Model<Creature2>> {

    // =========================================================
    // TEXTURE
    // assets/humanoid/textures/entity/creature2.png
    // =========================================================

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(
                    HumanoidMod.MOD_ID,
                    "textures/entity/creature2.png"
            );

    // =========================================================
    // MODEL LAYER
    // =========================================================

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(
                    new ResourceLocation(
                            HumanoidMod.MOD_ID,
                            "creature2"
                    ),
                    "main"
            );

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Creature2Renderer(
            EntityRendererProvider.Context context
    ) {
        super(
                context,
                new Creature2Model<>(
                        context.bakeLayer(LAYER_LOCATION)
                ),
                0.5F
        );
    }

    // =========================================================
    // TEXTURE LOCATION
    // =========================================================

    @Override
    public ResourceLocation getTextureLocation(
            Creature2 entity
    ) {
        return TEXTURE;
    }
        }
