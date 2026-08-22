package com.humanoid.horror.entity;

import com.humanoid.horror.HumanoidMod;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(
                    ForgeRegistries.ENTITY_TYPES,
                    HumanoidMod.MOD_ID
            );

    public static final RegistryObject<EntityType<Humanoid>> HUMANOID =
            ENTITIES.register(
                    "humanoid",
                    () -> EntityType.Builder
                            .of(Humanoid::new, MobCategory.MONSTER)
                            .sized(1.5F, 3.0F)
                            .build("humanoid")
            );

    public static final RegistryObject<EntityType<Creature1>> CREATURE1 =
            ENTITIES.register(
                    "creature1",
                    () -> EntityType.Builder
                            .of(Creature1::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .build("creature1")
            );

    public static final RegistryObject<EntityType<Creature2>> CREATURE2 =
            ENTITIES.register(
                    "creature2",
                    () -> EntityType.Builder
                            .of(Creature2::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .build("creature2")
            );

    public static final RegistryObject<EntityType<Creature3>> CREATURE3 =
            ENTITIES.register(
                    "creature3",
                    () -> EntityType.Builder
                            .of(Creature3::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .build("creature3")
            );
}
