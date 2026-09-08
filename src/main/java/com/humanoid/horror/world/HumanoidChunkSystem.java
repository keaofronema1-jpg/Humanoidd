package com.humanoid.horror.world;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.check.Check;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.Queue;

@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class HumanoidChunkSystem {

    private static final int AREA_CHUNKS = 16;

    private static final int START_DISTANCE = 200;

    private static final int CHUNKS_PER_TICK = 2;

    private static final int WORLD_MIN_Y = -64;
    private static final int WORLD_MAX_Y = 319;

    private static final BlockState ORE_REPLACEMENT =
            Blocks.STONE.defaultBlockState();

    private static final Queue<ChunkPosition> CHUNK_QUEUE =
            new ArrayDeque<>();

    private static boolean started = false;

    private static boolean finished = false;

    private static ServerLevel activeLevel = null;

    private HumanoidChunkSystem() {
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
                event.getServer();

        if (server == null) {
            return;
        }

        if (!HumanoidMod.isStartTriggered) {
            return;
        }

        ServerLevel overworld =
                server.overworld();

        if (overworld == null) {
            return;
        }

        if (!started) {

            startSystem(
                    server,
                    overworld
            );

            return;
        }

        if (finished) {
            return;
        }

        for (int i = 0;
             i < CHUNKS_PER_TICK;
             i++) {

            ChunkPosition position =
                    CHUNK_QUEUE.poll();

            if (position == null) {

                finished = true;

                System.out.println(
                        "[HumanoidChunkSystem] " +
                        "16x16 chunk sistemi tamamlandı."
                );

                break;
            }

            processChunk(
                    activeLevel,
                    position.chunkX,
                    position.chunkZ
            );
        }
    }

    // =========================================================
    // SYSTEM START
    // =========================================================

    private static void startSystem(
            MinecraftServer server,
            ServerLevel level
    ) {

        BlockPos startPos =
                Check.getStartPosition(server);

        if (startPos == null) {

            System.err.println(
                    "[HumanoidChunkSystem] " +
                    "Start koordinatı bulunamadı."
            );

            return;
        }

        BlockPos areaStart =
                new BlockPos(
                        startPos.getX()
                                + START_DISTANCE,
                        startPos.getY(),
                        startPos.getZ()
                );

        int startChunkX =
                Math.floorDiv(
                        areaStart.getX(),
                        16
                );

        int startChunkZ =
                Math.floorDiv(
                        areaStart.getZ(),
                        16
                );

        CHUNK_QUEUE.clear();

        for (int x = 0;
             x < AREA_CHUNKS;
             x++) {

            for (int z = 0;
                 z < AREA_CHUNKS;
                 z++) {

                CHUNK_QUEUE.add(
                        new ChunkPosition(
                                startChunkX + x,
                                startChunkZ + z
                        )
                );
            }
        }

        activeLevel =
                level;

        started = true;
        finished = false;

        System.out.println(
                "[HumanoidChunkSystem] " +
                "Başladı. Start=" +
                startPos +
                ", AreaStart=" +
                areaStart +
                ", ChunkStart=(" +
                startChunkX +
                "," +
                startChunkZ +
                "), Toplam=256"
        );
    }

    // =========================================================
    // CHUNK PROCESS
    // =========================================================

    private static void processChunk(
            ServerLevel level,
            int chunkX,
            int chunkZ
    ) {

        if (level == null) {
            return;
        }

        level.getChunk(
                chunkX,
                chunkZ
        );

        int minBlockX =
                chunkX * 16;

        int minBlockZ =
                chunkZ * 16;

        int maxBlockX =
                minBlockX + 15;

        int maxBlockZ =
                minBlockZ + 15;

        int highestY =
                findHighestNonBedrockBlock(
                        level,
                        minBlockX,
                        minBlockZ,
                        maxBlockX,
                        maxBlockZ
                );

        if (highestY < WORLD_MIN_Y) {
            return;
        }

        int copyStartY =
                highestY + 1;

        if (copyStartY > WORLD_MAX_Y) {
            return;
        }

        copyChunkUp(
                level,
                minBlockX,
                minBlockZ,
                maxBlockX,
                maxBlockZ,
                highestY,
                copyStartY
        );

        System.out.println(
                "[HumanoidChunkSystem] " +
                "Chunk işlendi: (" +
                chunkX +
                "," +
                chunkZ +
                ")"
        );
    }

    // =========================================================
    // FIND HIGHEST BLOCK
    // =========================================================

    private static int findHighestNonBedrockBlock(
            ServerLevel level,
            int minX,
            int minZ,
            int maxX,
            int maxZ
    ) {

        int highest =
                WORLD_MIN_Y - 1;

        for (int y = WORLD_MAX_Y;
             y >= WORLD_MIN_Y;
             y--) {

            boolean found = false;

            for (int x = minX;
                 x <= maxX;
                 x++) {

                for (int z = minZ;
                     z <= maxZ;
                     z++) {

                    BlockState state =
                            level.getBlockState(
                                    new BlockPos(
                                            x,
                                            y,
                                            z
                                    )
                            );

                    if (state.isAir()) {
                        continue;
                    }

                    if (state.is(Blocks.BEDROCK)) {
                        continue;
                    }

                    found = true;
                    break;
                }

                if (found) {
                    break;
                }
            }

            if (found) {
                highest = y;
                break;
            }
        }

        return highest;
    }

    // =========================================================
    // COPY CHUNK
    // =========================================================

    private static void copyChunkUp(
            ServerLevel level,
            int minX,
            int minZ,
            int maxX,
            int maxZ,
            int highestY,
            int copyStartY
    ) {

        int sourceMinY =
                WORLD_MIN_Y;

        int sourceMaxY =
                highestY;

        int height =
                sourceMaxY
                        - sourceMinY
                        + 1;

        if (height <= 0) {
            return;
        }

        int maxTargetY =
                copyStartY
                        + height
                        - 1;

        if (maxTargetY > WORLD_MAX_Y) {

            height =
                    WORLD_MAX_Y
                            - copyStartY
                            + 1;

            if (height <= 0) {
                return;
            }
        }

        BlockState[][][] states =
                new BlockState[16][height][16];

        // =====================================================
        // 1. OKU
        // =====================================================

        for (int localX = 0;
             localX < 16;
             localX++) {

            for (int localY = 0;
                 localY < height;
                 localY++) {

                int sourceY =
                        sourceMinY
                                + localY;

                for (int localZ = 0;
                     localZ < 16;
                     localZ++) {

                    BlockPos sourcePos =
                            new BlockPos(
                                    minX + localX,
                                    sourceY,
                                    minZ + localZ
                            );

                    BlockState state =
                            level.getBlockState(
                                    sourcePos
                            );

                    if (state.is(Blocks.BEDROCK)) {

                        states[
                                localX
                        ][
                                localY
                        ][
                                localZ
                        ] =
                                Blocks.AIR
                                        .defaultBlockState();

                        continue;
                    }

                    if (isOre(state)) {

                        states[
                                localX
                        ][
                                localY
                        ][
                                localZ
                        ] =
                                ORE_REPLACEMENT;

                        continue;
                    }

                    states[
                            localX
                    ][
                            localY
                    ][
                            localZ
                    ] =
                            state;
                }
            }
        }

        // =====================================================
        // 2. KOPYALA / YERLEŞTİR
        // =====================================================

        for (int localX = 0;
             localX < 16;
             localX++) {

            for (int localY = 0;
                 localY < height;
                 localY++) {

                int targetY =
                        copyStartY
                                + localY;

                if (targetY > WORLD_MAX_Y) {
                    continue;
                }

                for (int localZ = 0;
                     localZ < 16;
                     localZ++) {

                    BlockState state =
                            states[
                                    localX
                            ][
                                    localY
                            ][
                                    localZ
                            ];

                    if (state == null
                            || state.isAir()) {
                        continue;
                    }

                    BlockPos targetPos =
                            new BlockPos(
                                    minX + localX,
                                    targetY,
                                    minZ + localZ
                            );

                    level.setBlock(
                            targetPos,
                            state,
                            2
                    );
                }
            }
        }

        states = null;
    }

    // =========================================================
    // ORE CHECK
    // =========================================================

    private static boolean isOre(
            BlockState state
    ) {

        return state.is(Blocks.COAL_ORE)
                || state.is(Blocks.DEEPSLATE_COAL_ORE)
                || state.is(Blocks.IRON_ORE)
                || state.is(Blocks.DEEPSLATE_IRON_ORE)
                || state.is(Blocks.COPPER_ORE)
                || state.is(Blocks.DEEPSLATE_COPPER_ORE)
                || state.is(Blocks.GOLD_ORE)
                || state.is(Blocks.DEEPSLATE_GOLD_ORE)
                || state.is(Blocks.REDSTONE_ORE)
                || state.is(Blocks.DEEPSLATE_REDSTONE_ORE)
                || state.is(Blocks.LAPIS_ORE)
                || state.is(Blocks.DEEPSLATE_LAPIS_ORE)
                || state.is(Blocks.DIAMOND_ORE)
                || state.is(Blocks.DEEPSLATE_DIAMOND_ORE)
                || state.is(Blocks.EMERALD_ORE)
                || state.is(Blocks.DEEPSLATE_EMERALD_ORE)
                || state.is(Blocks.ANCIENT_DEBRIS);
    }

    // =========================================================
    // RESET
    // =========================================================

    public static void reset() {

        CHUNK_QUEUE.clear();

        started = false;
        finished = false;

        activeLevel = null;
    }

    // =========================================================
    // STATUS
    // =========================================================

    public static boolean isStarted() {
        return started;
    }

    public static boolean isFinished() {
        return finished;
    }

    public static int getRemainingChunks() {
        return CHUNK_QUEUE.size();
    }

    // =========================================================
    // CHUNK POSITION
    // =========================================================

    private static final class ChunkPosition {

        private final int chunkX;
        private final int chunkZ;

        private ChunkPosition(
                int chunkX,
                int chunkZ
        ) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }
    }
}
