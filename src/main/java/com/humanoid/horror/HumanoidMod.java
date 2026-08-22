package com.humanoid.horror;

import com.humanoid.horror.entity.Creature3;
import com.humanoid.horror.network.ModMessages;
import com.humanoid.horror.entity.ModEntities;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;

import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("humanoid")
public class HumanoidMod {

    public static final String MOD_ID = "humanoid";

    public static boolean isStartTriggered = false;
    public static int currentDay = 1;

    public HumanoidMod() {

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Entity kayıtları
        ModEntities.ENTITIES.register(modEventBus);

        // Event listener'ları manuel bağlayarak çift tetiklenmeyi önlüyoruz
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::onAttributeCreate);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModMessages.register();
        });
    }

    private void onAttributeCreate(EntityAttributeCreationEvent event) {

        // HUMANOID
        if (ModEntities.HUMANOID.exists()) {
            event.put(
                    ModEntities.HUMANOID.get(),
                    Monster.createMonsterAttributes()
                            .add(Attributes.MAX_HEALTH, 40.0D)
                            .add(Attributes.MOVEMENT_SPEED, 0.3D)
                            .add(Attributes.ATTACK_DAMAGE, 6.0D)
                            .add(Attributes.FOLLOW_RANGE, 32.0D)
                            .build()
            );
        }

        // CREATURE3
        if (ModEntities.CREATURE3.exists()) {
            event.put(
                    ModEntities.CREATURE3.get(),
                    Creature3.createAttributes().build()
            );
        }
    }
}
