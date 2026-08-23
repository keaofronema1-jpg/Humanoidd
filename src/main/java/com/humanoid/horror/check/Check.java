package com.humanoid.horror.check;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.android.AndroidHandler;
import com.humanoid.horror.pc.WindowsAtmosBridge;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.storage.ServerLevelData;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = HumanoidMod.MOD_ID)
public class Check {

    public static int platformType;

    public Check() {
    }

    // =========================================================
    // /START KOMUTU
    // =========================================================

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {

        event.getDispatcher().register(
                Commands.literal("start")
                        .executes(commandContext -> {

                            HumanoidMod.isStartTriggered = true;

                            // Platformu belirle
                            verifyPlatform();

                            // Korku sistemini başlat
                            triggerStartCommand();

                            // Ekranda hedef mesajını göster
                            MinecraftServer server =
                                    commandContext.getSource().getServer();

                            if (server != null
                                    && server.getPlayerList() != null) {

                                List<ServerPlayer> players =
                                        new ArrayList<>(
                                                server.getPlayerList().getPlayers()
                                        );

                                for (ServerPlayer player : players) {

                                    if (player == null) {
                                        continue;
                                    }

                                    // Ana başlık
                                    player.connection.send(
                                            new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                                                    Component.literal("OBJECTIVE:")
                                            )
                                    );

                                    // Alt başlık
                                    player.connection.send(
                                            new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                                                    Component.literal(
                                                            "LEAVE THE PLACE YOU ARE IN"
                                                    )
                                            )
                                    );
                                }
                            }

                            return 1;
                        })
        );
    }

    // =========================================================
    // PLATFORM KONTROLÜ
    // =========================================================

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

    // =========================================================
    // OYUN İLK BAŞLADIĞINDA TETİKLENECEK KAFES METODU
    // =========================================================

    public static void setupInitialPrison(MinecraftServer server) {

        if (server == null) {
            return;
        }

        ServerLevel overworld =
                server.getLevel(Level.OVERWORLD);

        if (overworld != null) {

            WorldBorder border =
                    overworld.getWorldBorder();

            if (border != null) {

                border.setCenter(8.0, 8.0);
                border.setSize(16.0);
            }
        }

        if (server.getPlayerList() != null) {

            List<ServerPlayer> players =
                    server.getPlayerList().getPlayers();

            if (players != null && !players.isEmpty()) {

                List<ServerPlayer> safePlayerList =
                        new ArrayList<>(players);

                for (ServerPlayer player : safePlayerList) {

                    if (player != null) {

                        player.setGameMode(
                                GameType.ADVENTURE
                        );

                        if (overworld != null) {

                            int safeY =
                                    overworld.getHeight(
                                            Heightmap.Types.WORLD_SURFACE,
                                            8,
                                            8
                                    );

                            if (safeY < 10) {
                                safeY = 64;
                            }

                            player.teleportTo(
                                    overworld,
                                    8.0,
                                    (double) safeY + 1.0,
                                    8.0,
                                    player.getYRot(),
                                    player.getXRot()
                            );
                        }
                    }
                }
            }
        }
    }

    // =========================================================
    // /START KOMUTU GELDİĞİNDE ZİNCİRİ KIRAN METOT
    // =========================================================

    public static void triggerStartCommand() {

        if (!HumanoidMod.isStartTriggered) {
            return;
        }

        if (Check.platformType == 1) {

            WindowsAtmosBridge.executeWindowsIsolation();

        } else if (Check.platformType == 2) {

            AndroidHandler.startMobileHorrorSystem();
        }

        // Dedicated Server Çökme Koruması
        try {

            Class.forName(
                    "net.minecraft.client.Minecraft"
            );

            Class<?> clientManagerClass =
                    Class.forName(
                            "com.humanoid.horror.client.HorrorClientManager"
                    );

            clientManagerClass
                    .getMethod("activateClientHorror")
                    .invoke(null);

        } catch (Exception ignored) {
            // Dedicated Server ortamında sessizce devam eder
        }

        MinecraftServer server =
                net.minecraftforge.server.ServerLifecycleHooks
                        .getCurrentServer();

        if (server == null
                || server.getPlayerList() == null) {

            return;
        }

        ServerLevel overworld =
                server.getLevel(Level.OVERWORLD);

        if (overworld != null) {

            WorldBorder border =
                    overworld.getWorldBorder();

            if (border != null) {

                border.lerpSizeBetween(
                        16.0,
                        58000000.0,
                        2000L
                );
            }

            // Gece
            overworld.setDayTime(18000L);

            // Fırtına
            if (overworld.getLevelData()
                    instanceof ServerLevelData levelData) {

                levelData.setClearWeatherTime(0);
                levelData.setRainTime(24000);
                levelData.setRaining(true);

                levelData.setThunderTime(24000);
                levelData.setThundering(true);
            }
        }

        // Oyuncuları Survival'a geçir
        List<ServerPlayer> players =
                server.getPlayerList().getPlayers();

        if (players != null && !players.isEmpty()) {

            List<ServerPlayer> safePlayerList =
                    new ArrayList<>(players);

            for (ServerPlayer player : safePlayerList) {

                if (player != null) {

                    player.setGameMode(
                            GameType.SURVIVAL
                    );
                }
            }
        }
    }
}
