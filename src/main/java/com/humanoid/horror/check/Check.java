package com.humanoid.horror.check;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.android.AndroidHandler;
import com.humanoid.horror.pc.PCKeyHandler;
import com.humanoid.horror.pc.WindowsAtmosBridge;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.storage.ServerLevelData;
import java.util.ArrayList;
import java.util.List;

public class Check {

    public static int platformType;

    public Check() {
    }

    public static void verifyPlatform() {
        String osName = System.getProperty("os.name");
        if (osName == null) {
            osName = "";
        }
        osName = osName.toLowerCase();

        if (osName.contains("android")) {
            Check.platformType = 2;
            AndroidHandler.initMobileLock();
        } else {
            Check.platformType = 1;
            // PC tarafı başlatma hazırlığı
        }
    }

    // OYUN İLK BAŞLADIĞINDA TETİKLENECEK KAFES METODU
    public static void setupInitialPrison(MinecraftServer server) {
        if (server == null) return;

        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld != null) {
            WorldBorder border = overworld.getWorldBorder();
            if (border != null) {
                border.setCenter(8.0, 8.0); // Chunk 0,0 ortası
                border.setSize(16.0);       // Boyut: Tam 1 Chunk
            }
        }

        if (server.getPlayerList() != null) {
            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            if (players != null && !players.isEmpty()) {
                List<ServerPlayer> safePlayerList = new ArrayList<>(players);
                
                for (ServerPlayer player : safePlayerList) {
                    if (player != null) {
                        player.setGameMode(GameType.ADVENTURE); 
                        
                        if (overworld != null) {
                            int safeY = overworld.getHeight(Heightmap.Types.WORLD_SURFACE, 8, 8);
                            
                            if (safeY < 10) { 
                                safeY = 64; 
                            }
                            
                            player.teleportTo(overworld, 8.0, (double)safeY + 1.0, 8.0, player.getYRot(), player.getXRot());
                        }
                    }
                }
            }
        }
    }

    // /START KOMUTU GELDİĞİNDE ZİNCİRİ KIRAN METOT
    public static void triggerStartCommand() {
        if (!HumanoidMod.isStartTriggered) {
            return;
        }

        if (Check.platformType == 1) {
            WindowsAtmosBridge.executeWindowsIsolation();
        } else if (Check.platformType == 2) {
            AndroidHandler.startMobileHorrorSystem();
        }

        // Dedicated Server Çökme Koruması (Client sınıfını Reflection ile güvenli çağırma)
        try {
            Class.forName("net.minecraft.client.Minecraft");
            Class<?> clientManagerClass = Class.forName("com.humanoid.horror.client.HorrorClientManager");
            clientManagerClass.getMethod("activateClientHorror").invoke(null);
        } catch (Exception ignored) {
            // Sunucu (VDS) ortamında sessizce devam eder
        }

        MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null || server.getPlayerList() == null) {
            return;
        }

        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld != null) {
            WorldBorder border = overworld.getWorldBorder();
            if (border != null) {
                // Sınırı 2 saniyede yumuşakça aç
                border.lerpSizeBetween(16.0, 58000000.0, 2000L); 
            }
            
            // Gece yarısı ve Fırtına başlat
            overworld.setDayTime(18000L);
            
            if (overworld.getLevelData() instanceof ServerLevelData levelData) {
                levelData.setClearWeatherTime(0);
                levelData.setRainTime(24000);
                levelData.setRaining(true);
                levelData.setThunderTime(24000);
                levelData.setThundering(true);
            }
        }

        // Oyuncuları Survival moduna çek
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players != null && !players.isEmpty()) {
            List<ServerPlayer> safePlayerList = new ArrayList<>(players);
            for (ServerPlayer player : safePlayerList) {
                if (player != null) {
                    player.setGameMode(GameType.SURVIVAL); 
                }
            }
        }
    }
}
