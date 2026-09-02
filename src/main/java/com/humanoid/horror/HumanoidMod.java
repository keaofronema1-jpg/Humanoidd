package com.humanoid.horror;

import com.humanoid.horror.entity.Creature3;
import com.humanoid.horror.network.ModMessages;
import com.humanoid.horror.registry.ModBlocks;
import com.humanoid.horror.registry.ModEntities;
import com.humanoid.horror.registry.ModSounds;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

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
    // CONSTRUCTOR
    // =========================================================

    public HumanoidMod() {

        IEventBus modEventBus =
                FMLJavaModLoadingContext
                        .get()
                        .getModEventBus();

        // ENTITY REGISTRY
        // Bütün entity kayıtları artık sadece ModEntities'den geliyor.
        ModEntities.ENTITIES.register(modEventBus);

        // BLOCK REGISTRY
        ModBlocks.BLOCKS.register(modEventBus);

        // SOUND REGISTRY
        ModSounds.SOUNDS.register(modEventBus);

        // COMMON SETUP
        modEventBus.addListener(this::setup);
    }

    // =========================================================
    // COMMON SETUP
    // =========================================================

    private void setup(
            final FMLCommonSetupEvent event
    ) {

        event.enqueueWork(() -> {

            // Network sistemi
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
                ModEntities.HUMANOID.get(),
                net.minecraft.world.entity.monster.Monster
                        .createMonsterAttributes()
                        .add(
                                net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,
                                40.0D
                        )
                        .add(
                                net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,
                                0.3D
                        )
                        .add(
                                net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                                6.0D
                        )
                        .add(
                                net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE,
                                32.0D
                        )
                        .build()
        );

        // Creature3
        event.put(
                ModEntities.CREATURE3.get(),
                Creature3.createAttributes().build()
        );
    }
}
