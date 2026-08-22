package com.humanoid.horror.client;

import com.humanoid.horror.client.model.Creature3Model;
import com.humanoid.horror.client.renderer.Creature3Renderer;
import com.humanoid.horror.client.Creature1Model;
import com.humanoid.horror.client.Creature1Renderer;
import com.humanoid.horror.client.Creature2Model;
import com.humanoid.horror.client.Creature2Renderer;

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
    // MODEL LAYER KAYITLARI
    // =========================================================

    @SubscribeEvent
    public static void registerLayerDefinitions(
            EntityRenderersEvent.RegisterLayerDefinitions event) {

        // Creature1
        event.registerLayerDefinition(
                Creature1Renderer.LAYER_LOCATION,
                Creature1Model::createBodyLayer
        );

        // Creature2
        event.registerLayerDefinition(
                Creature2Renderer.LAYER_LOCATION,
                Creature2Model::createBodyLayer
        );

        // Creature3
        event.registerLayerDefinition(
                Creature3Model.LAYER_LOCATION,
                Creature3Model::createBodyLayer
        );
    }

    // =========================================================
    // ENTITY RENDERER KAYITLARI
    // =========================================================

    @SubscribeEvent
    public static void registerEntityRenderers(
            EntityRenderersEvent.RegisterRenderers event) {

        // Creature1
        event.registerEntityRenderer(
                ModEntities.CREATURE1.get(),
                Creature1Renderer::new
        );

        // Creature2
        event.registerEntityRenderer(
                ModEntities.CREATURE2.get(),
                Creature2Renderer::new
        );

        // Creature3
        event.registerEntityRenderer(
                ModEntities.CREATURE3.get(),
                Creature3Renderer::new
        );
    }
}
