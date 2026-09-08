package com.humanoid.horror.world;

import com.humanoid.horror.HumanoidMod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.Queue;

@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class HumanoidChunkSystem {

    /*
     * Hazırlanacak alan:
     *
     * 16 x 16 CHUNK
     *
     * = 256 x 256 blok
     */
    private static final int AREA_CHUNKS = 16;

    /*
     * Oyuncunun ilk giriş konumundan
     * X ekseninde 200 blok ileri.
     */
    private static final int START_DISTANCE = 200;

    /*
     * Her server tick'inde işlenecek chunk sayısı.
     */
    private static final int CHUNKS_PER_TICK = 2;

    /*
     * Minecraft 1.20.1 dünya yüksekliği.
     */
    private static final int WORLD_MIN_Y = -64;
    private static final int WORLD_MAX_Y = 319;

    /*
     * Cevherler yukarı kopyalanırken taş olacak.
     */
    private static final BlockState ORE_REPLACEMENT =
            Blocks.STONE.defaultBlockState();

    /*
     * İşlenecek chunk kuyruğu.
     */
    private static final Queue<ChunkPosition> CHUNK_QUEUE =
            new ArrayDeque<>();

    /*
     * Sistem daha önce başlatıldı mı?
     */
    private static boolean started = false;

    /*
     * Tüm chunklar tamamlandı mı?
     */
    private static boolean finished = false;

    /*
     * İlk oyuncunun konumu alındı mı?
     *
     * Bu değer sayesinde oyuncu listesi
     * her tick kontrol edilmez.
     */
    private static boolean playerPositionCaptured = false;

    /*
     * Çalışılan dünya.
     */
    private static ServerLevel activeLevel = null;

    /*
     * İlk oyuncunun girişteki konumu.
     */
    private static BlockPos initialPlayerPosition = null;

    private HumanoidChunkSystem() {
    }

    /*
     * ============================================================
     * OYUNCU GİRİŞİ
     * ============================================================
     *
     * İlk oyuncu dünyaya girdiğinde yalnızca bir kez çalışır.
     *
     * /start beklenmez.
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        /*
         * Sistem zaten başlatıldıysa başka oyuncular
         * geldiğinde tekrar başlamaz.
         */
        if (started || playerPositionCaptured) {
            return;
        }

        MinecraftServer server = player.getServer();

        if (server == null) {
            return;
        }

        ServerLevel level = server.overworld();

        if (level == null) {
            return;
        }

        /*
         * Oyuncunun ilk giriş konumunu yalnızca bir kere alıyoruz.
         */
        initialPlayerPosition = player.blockPosition();

        playerPositionCaptured = true;

        /*
         * Chunk sistemi hemen başlıyor.
         */
        startSystem(server, level, initialPlayerPosition);
    }

    /*
     * ============================================================
     * SERVER TICK
     * ============================================================
     *
     * Burada oyuncu aranmaz.
     *
     * Sadece daha önce oluşturulmuş chunk kuyruğu işlenir.
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        /*
         * Oyuncu henüz girmediyse hiçbir şey yapma.
         *
         * Burada oyuncu listesi kontrol edilmiyor.
         */
        if (!started) {
            return;
        }

        /*
         * İş bittiyse tick maliyeti yok denecek kadar az.
         */
        if (finished) {
            return;
        }

        /*
         * Aktif dünya yoksa devam etme.
         */
        if (activeLevel == null) {
            return;
        }

        /*
         * Tick başına sınırlı sayıda chunk işle.
         */
        for (int i = 0; i < CHUNKS_PER_TICK; i++) {

            ChunkPosition position = CHUNK_QUEUE.poll();

            if (position == null) {

                finished = true;

                System.out.println(
                        "[HumanoidChunkSystem] "
                                + "16x16 chunk sistemi tamamlandı."
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

    /*
     * ============================================================
     * SİSTEMİ BAŞLAT
     * ============================================================
     */
    private static void startSystem(
            MinecraftServer server,
            ServerLevel level,
            BlockPos playerPosition
    ) {

        if (started) {
            return;
        }

        if (playerPosition == null) {
            return;
        }

        /*
         * Oyuncudan 200 blok ileri.
         *
         * Y ve Z aynı kalıyor.
         */
        BlockPos areaStart = new BlockPos(
                playerPosition.getX() + START_DISTANCE,
                playerPosition.getY(),
                playerPosition.getZ()
        );

        /*
         * Blok koordinatını chunk koordinatına çevir.
         */
        int startChunkX =
                Math.floorDiv(areaStart.getX(), 16);

        int startChunkZ =
                Math.floorDiv(areaStart.getZ(), 16);

        /*
         * Eski kuyruk temizleniyor.
         */
        CHUNK_QUEUE.clear();

        /*
         * 16 x 16 = 256 chunk.
         */
        for (int x = 0; x < AREA_CHUNKS; x++) {

            for (int z = 0; z < AREA_CHUNKS; z++) {

                CHUNK_QUEUE.add(
                        new ChunkPosition(
                                startChunkX + x,
                                startChunkZ + z
                        )
                );
            }
        }

        activeLevel = level;

        started = true;
        finished = false;

        System.out.println(
                "[HumanoidChunkSystem] Başladı."
                        + " PlayerStart=" + playerPosition
                        + ", AreaStart=" + areaStart
                        + ", ChunkStart=("
                        + startChunkX
                        + ","
                        + startChunkZ
                        + ")"
                        + ", ToplamChunk=256"
        );
    }

    /*
     * ============================================================
     * CHUNK İŞLE
     * ============================================================
     */
    private static void processChunk(
            ServerLevel level,
            int chunkX,
            int chunkZ
    ) {

        if (level == null) {
            return;
        }

        /*
         * Chunk'ı yükle.
         */
        level.getChunk(chunkX, chunkZ);

        int minBlockX = chunkX * 16;
        int minBlockZ = chunkZ * 16;

        int maxBlockX = minBlockX + 15;
        int maxBlockZ = minBlockZ + 15;

        /*
         * Chunk'ın en üst dolu bloğunu bul.
         */
        int highestY = findHighestNonBedrockBlock(
                level,
                minBlockX,
                minBlockZ,
                maxBlockX,
                maxBlockZ
        );

        if (highestY < WORLD_MIN_Y) {
            return;
        }

        /*
         * Bulunan yüzeyin hemen üstünden
         * kopyalamaya başla.
         */
        int copyStartY = highestY + 1;

        if (copyStartY > WORLD_MAX_Y) {
            return;
        }

        /*
         * Chunk'ın alt kısmını yukarı kopyala.
         */
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
                "[HumanoidChunkSystem] Chunk işlendi: ("
                        + chunkX
                        + ","
                        + chunkZ
                        + ")"
        );
    }

    /*
     * ============================================================
     * EN ÜST DOLU BLOĞU BUL
     * ============================================================
     */
    private static int findHighestNonBedrockBlock(
            ServerLevel level,
            int minX,
            int minZ,
            int maxX,
            int maxZ
    ) {

        int highest = WORLD_MIN_Y - 1;

        /*
         * Yukarıdan aşağı tarıyoruz.
         */
        for (int y = WORLD_MAX_Y; y >= WORLD_MIN_Y; y--) {

            boolean found = false;

            for (int x = minX; x <= maxX; x++) {

                for (int z = minZ; z <= maxZ; z++) {

                    BlockState state =
                            level.getBlockState(
                                    new BlockPos(x, y, z)
                            );

                    /*
                     * Hava sayılmaz.
                     */
                    if (state.isAir()) {
                        continue;
                    }

                    /*
                     * Bedrock sayılmaz.
                     */
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

    /*
     * ============================================================
     * CHUNK'I YUKARI KOPYALA
     * ============================================================
     */
    private static void copyChunkUp(
            ServerLevel level,
            int minX,
            int minZ,
            int maxX,
            int maxZ,
            int highestY,
            int copyStartY
    ) {

        int sourceMinY = WORLD_MIN_Y;
        int sourceMaxY = highestY;

        int height =
                sourceMaxY - sourceMinY + 1;

        if (height <= 0) {
            return;
        }

        /*
         * Hedefte ulaşılabilecek maksimum Y.
         */
        int maxTargetY =
                copyStartY + height - 1;

        /*
         * Dünya sınırını aşacaksa kırp.
         */
        if (maxTargetY > WORLD_MAX_Y) {

            height =
                    WORLD_MAX_Y - copyStartY + 1;

            if (height <= 0) {
                return;
            }
        }

        /*
         * Chunk içindeki blokları RAM'e al.
         */
        BlockState[][][] states =
                new BlockState[16][height][16];

        /*
         * KAYNAK BLOKLAR.
         */
        for (int localX = 0; localX < 16; localX++) {

            for (int localY = 0; localY < height; localY++) {

                int sourceY =
                        sourceMinY + localY;

                for (int localZ = 0; localZ < 16; localZ++) {

                    BlockPos sourcePos =
                            new BlockPos(
                                    minX + localX,
                                    sourceY,
                                    minZ + localZ
                            );

                    BlockState state =
                            level.getBlockState(sourcePos);

                    /*
                     * Bedrock yukarı taşınmayacak.
                     */
                    if (state.is(Blocks.BEDROCK)) {

                        states[localX][localY][localZ] =
                                Blocks.AIR.defaultBlockState();

                        continue;
                    }

                    /*
                     * Cevherleri taş yap.
                     */
                    if (isOre(state)) {

                        states[localX][localY][localZ] =
                                ORE_REPLACEMENT;

                        continue;
                    }

                    /*
                     * Normal blok.
                     */
                    states[localX][localY][localZ] =
                            state;
                }
            }
        }

        /*
         * HEDEF BLOKLAR.
         */
        for (int localX = 0; localX < 16; localX++) {

            for (int localY = 0; localY < height; localY++) {

                int targetY =
                        copyStartY + localY;

                if (targetY > WORLD_MAX_Y) {
                    continue;
                }

                for (int localZ = 0; localZ < 16; localZ++) {

                    BlockState state =
                            states[localX][localY][localZ];

                    if (state == null) {
                        continue;
                    }

                    /*
                     * Havayı yazmaya gerek yok.
                     */
                    if (state.isAir()) {
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
    }

    /*
     * ============================================================
     * CEVHER KONTROLÜ
     * ============================================================
     */
    private static boolean isOre(BlockState state) {

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

    /*
     * ============================================================
     * RESET
     * ============================================================
     */
    public static void reset() {

        CHUNK_QUEUE.clear();

        started = false;
        finished = false;

        playerPositionCaptured = false;

        activeLevel = null;
        initialPlayerPosition = null;
    }

    /*
     * ============================================================
     * DIŞARIDAN ERİŞİM
     * ============================================================
     */

    public static boolean isStarted() {
        return started;
    }

    public static boolean isFinished() {
        return finished;
    }

    public static boolean isPlayerPositionCaptured() {
        return playerPositionCaptured;
    }

    public static int getRemainingChunks() {
        return CHUNK_QUEUE.size();
    }

    public static BlockPos getInitialPlayerPosition() {
        return initialPlayerPosition;
    }

    /*
     * ============================================================
     * CHUNK POZİSYONU
     * ============================================================
     */
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
