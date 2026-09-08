package com.humanoid.horror.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Optional;

@Mixin(
        target = "net.minecraft.world.level.levelgen.WorldPresets$Registrar"
)
public abstract class PlainsOnlyWorldgen {

    /**
     * Sadece Overworld oluşturulurken kullanılan
     * BiomeSource'u Plains'e sabitler.
     *
     * Nether, End ve diğer custom dimension'ların
     * NoiseBasedChunkGenerator'larına dokunmaz.
     */
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
                originalSource
                        .possibleBiomes()
                        .stream()
                        .filter(holder ->
                                holder.unwrapKey()
                                        .map(key ->
                                                key.location()
                                                        .toString()
                                                        .equals("minecraft:plains")
                                        )
                                        .orElse(false)
                        )
                        .findFirst();

        if (plains.isPresent()) {
            return new FixedBiomeSource(
                    plains.get()
            );
        }

        /*
         * Plains holder bulunamazsa dünya oluşturmayı
         * çökertmek yerine vanilla source'u koru.
         */
        return originalSource;
    }
}
