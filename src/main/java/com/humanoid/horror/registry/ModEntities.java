package com.humanoid.horror.registry;

import com.humanoid.horror.HumanoidMod;

import com.humanoid.horror.entity.Creature1;
import com.humanoid.horror.entity.Creature2;
import com.humanoid.horror.entity.Creature3;
import com.humanoid.horror.entity.Humanoid;
import com.humanoid.horror.entity.PhotoScareEntity;

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
                            .of(
                                    Humanoid::new,
                                    MobCategory.MONSTER
                            )
                            .sized(0.6F, 1.95F)
                            .build("humanoid")
            );

    public static final RegistryObject<EntityType<Creature1>> CREATURE1 =
            ENTITIES.register(
                    "creature1",
                    () -> EntityType.Builder
                            .of(
                                    Creature1::new,
                                    MobCategory.MONSTER
                            )
                            .sized(0.8F, 2.0F)
                            .build("creature1")
            );

    public static final RegistryObject<EntityType<Creature2>> CREATURE2 =
            ENTITIES.register(
                    "creature2",
                    () -> EntityType.Builder
                            .of(
                                    Creature2::new,
                                    MobCategory.MONSTER
                            )
                            .sized(0.8F, 2.2F)
                            .build("creature2")
            );

    public static final RegistryObject<EntityType<Creature3>> CREATURE3 =
            ENTITIES.register(
                    "creature3",
                    () -> EntityType.Builder
                            .of(
                                    Creature3::new,
                                    MobCategory.MONSTER
                            )
                            .sized(0.8F, 2.2F)
                            .build("creature3")
            );

    public static final RegistryObject<EntityType<PhotoScareEntity>> PHOTO_SCARE =
            ENTITIES.register(
                    "photo_scare",
                    () -> EntityType.Builder
                            .of(
                                    (EntityType<PhotoScareEntity> type,
                                     net.minecraft.world.level.Level level) ->
                                            new PhotoScareEntity(type, level),
                                    MobCategory.MISC
                            )
                            .sized(1.0F, 2.0F)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("photo_scare")
            );
}

Buradaki kritik değişiklik sadece "PHOTO_SCARE" factory'sinde: Java'nın "Entity" olarak çıkarmasını engelleyip "EntityType<PhotoScareEntity>" olarak açıkça belirttik.

Ama kanka: "PhotoScareEntity.java" içindeki constructor'ın da "EntityType<PhotoScareEntity>" / uyumlu generic kabul etmesi gerekiyor. Onu da gönderirsen tam halini direkt düzelteyim.
