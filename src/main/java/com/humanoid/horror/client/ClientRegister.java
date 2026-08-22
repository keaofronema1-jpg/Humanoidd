package com.humanoid.horror.client;

import com.humanoid.horror.client.model.Creature3Model;
import com.humanoid.horror.client.renderer.Creature3Renderer;
import com.humanoid.horror.entity.ModEntities; // Yaratık Entity'lerinin kayıtlı olduğu sınıfın
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "humanoid", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegister {

    // 1. MODEL LAYER REGISTRATION (Modeli Pişirme Kaydı)
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // Creature3 Model Kaydı
        event.registerLayerDefinition(Creature3Model.LAYER_LOCATION, Creature3Model::createBodyLayer);
        
        // İleride Creature1Model ve Creature2Model ekleyince buraya:
        // event.registerLayerDefinition(Creature1Model.LAYER_LOCATION, Creature1Model::createBodyLayer);
        // event.registerLayerDefinition(Creature2Model.LAYER_LOCATION, Creature2Model::createBodyLayer);
    }

    // 2. ENTITY RENDERER REGISTRATION (Entity ile Renderer'ı Eşleştirme)
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Creature3 Renderer Kaydı (ModEntities içindeki senin kaydın)
        event.registerEntityRenderer(ModEntities.CREATURE3.get(), Creature3Renderer::new);
        
        // İleride Creature1 ve Creature2 için:
        // event.registerEntityRenderer(ModEntities.CREATURE1.get(), Creature1Renderer::new);
        // event.registerEntityRenderer(ModEntities.CREATURE2.get(), Creature2Renderer::new);
    }
}
