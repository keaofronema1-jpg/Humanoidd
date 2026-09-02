package com.humanoid.horror.client;

import com.humanoid.horror.client.Creature1Model;
import com.humanoid.horror.client.Creature2Model;
import com.humanoid.horror.client.model.Creature3Model;
import com.humanoid.horror.client.model.HumanoidModel;

import com.humanoid.horror.client.Creature1Renderer;
import com.humanoid.horror.client.Creature2Renderer;
import com.humanoid.horror.client.renderer.Creature3Renderer;
import com.humanoid.horror.client.renderer.HumanoidRenderer;

import com.humanoid.horror.registry.ModEntities;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "humanoid",
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientRegister {

    // =========================================================
    // MODEL LAYER REGISTRATION
    // =========================================================

    @SubscribeEvent
    public static void registerLayerDefinitions(
            EntityRenderersEvent.RegisterLayerDefinitions event
    ) {

        event.registerLayerDefinition(
                Creature1Model.LAYER_LOCATION,
                Creature1Model::createBodyLayer
        );

        event.registerLayerDefinition(
                Creature2Model.LAYER_LOCATION,
                Creature2Model::createBodyLayer
        );

        event.registerLayerDefinition(
                Creature3Model.LAYER_LOCATION,
                Creature3Model::createBodyLayer
        );

        event.registerLayerDefinition(
                HumanoidModel.LAYER_LOCATION,
                HumanoidModel::createBodyLayer
        );
    }

    // =========================================================
    // ENTITY RENDERER REGISTRATION
    // =========================================================

    @SubscribeEvent
    public static void registerEntityRenderers(
            EntityRenderersEvent.RegisterRenderers event
    ) {

        event.registerEntityRenderer(
                ModEntities.CREATURE1.get(),
                Creature1Renderer::new
        );

        event.registerEntityRenderer(
                ModEntities.CREATURE2.get(),
                Creature2Renderer::new
        );

        event.registerEntityRenderer(
                ModEntities.CREATURE3.get(),
                Creature3Renderer::new
        );

        event.registerEntityRenderer(
                ModEntities.HUMANOID.get(),
                HumanoidRenderer::new
        );
    }
}
