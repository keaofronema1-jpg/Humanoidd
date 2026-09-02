package com.humanoid.horror;

import com.humanoid.horror.entity.Creature3;
import com.humanoid.horror.entity.Humanoid;
import com.humanoid.horror.network.ModMessages;
import com.humanoid.horror.registry.ModBlocks;
import com.humanoid.horror.registry.ModEntities;
import com.humanoid.horror.registry.ModSounds;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod("humanoid")
@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD
)
public class HumanoidMod {

    public static final String MOD_ID = "humanoid";

    public static boolean isStartTriggered = false;

    public static int currentDay = 1;

    // =========================================================
    // ENTITY REGISTRY
    // =========================================================

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(
                    ForgeRegistries.ENTITY_TYPES,
                    MOD_ID
            );

    // =========================================================
    // HUMANOID
    // =========================================================

    public static final RegistryObject<EntityType<Humanoid>> HUMANOID =
            ENTITIES.register(
                    "humanoid",
                    () -> EntityType.Builder.of(
                            Humanoid::new,
                            MobCategory.MONSTER
                    )
                    .sized(0.6F, 1.95F)
                    .build("humanoid")
            );

    // =========================================================
    // CREATURE3
    // =========================================================

    public static final RegistryObject<EntityType<Creature3>> CREATURE3 =
            ENTITIES.register(
                    "creature3",
                    () -> EntityType.Builder.of(
                            Creature3::new,
                            MobCategory.MONSTER
                    )
                    .sized(0.8F, 2.2F)
                    .clientTrackingRange(8)
                    .build("creature3")
            );

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public HumanoidMod() {

        IEventBus modEventBus =
                FMLJavaModLoadingContext
                        .get()
                        .getModEventBus();

        // Kendi Entity kayıtların
        ENTITIES.register(modEventBus);

        // ModEntities içindeki kayıtlar
        ModEntities.ENTITIES.register(modEventBus);

        // TPBlock1 / TPBlock2
        ModBlocks.BLOCKS.register(modEventBus);

        // Dimension2 sesleri
        ModSounds.SOUNDS.register(modEventBus);

        // Common setup
        modEventBus.addListener(this::setup);
    }

    // =========================================================
    // COMMON SETUP
    // =========================================================

    private void setup(
            final FMLCommonSetupEvent event
    ) {

        event.enqueueWork(() -> {

            // Mevcut network sistemin
            ModMessages.register();

        });
    }

    // =========================================================
    // ENTITY ATTRIBUTES
    // =========================================================

    @SubscribeEvent
    public static void onAttributeCreate(
            EntityAttributeCreationEvent event
    ) {

        // Humanoid
        event.put(
                HUMANOID.get(),
                Monster.createMonsterAttributes()
                        .add(
                                Attributes.MAX_HEALTH,
                                40.0D
                        )
                        .add(
                                Attributes.MOVEMENT_SPEED,
                                0.3D
                        )
                        .add(
                                Attributes.ATTACK_DAMAGE,
                                6.0D
                        )
                        .add(
                                Attributes.FOLLOW_RANGE,
                                32.0D
                        )
                        .build()
        );

        // Creature3
        event.put(
                CREATURE3.get(),
                Creature3.createAttributes().build()
        );
    }
}
