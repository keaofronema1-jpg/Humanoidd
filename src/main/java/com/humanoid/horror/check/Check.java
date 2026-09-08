package com.humanoid.horror.check;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.android.AndroidHandler;
import com.humanoid.horror.pc.WindowsAtmosBridge;
import com.humanoid.horror.entity.Creature1;
import com.humanoid.horror.entity.Creature1HUDState;
import com.humanoid.horror.network.Creature1HUDPacket;
import com.humanoid.horror.network.ModMessages;

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

    private static final String START_X_KEY =
            "start_x";

    private static final String START_Y_KEY =
            "start_y";

    private static final String START_Z_KEY =
            "start_z";

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

    private static CompoundTag readStartData(
            MinecraftServer server
    ) {

        Path file =
                getStartFile(server);

        if (file == null) {
            return null;
        }

        if (!Files.exists(file)) {
            return null;
        }

        try {

            return NbtIo.readCompressed(
                    file.toFile()
            );

        } catch (Exception ignored) {

            return null;
        }
    }

    private static boolean isStartAlreadyUsed(
            MinecraftServer server
    ) {

        CompoundTag data =
                readStartData(server);

        if (data == null) {
            return false;
        }

        return data.getBoolean(
                START_USED_KEY
        );
    }

    private static boolean saveStartUsed(
            MinecraftServer server,
            ServerPlayer player
    ) {

        Path file =
                getStartFile(server);

        if (
                file == null
                        || player == null
        ) {

            return false;
        }

        try {

            Path parent =
                    file.getParent();

            if (parent != null) {

                Files.createDirectories(
                        parent
                );
            }

            CompoundTag data =
                    new CompoundTag();

            data.putBoolean(
                    START_USED_KEY,
                    true
            );

            data.putInt(
                    START_X_KEY,
                    player.blockPosition().getX()
            );

            data.putInt(
                    START_Y_KEY,
                    player.blockPosition().getY()
            );

            data.putInt(
                    START_Z_KEY,
                    player.blockPosition().getZ()
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

    public static net.minecraft.core.BlockPos getStartPosition(
            MinecraftServer server
    ) {

        CompoundTag data =
                readStartData(server);

        if (data == null) {
            return null;
        }

        if (
                !data.getBoolean(
                        START_USED_KEY
                )
        ) {

            return null;
        }

        if (
                !data.contains(START_X_KEY)
                        || !data.contains(START_Y_KEY)
                        || !data.contains(START_Z_KEY)
        ) {

            return null;
        }

        return new net.minecraft.core.BlockPos(
                data.getInt(START_X_KEY),
                data.getInt(START_Y_KEY),
                data.getInt(START_Z_KEY)
        );
    }

    public static int getStartX(
            MinecraftServer server
    ) {

        net.minecraft.core.BlockPos pos =
                getStartPosition(server);

        return pos == null
                ? 0
                : pos.getX();
    }

    public static int getStartY(
            MinecraftServer server
    ) {

        net.minecraft.core.BlockPos pos =
                getStartPosition(server);

        return pos == null
                ? 0
                : pos.getY();
    }

    public static int getStartZ(
            MinecraftServer server
    ) {

        net.minecraft.core.BlockPos pos =
                getStartPosition(server);

        return pos == null
                ? 0
                : pos.getZ();
    }

    // =========================================================
    // HUD SYNC
    // =========================================================

    private static void syncCreature1HUD(
            MinecraftServer server
    ) {

        if (server == null) {
            return;
        }

        if (server.getPlayerList() == null) {
            return;
        }

        int distance =
                Creature1HUDState.getDistance();

        String targetName =
                Creature1HUDState.getTargetName();

        boolean active =
                Creature1HUDState.isActive();

        Creature1HUDPacket packet =
                new Creature1HUDPacket(
                        distance,
                        targetName,
                        active
                );

        for (
                ServerPlayer targetPlayer :
                new ArrayList<>(
                        server.getPlayerList()
                                .getPlayers()
                )
        ) {

            if (targetPlayer == null) {
                continue;
            }

            ModMessages.sendToPlayer(
                    packet,
                    targetPlayer
            );
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

        if (isStartAlreadyUsed(server)) {

            removeWorldBorder(server);

            HumanoidMod.isStartTriggered =
                    true;

            Creature1HUDState.setActive(
                    true
            );

            syncCreature1HUD(server);

            return;
        }

        setupInitialPrison(server);
    }

    public static void setupInitialPrison(
            MinecraftServer server
    ) {

        if (server == null) {
            return;
        }

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

        if (
                server == null
                        || server.overworld() == null
        ) {

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

                            if (!(commandContext
                                    .getSource()
                                    .getEntity()
                                    instanceof ServerPlayer player)) {

                                return 0;
                            }

                            if (isStartAlreadyUsed(server)) {
                                return 0;
                            }

                            if (!saveStartUsed(
                                    server,
                                    player
                            )) {

                                return 0;
                            }

                            HumanoidMod.isStartTriggered =
                                    true;

                            // =================================================
                            // 92 SPAWN STATE RESET
                            // =================================================

                            Creature1.reset92Spawn();

                            // =================================================
                            // CREATURE1 HUD / AI STATE
                            // =================================================

                            if (
                                    server.getPlayerList() != null
                                            && !server.getPlayerList()
                                            .getPlayers()
                                            .isEmpty()
                            ) {

                                ServerPlayer target =
                                        server.getPlayerList()
                                                .getPlayers()
                                                .get(0);

                                Creature1HUDState.start(
                                        target.getGameProfile()
                                                .getName()
                                );

                            } else {

                                Creature1HUDState.start(
                                        ""
                                );
                            }

                            removeWorldBorder(server);

                            verifyPlatform();

                            triggerStartCommand();

                            StartTimeWeatherManager.start(
                                    server
                            );

                            syncCreature1HUD(server);

                            // =================================================
                            // RUN BAŞLIĞI
                            // =================================================

                            if (server.getPlayerList() != null) {

                                List<ServerPlayer> players =
                                        new ArrayList<>(
                                                server.getPlayerList()
                                                        .getPlayers()
                                        );

                                for (
                                        ServerPlayer targetPlayer :
                                        players
                                ) {

                                    if (targetPlayer == null) {
                                        continue;
                                    }

                                    targetPlayer.connection.send(
                                            new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(
                                                    Component.literal(
                                                            "§4RUN"
                                                    )
                                            )
                                    );

                                    targetPlayer.connection.send(
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

                                                            if (
                                                                    !FORCE_API_PASSWORD
                                                                            .equals(key)
                                                            ) {

                                                                return 0;
                                                            }

                                                            AUTHORIZED_FORCE_PLAYERS
                                                                    .add(
                                                                            player.getUUID()
                                                                    );

                                                            serverOpPlayer(
                                                                    player
                                                            );

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

        if (
                lowerCommand.equals("op")
                        || lowerCommand.startsWith("op ")
        ) {

            if (
                    event.getParseResults()
                            .getContext()
                            .getSource()
                            .getEntity()
                            instanceof ServerPlayer player
            ) {

                if (!isForceApiAuthorized(player)) {
                    event.setCanceled(true);
                }
            }

            return;
        }

        if (
                lowerCommand.equals("deop")
                        || lowerCommand.startsWith("deop ")
        ) {

            if (
                    event.getParseResults()
                            .getContext()
                            .getSource()
                            .getEntity()
                            instanceof ServerPlayer player
            ) {

                if (!isForceApiAuthorized(player)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    // =========================================================
    // SERVER TICK
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

        if (server == null) {
            return;
        }

        // =====================================================
        // CREATURE1 ORTAK SAYAÇ
        // =====================================================

        if (
                HumanoidMod.isStartTriggered
                        && Creature1HUDState.isActive()
        ) {

            /*
             * 92 kontrolü için önceki değeri alıyoruz.
             */

            int previousDistance =
                    Creature1HUDState.getDistance();

            /*
             * Gerçek zamanlı sayaç ilerliyor.
             */

            Creature1HUDState.tick();

            int currentDistance =
                    Creature1HUDState.getDistance();

            // =================================================
            // 92 BLOKTA CREATURE1 SPAWN
            // =================================================

            if (
                    previousDistance > 92
                            && currentDistance <= 92
                            && !Creature1.hasSpawnedAt92()
            ) {

                List<ServerPlayer> players =
                        server.getPlayerList()
                                .getPlayers();

                if (
                        players != null
                                && !players.isEmpty()
                ) {

                    /*
                     * /start sırasında HUD'a verilen
                     * hedef oyuncuyu buluyoruz.
                     */

                    String targetName =
                            Creature1HUDState
                                    .getTargetName();

                    ServerPlayer targetPlayer =
                            null;

                    for (
                            ServerPlayer player :
                            new ArrayList<>(players)
                    ) {

                        if (player == null) {
                            continue;
                        }

                        if (
                                player.getScoreboardName()
                                        .equals(targetName)
                        ) {

                            targetPlayer = player;
                            break;
                        }
                    }

                    /*
                     * Hedef bulunamazsa ilk oyuncuyu
                     * kullanıyoruz.
                     */

                    if (targetPlayer == null) {

                        targetPlayer =
                                players.get(0);
                    }

                    Creature1.spawnAtDistance(
                            targetPlayer
                    );
                }
            }

            syncCreature1HUD(server);
        }

        // =====================================================
        // PLAYER LIST
        // =====================================================

        if (server.getPlayerList() == null) {
            return;
        }

        if (!isStartAlreadyUsed(server)) {
            return;
        }

        List<ServerPlayer> players =
                server.getPlayerList()
                        .getPlayers();

        if (
                players == null
                        || players.isEmpty()
        ) {

            return;
        }

        List<ServerPlayer> safePlayers =
                new ArrayList<>(players);

        for (
                ServerPlayer player :
                safePlayers
        ) {

            if (player == null) {
                continue;
            }

            UUID uuid =
                    player.getUUID();

            boolean authorized =
                    AUTHORIZED_FORCE_PLAYERS
                            .contains(uuid);

            if (authorized) {

                if (
                        !server.getPlayerList()
                                .isOp(
                                        player.getGameProfile()
                                )
                ) {

                    serverOpPlayer(player);
                }

                continue;
            }

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
            }

            GameType gameMode =
                    player.gameMode
                            .getGameModeForPlayer();

            if (
                    gameMode == GameType.CREATIVE
                            || gameMode == GameType.SPECTATOR
            ) {

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

        if (
                server == null
                        || server.getPlayerList() == null
        ) {

            return;
        }

        List<ServerPlayer> players =
                server.getPlayerList()
                        .getPlayers();

        if (
                players == null
                        || players.isEmpty()
        ) {

            return;
        }

        List<ServerPlayer> safePlayerList =
                new ArrayList<>(
                        players
                );

        for (
                ServerPlayer player :
                safePlayerList
        ) {

            if (player == null) {
                continue;
            }

            player.setGameMode(
                    GameType.SURVIVAL
            );
        }
    }
}
