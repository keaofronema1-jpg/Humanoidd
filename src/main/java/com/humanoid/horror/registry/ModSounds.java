package com.humanoid.horror.registry;

import com.humanoid.horror.HumanoidMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(
                    Registries.SOUND_EVENT,
                    HumanoidMod.MOD_ID
            );

    public static final RegistryObject<SoundEvent> DIMENSION2_MUSIC =
            SOUNDS.register(
                    "dimension2music",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(
                                    HumanoidMod.MOD_ID,
                                    "dimension2music"
                            )
                    )
            );

    public static final RegistryObject<SoundEvent> DIMENSION2_MUSIC2 =
            SOUNDS.register(
                    "dimension2music2",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(
                                    HumanoidMod.MOD_ID,
                                    "dimension2music2"
                            )
                    )
            );
}
