package com.humanoid.horror.event;

import com.humanoid.horror.entity.Creature1;
import com.humanoid.horror.entity.Creature2;
import com.humanoid.horror.entity.Creature3;
import com.humanoid.horror.entity.Humanoid;
import com.humanoid.horror.registry.ModEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "humanoid", bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityAttributeRegister {

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        // Moddaki TÜM yaratıkların can, hız ve saldırı attribute'larını eksiksiz kaydediyoruz
        event.put(ModEntities.HUMANOID.get(), Humanoid.createAttributes().build());
        event.put(ModEntities.CREATURE1.get(), Creature1.createAttributes().build());
        event.put(ModEntities.CREATURE2.get(), Creature2.createAttributes().build());
        event.put(ModEntities.CREATURE3.get(), Creature3.createAttributes().build());
    }
}
