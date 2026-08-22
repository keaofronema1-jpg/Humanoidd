package com.humanoid.horror.event;

import com.humanoid.horror.entity.Creature1;
import com.humanoid.horror.entity.Creature2;
import com.humanoid.horror.entity.Creature3;
import com.humanoid.horror.entity.Humanoid;
import com.humanoid.horror.entity.ModEntities;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

public class EntityAttributeRegister {

    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        if (ModEntities.HUMANOID.isPresent()) {
            event.put(ModEntities.HUMANOID.get(), Humanoid.createAttributes().build());
        }
        if (ModEntities.CREATURE1.isPresent()) {
            event.put(ModEntities.CREATURE1.get(), Creature1.createAttributes().build());
        }
        if (ModEntities.CREATURE2.isPresent()) {
            event.put(ModEntities.CREATURE2.get(), Creature2.createAttributes().build());
        }
        if (ModEntities.CREATURE3.isPresent()) {
            event.put(ModEntities.CREATURE3.get(), Creature3.createAttributes().build());
        }
    }
}
