package com.humanoid.horror.server;

import com.humanoid.horror.pc.PCKeyHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "humanoid", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LobbyEventHandler {

    public LobbyEventHandler() {
    }

    /**
     * OYUNCU DÜNYAYA GİRDİĞİ AN TETİKLENEN EVENT
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        
        // Sistem zaten kilitlenip oyun başladıysa lobiyi tekrar kurma
        if (PCKeyHandler.isLocked) {
            return;
        }

        // Oyuncu nesnesi sunucu tarafında mı kontrolü
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // 1. OYUNCUYU MACERA MODUNA (ADVENTURE) ALIYORUZ
        serverPlayer.setGameMode(GameType.ADVENTURE);

        // Sunucu dünyasını (ServerLevel) alıyoruz
        ServerLevel serverLevel = serverPlayer.serverLevel();

        // 2. DÜNYA SINIRINI 1 CHUNK (16 BLOK) OLARAK AYARLIYORUZ
        WorldBorder worldBorder = serverLevel.getWorldBorder();
        worldBorder.setSize(16.0D);

        // Spawn noktasını alıyoruz
        BlockPos spawnPos = serverLevel.getSharedSpawnPos();
        
        // Sınır merkezini oyuncunun duracağı tam bloğun merkezine kilitliyoruz (+0.5D kaydırma ile)
        worldBorder.setCenter(spawnPos.getX() + 0.5D, spawnPos.getZ() + 0.5D); 

        // Oyuncuyu bloğun tam ortasına güvenle teleport ediyoruz
        serverPlayer.teleportTo(
                serverLevel,
                spawnPos.getX() + 0.5D,
                spawnPos.getY(),
                spawnPos.getZ() + 0.5D,
                serverPlayer.getYRot(),
                serverPlayer.getXRot()
        );
    }
}
