package com.humanoid.horror;

import com.humanoid.horror.entity.Creature3;
import com.humanoid.horror.network.ModMessages;
import com.humanoid.horror.registry.ModEntities;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;

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

    public HumanoidMod() {

        IEventBus modEventBus =
                FMLJavaModLoadingContext.get().getModEventBus();

        // TÜM entity kayıtları artık sadece ModEntities üzerinden yapılır.
        ModEntities.ENTITIES.register(modEventBus);

        // Network kayıtları
        modEventBus.addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {

        event.enqueueWork(() -> {
            ModMessages.register();
        });
    }

    /**
     * Entity attribute kayıtları.
     *
     * Humanoid ve Creature3 artık HumanoidMod içinde
     * tekrar oluşturulmuyor.
     *
     * Registry kaynakları doğrudan ModEntities'den alınıyor.
     */
    @SubscribeEvent
    public static void onAttributeCreate(
            EntityAttributeCreationEvent event) {

        // HUMANOID
        event.put(
                ModEntities.HUMANOID.get(),
                Monster.createMonsterAttributes()
                        .add(Attributes.MAX_HEALTH, 40.0D)
                        .add(Attributes.MOVEMENT_SPEED, 0.3D)
                        .add(Attributes.ATTACK_DAMAGE, 6.0D)
                        .add(Attributes.FOLLOW_RANGE, 32.0D)
                        .build()
        );

        // CREATURE3
        event.put(
                ModEntities.CREATURE3.get(),
                Creature3.createAttributes().build()
        );
    }
}