package com.humanoid.horror.client.model;

import com.humanoid.horror.client.model.Creature3Model;
import com.humanoid.horror.client.renderer.Creature3Renderer;
import com.humanoid.horror.registry.ModEntities; // Paketi dilediğin gibi registry olarak güncelledik!
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "humanoid", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class Creature3Model {

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(Creature3Model.LAYER_LOCATION, Creature3Model::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.CREATURE3.get(), Creature3Renderer::new);
    }
}
