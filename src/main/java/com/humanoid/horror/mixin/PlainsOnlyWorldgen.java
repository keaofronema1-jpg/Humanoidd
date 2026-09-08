package com.humanoid.horror.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.WorldPresets;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Optional;

@Mixin(WorldPresets.Registrar.class)
public abstract class PlainsOnlyWorldgen {

    @ModifyArg(
            method = "createOverworldOptions",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/NoiseBasedChunkGenerator;<init>(Lnet/minecraft/world/level/biome/BiomeSource;Lnet/minecraft/core/Holder;)V"
            ),
            index = 0
    )
    private BiomeSource humanoid$forcePlains(
            BiomeSource originalSource
    ) {
        Optional<Holder<Biome>> plains =
                originalSource.possibleBiomes()
                        .stream()
                        .filter(holder ->
                                holder.unwrapKey()
                                        .map(key ->
                                                key.location().toString()
                                                        .equals("minecraft:plains")
                                        )
                                        .orElse(false)
                        )
                        .findFirst();

        return plains
                .map(FixedBiomeSource::new)
                .orElse(originalSource);
    }
}
