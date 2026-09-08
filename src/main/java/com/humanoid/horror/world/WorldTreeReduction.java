package com.humanoid.horror.world;

import com.humanoid.horror.HumanoidMod;

import net.minecraft.world.entity.animal.Animal;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class WorldMobReduction {

    private static final Random RANDOM = new Random();

    /*
     * %80 azaltma:
     * %20 doğal spawn'a izin verilir.
     */
    private static final double SPAWN_CHANCE = 0.20D;

    private WorldMobReduction() {
    }

    @SubscribeEvent
    public static void onLivingSpawn(LivingSpawnEvent.CheckSpawn event) {

        // Sadece doğal spawnları azalt.
        if (event.getSpawnReason() != LivingSpawnEvent.SpawnReason.NATURAL) {
            return;
        }

        /*
         * Sadece Animal sınıfındaki pasif hayvanlar.
         *
         * Örnek:
         * Cow
         * Pig
         * Sheep
         * Chicken
         * Rabbit
         * Horse
         * vb.
         */
        if (!(event.getEntity() instanceof Animal)) {
            return;
        }

        /*
         * %20 ihtimalle spawn'a izin ver.
         * %80 ihtimalle spawn'ı engelle.
         */
        if (RANDOM.nextDouble() >= SPAWN_CHANCE) {
            event.setCanceled(true);
        }
    }
}
