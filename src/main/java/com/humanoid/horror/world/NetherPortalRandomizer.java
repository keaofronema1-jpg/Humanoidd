package com.humanoid.horror.world;

import com.humanoid.horror.HumanoidMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class NetherPortalRandomizer {

    private static final Random RANDOM = new Random();

    private static final ResourceKey<Level> HUMANOID_DIMENSION =
            ResourceKey.create(
                    Registries.DIMENSION,
                    new ResourceLocation(
                            HumanoidMod.MOD_ID,
                            "humanoid_dimension"
                    )
            );

    private static final ResourceKey<Level> DIMENSION2 =
            ResourceKey.create(
                    Registries.DIMENSION,
                    new ResourceLocation(
                            HumanoidMod.MOD_ID,
                            "dimension2"
                    )
            );

    private NetherPortalRandomizer() {
    }

    @SubscribeEvent
    public static void onTravelToDimension(EntityTravelToDimensionEvent event) {

        // Sadece oyuncular.
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Sadece Nether'e gitmeye çalışan portal geçişleri.
        if (event.getDimension() != Level.NETHER) {
            return;
        }

        MinecraftServer server = player.getServer();

        if (server == null) {
            return;
        }

        // Nether'e geçişi tamamen engelle.
        event.setCanceled(true);

        // %50 humanoid_dimension, %50 dimension2.
        ResourceKey<Level> targetKey;

        if (RANDOM.nextBoolean()) {
            targetKey = HUMANOID_DIMENSION;
        } else {
            targetKey = DIMENSION2;
        }

        ServerLevel targetLevel = server.getLevel(targetKey);

        if (targetLevel == null) {
            return;
        }

        /*
         * Portal eventinin mevcut geçiş işlemi tamamen bittikten
         * sonra oyuncuyu seçilen dimension'a gönderiyoruz.
         */
        server.execute(() -> {

            if (!player.isAlive()) {
                return;
            }

            ServerLevel destination = server.getLevel(targetKey);

            if (destination == null) {
                return;
            }

            player.changeDimension(destination);
        });
    }
}
