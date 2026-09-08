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

    // =========================================================
    // scare.ogg
    // =========================================================

    public static final RegistryObject<SoundEvent> SCARE =
            SOUNDS.register(
                    "scare",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(
                                    HumanoidMod.MOD_ID,
                                    "scare"
                            )
                    )
            );

    // =========================================================
    // wep.ogg
    // =========================================================

    public static final RegistryObject<SoundEvent> WEP =
            SOUNDS.register(
                    "wep",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(
                                    HumanoidMod.MOD_ID,
                                    "wep"
                            )
                    )
            );

    // =========================================================
    // cont.ogg
    // =========================================================

    public static final RegistryObject<SoundEvent> CONT =
            SOUNDS.register(
                    "cont",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(
                                    HumanoidMod.MOD_ID,
                                    "cont"
                            )
                    )
            );

    // =========================================================
    // dimension2music.ogg
    // =========================================================

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

    // =========================================================
    // dimension2music2.ogg
    // =========================================================

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

    // =========================================================
    // entity.ogg
    // =========================================================

    public static final RegistryObject<SoundEvent> PHOTO_ENTITY =
            SOUNDS.register(
                    "entity",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(
                                    HumanoidMod.MOD_ID,
                                    "entity"
                            )
                    )
            );

    // =========================================================
    // random.ogg
    // =========================================================

    public static final RegistryObject<SoundEvent> RANDOM =
            SOUNDS.register(
                    "random",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(
                                    HumanoidMod.MOD_ID,
                                    "random"
                            )
                    )
            );

    // =========================================================
    // forge.ogg
    // =========================================================

    public static final RegistryObject<SoundEvent> FORGE_INTRO =
            SOUNDS.register(
                    "forge_intro",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(
                                    HumanoidMod.MOD_ID,
                                    "forge_intro"
                            )
                    )
            );
}
