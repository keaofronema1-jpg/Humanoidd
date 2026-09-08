package com.humanoid.horror.world;

import com.humanoid.horror.HumanoidMod;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.level.BiomeLoadingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class WorldTreeReduction {

    private static final ResourceLocation HUMANOID_TREE =
            new ResourceLocation(
                    HumanoidMod.MOD_ID,
                    "humanoidtree"
            );

    /*
     * 600 tick = 30 saniye
     */
    private static final int SPAWN_INTERVAL = 600;

    /*
     * Ağacın oyuncudan maksimum uzaklığı.
     *
     * X: -1000 ... +1000
     * Z: -1000 ... +1000
     */
    private static final int RANDOM_DISTANCE = 1000;

    private static int tickCounter = 0;

    private static final Random RANDOM = new Random();

    private WorldTreeReduction() {
    }

    // =========================================================
    // VANILLA AĞAÇLARINI TAMAMEN ENGELLE
    // =========================================================

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBiomeLoading(BiomeLoadingEvent event) {

        List<Supplier<ConfiguredFeature<?, ?>>> features =
                event.getGeneration()
                        .getFeatures(
                                GenerationStep.Decoration.VEGETAL_DECORATION
                        );

        /*
         * Vegetal decoration içindeki bütün feature zincirlerini
         * kontrol ediyoruz.
         *
         * TreeFeature içeren feature'lar tamamen kaldırılıyor.
         */
        features.removeIf(WorldTreeReduction::containsTreeFeature);
    }

    private static boolean containsTreeFeature(
            Supplier<ConfiguredFeature<?, ?>> supplier
    ) {

        if (supplier == null) {
            return false;
        }

        ConfiguredFeature<?, ?> feature;

        try {
            feature = supplier.get();
        } catch (Exception ignored) {
            return false;
        }

        if (feature == null) {
            return false;
        }

        /*
         * Direkt vanilla TreeFeature.
         */
        if (feature.feature() instanceof TreeFeature) {
            return true;
        }

        /*
         * Wrapped / decorated feature zincirlerini kontrol et.
         */
        try {
            return feature.getFeatures()
                    .anyMatch(
                            WorldTreeReduction::containsTreeFeature
                    );
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean containsTreeFeature(
            ConfiguredFeature<?, ?> feature
    ) {

        if (feature == null) {
            return false;
        }

        if (feature.feature() instanceof TreeFeature) {
            return true;
        }

        try {
            return feature.getFeatures()
                    .anyMatch(
                            WorldTreeReduction::containsTreeFeature
                    );
        } catch (Exception ignored) {
            return false;
        }
    }

    // =========================================================
    // CUSTOM NBT AĞAÇ SİSTEMİ
    // =========================================================

    @SubscribeEvent
    public static void onServerTick(
            TickEvent.ServerTickEvent event
    ) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        MinecraftServer server = event.getServer();

        if (server == null) {
            return;
        }

        /*
         * /start yapılmadan çalışmasın.
         */
        if (!HumanoidMod.isStartTriggered) {
            return;
        }

        tickCounter++;

        if (tickCounter < SPAWN_INTERVAL) {
            return;
        }

        tickCounter = 0;

        ServerLevel overworld =
                server.getLevel(Level.OVERWORLD);

        if (overworld == null) {
            return;
        }

        List<ServerPlayer> players =
                overworld.players();

        if (players.isEmpty()) {
            return;
        }

        /*
         * Rastgele bir oyuncu seç.
         */
        ServerPlayer player =
                players.get(
                        RANDOM.nextInt(players.size())
                );

        spawnRandomTree(overworld, player);
    }

    // =========================================================
    // RASTGELE KONUM
    // =========================================================

    private static void spawnRandomTree(
            ServerLevel level,
            ServerPlayer player
    ) {

        int playerX =
                player.blockPosition().getX();

        int playerZ =
                player.blockPosition().getZ();

        int randomX =
                playerX
                        + RANDOM.nextInt(
                                RANDOM_DISTANCE * 2 + 1
                        )
                        - RANDOM_DISTANCE;

        int randomZ =
                playerZ
                        + RANDOM.nextInt(
                                RANDOM_DISTANCE * 2 + 1
                        )
                        - RANDOM_DISTANCE;

        int surfaceY =
                level.getHeight(
                        Heightmap.Types.WORLD_SURFACE,
                        randomX,
                        randomZ
                );

        BlockPos groundPos =
                new BlockPos(
                        randomX,
                        surfaceY - 1,
                        randomZ
                );

        BlockPos treePos =
                groundPos.above();

        /*
         * Uygun olmayan yüzeyde oluşturma.
         */
        if (!isSuitableGround(level, groundPos)) {
            return;
        }

        /*
         * Ağacın başlayacağı yer doluysa oluşturma.
         */
        if (!level.isEmptyBlock(treePos)) {
            return;
        }

        placeHumanoidTree(level, treePos);
    }

    // =========================================================
    // ZEMİN KONTROLÜ
    // =========================================================

    private static boolean isSuitableGround(
            ServerLevel level,
            BlockPos pos
    ) {

        if (!level.hasChunkAt(pos)) {
            return false;
        }

        if (level.getBlockState(pos).is(Blocks.WATER)) {
            return false;
        }

        if (level.getBlockState(pos).is(Blocks.LAVA)) {
            return false;
        }

        /*
         * Normal toprak türleri.
         */
        if (level.getBlockState(pos).is(BlockTags.DIRT)) {
            return true;
        }

        /*
         * Çimen bloğu.
         */
        if (level.getBlockState(pos).is(Blocks.GRASS_BLOCK)) {
            return true;
        }

        /*
         * Podzol / mycelium gibi yüzeylerde de çalışsın.
         */
        if (level.getBlockState(pos).is(Blocks.PODZOL)) {
            return true;
        }

        if (level.getBlockState(pos).is(Blocks.MYCELIUM)) {
            return true;
        }

        return false;
    }

    // =========================================================
    // NBT'Yİ YERLEŞTİR
    // =========================================================

    private static void placeHumanoidTree(
            ServerLevel level,
            BlockPos position
    ) {

        StructureTemplate template =
                level.getStructureManager()
                        .get(HUMANOID_TREE)
                        .orElse(null);

        if (template == null) {

            System.err.println(
                    "[Humanoid] humanoidtree.nbt bulunamadı!"
            );

            return;
        }

        try {

            StructurePlaceSettings settings =
                    new StructurePlaceSettings();

            settings.setRotation(
                    net.minecraft.world.level.block.Rotation.NONE
            );

            settings.setMirror(
                    net.minecraft.world.level.block.Mirror.NONE
            );

            settings.setIgnoreEntities(false);

            boolean placed =
                    template.placeInWorld(
                            level,
                            position,
                            position,
                            settings,
                            level.random,
                            2
                    );

            if (placed) {

                System.out.println(
                        "[Humanoid] humanoidtree oluşturuldu: "
                                + position.getX()
                                + ", "
                                + position.getY()
                                + ", "
                                + position.getZ()
                );
            }

        } catch (Exception exception) {

            System.err.println(
                    "[Humanoid] humanoidtree oluşturulurken hata!"
            );

            exception.printStackTrace();
        }
    }
}
