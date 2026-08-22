package com.humanoid.horror;

import com.humanoid.horror.event.EntityAttributeRegister;
import com.humanoid.horror.network.ModMessages;
import com.humanoid.horror.entity.ModEntities;

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

        // Event yönlendirmeleri
        modEventBus.addListener(this::setup);
        
        // Attribute kaydı geldiğinde direkt EntityAttributeRegister sınıfına yönlendiriyoruz
        modEventBus.addListener(EntityAttributeRegister::onAttributeCreate);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModMessages.register();
        });
    }
}
