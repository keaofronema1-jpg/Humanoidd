package com.humanoid.horror.client;

import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "humanoid",
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class HumanoidDimensionEffectsRegister {

    public static final ResourceLocation EFFECTS =
            new ResourceLocation(
                    "humanoid",
                    "humanoid_dimension_effects"
            );

    @SubscribeEvent
    public static void register(
            RegisterDimensionSpecialEffectsEvent event
    ) {

        event.register(
                EFFECTS,
                new HumanoidDimensionEffects()
        );
    }
}
