package com.humanoid.horror.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(
targets = "net.minecraft.world.level.levelgen.presets.WorldPresets$Registrar"
)
public abstract class PlainsOnlyWorldgen {

private static final ResourceKey<Biome> PLAINS =
        ResourceKey.create(
                BuiltInRegistries.BIOME.key(),
                new ResourceLocation("minecraft", "plains")
        );

@ModifyArg(
        method = "createOverworldOptions(Lnet/minecraft/world/level/biome/BiomeSource;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/level/dimension/DimensionOptions;",
        at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/level/levelgen/NoiseBasedChunkGenerator;<init>(Lnet/minecraft/world/level/biome/BiomeSource;Lnet/minecraft/core/Holder;)V"
        ),
        index = 0
)
private BiomeSource humanoid$forcePlains(
        BiomeSource originalSource
) {

    Holder<Biome> plainsHolder =
            BuiltInRegistries.BIOME
                    .getHolder(PLAINS)
                    .orElse(null);

    if (plainsHolder == null) {
        return originalSource;
    }

    return new FixedBiomeSource(plainsHolder);
}

}
