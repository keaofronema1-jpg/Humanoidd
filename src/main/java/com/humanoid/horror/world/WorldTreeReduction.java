package com.humanoid.horror.world;

import com.humanoid.horror.HumanoidMod;

import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraftforge.event.level.DecorateBiomeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class WorldTreeReduction {

    private static final Random RANDOM = new Random();

    // %80 azaltma = %20'sini bırak
    private static final double TREE_SPAWN_CHANCE = 0.20D;

    private WorldTreeReduction() {
    }

    @SubscribeEvent
    public static void onBiomeDecorate(
            DecorateBiomeEvent.Decorate event
    ) {

        if (event.getType() != DecorateBiomeEvent.Decorate.EventType.TREE) {
            return;
        }

        /*
         * Ağacın oluşmasına %20 ihtimalle izin veriyoruz.
         * %80 ihtimalle vanilla ağacın oluşmasını engelliyoruz.
         */
        if (RANDOM.nextDouble() >= TREE_SPAWN_CHANCE) {
            event.setCanceled(true);
        }
    }
}
