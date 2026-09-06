package com.humanoid.horror.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final RegistryObject<EntityType<Humanoid>> HUMANOID =
            com.humanoid.horror.registry.ModEntities.HUMANOID;

    public static final RegistryObject<EntityType<Creature1>> CREATURE1 =
            com.humanoid.horror.registry.ModEntities.CREATURE1;

    public static final RegistryObject<EntityType<Creature2>> CREATURE2 =
            com.humanoid.horror.registry.ModEntities.CREATURE2;

    public static final RegistryObject<EntityType<Creature3>> CREATURE3 =
            com.humanoid.horror.registry.ModEntities.CREATURE3;

    public static final RegistryObject<EntityType<PhotoScareEntity>> PHOTO_SCARE =
            com.humanoid.horror.registry.ModEntities.PHOTO_SCARE;
}

Böylece iki "ModEntities" sınıfı da aynı kayıtları gösteriyor ve "PHOTO_SCARE" eksik kalmıyor.
