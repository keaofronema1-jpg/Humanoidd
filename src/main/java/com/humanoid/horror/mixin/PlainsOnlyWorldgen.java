package com.humanoid.horror.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Optional;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class PlainsOnlyWorldgen {

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;<init>(Lnet/minecraft/world/level/biome/BiomeSource;)V"
            ),
            index = 0
    )
    private static BiomeSource humanoid$forcePlains(BiomeSource originalSource) {

        Optional<Holder<Biome>> plains = originalSource
                .possibleBiomes()
                .stream()
                .filter(holder -> holder.unwrapKey()
                        .map(key -> key.location().toString().equals("minecraft:plains"))
                        .orElse(false))
                .findFirst();

        if (plains.isPresent()) {
            return new FixedBiomeSource(plains.get());
        }

        // Güvenlik: Plains bulunamazsa vanilla biome source'u bozma.
        return originalSource;
    }
}
