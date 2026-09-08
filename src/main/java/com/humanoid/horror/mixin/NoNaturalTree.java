package com.humanoid.horror.mixin;

import com.humanoid.horror.HumanoidMod;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BiomeGenerationSettings.class)
public abstract class NoNaturalTrees {

    @Shadow
    @Final
    private List<HolderSet<PlacedFeature>> features;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void humanoid$removeNaturalTrees(
            List<HolderSet<PlacedFeature>> features,
            CallbackInfo ci
    ) {

        if (!HumanoidWorldgenConfig.areTreesDisabled()) {
            return;
        }

        if (this.features == null) {
            return;
        }

        int vegetationIndex =
                GenerationStep.Decoration.VEGETAL_DECORATION.ordinal();

        if (vegetationIndex < 0 ||
                vegetationIndex >= this.features.size()) {
            return;
        }

        HolderSet<PlacedFeature> vegetation =
                this.features.get(vegetationIndex);

        if (vegetation == null) {
            return;
        }

        /*
         * Minecraft 1.20.1'de Plains'in doğal ağaç
         * generation'ı "trees_plains" gibi placed
         * feature'lar üzerinden gelir.
         *
         * Sadece tree isimli feature'ları kaldırıyoruz.
         * Çimen, çiçek vb. vegetation feature'ları
         * bırakılıyor.
         */
        vegetation.stream()
                .filter(holder ->
                        holder.unwrapKey()
                                .map(key -> {
                                    String path =
                                            key.location()
                                                    .getPath()
                                                    .toLowerCase();

                                    return path.contains("tree");
                                })
                                .orElse(false)
                )
                .forEach(holder ->
                        System.out.println(
                                "[HumanoidWorldgen] Doğal ağaç kapatıldı: "
                                        + holder.unwrapKey()
                                                .map(key ->
                                                        key.location()
                                                                .toString()
                                                )
                                                .orElse("unknown")
                        )
                );

        /*
         * HolderSet doğrudan mutable olmayabileceği için
         * yukarıdaki yöntem tek başına güvenli değildir.
         *
         * Bu nedenle aşağıdaki filtre ile yeni vegetation
         * listesini oluşturuyoruz.
         */
        List<Holder<PlacedFeature>> remaining =
                vegetation.stream()
                        .filter(holder ->
                                !holder.unwrapKey()
                                        .map(key ->
                                                key.location()
                                                        .getPath()
                                                        .toLowerCase()
                                                        .contains("tree")
                                        )
                                        .orElse(false)
                        )
                        .toList();

        this.features.set(
                vegetationIndex,
                HolderSet.direct(remaining)
        );
    }
}
