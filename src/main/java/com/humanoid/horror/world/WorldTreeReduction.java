package com.humanoid.horror.world;

import com.humanoid.horror.HumanoidMod;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.level.BiomeLoadingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public final class WorldTreeReduction {

    private static final ResourceLocation TREE_STRUCTURE =
            new ResourceLocation(
                    HumanoidMod.MOD_ID,
                    "humanoidtree"
            );

    /*
     * 30 saniyede bir yeni humanoidtree denemesi.
     */
    private static final int SPAWN_INTERVAL = 600;

    /*
     * Rastgele alan:
     * oyuncudan -1000 / +1000 X-Z.
     */
    private static final int RANDOM_DISTANCE = 1000;

    private static final String DATA_NAME =
            "humanoid_tree_data";

    private static final Random RANDOM =
            new Random();

    private static int tickCounter = 0;

    private WorldTreeReduction() {
    }

    // =========================================================
    // VANILLA AĞAÇLARINI ENGELLE
    // =========================================================

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBiomeLoading(
            BiomeLoadingEvent event
    ) {

        List<Supplier<ConfiguredFeature<?, ?>>> features =
                event.getGeneration()
                        .getFeatures(
                                GenerationStep.Decoration.VEGETAL_DECORATION
                        );

        /*
         * Vanilla tree feature'larını tamamen kaldır.
         *
         * Bu sadece world generation feature'larını etkiler.
         * Oyuncunun sonradan diktiği ağaçlara dokunmaz.
         */
        features.removeIf(
                WorldTreeReduction::isTreeFeature
        );
    }

    private static boolean isTreeFeature(
            Supplier<ConfiguredFeature<?, ?>> supplier
    ) {

        if (supplier == null) {
            return false;
        }

        try {

            ConfiguredFeature<?, ?> feature =
                    supplier.get();

            if (feature == null) {
                return false;
            }

            Feature<?> type =
                    feature.feature();

            /*
             * Direkt TreeFeature.
             */
            if (type instanceof TreeFeature) {
                return true;
            }

            /*
             * Decorated / wrapped feature.
             */
            return feature.getFeatures()
                    .anyMatch(
                            WorldTreeReduction::isTreeFeature
                    );

        } catch (Exception ignored) {

            return false;
        }
    }

    private static boolean isTreeFeature(
            ConfiguredFeature<?, ?> feature
    ) {

        if (feature == null) {
            return false;
        }

        try {

            if (feature.feature()
                    instanceof TreeFeature) {

                return true;
            }

            return feature.getFeatures()
                    .anyMatch(
                            WorldTreeReduction::isTreeFeature
                    );

        } catch (Exception ignored) {

            return false;
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
                event.getServer();

        if (server == null) {
            return;
        }

        /*
         * /start yapılmadan sistem çalışmaz.
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
         * Rastgele oyuncu.
         */
        ServerPlayer player =
                players.get(
                        RANDOM.nextInt(players.size())
                );

        spawnRandomTree(
                overworld,
                player
        );
    }

    // =========================================================
    // RASTGELE AĞAÇ
    // =========================================================

    private static void spawnRandomTree(
            ServerLevel level,
            ServerPlayer player
    ) {

        HumanoidTreeData data =
                HumanoidTreeData.get(level);

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

        /*
         * Chunk yüklü değilse o denemeyi atla.
         */
        if (!level.hasChunkAt(
                new BlockPos(
                        randomX,
                        level.getMinBuildHeight(),
                        randomZ
                )
        )) {
            return;
        }

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
         * Uygun zemin değilse oluşturma.
         */
        if (!isSuitableGround(
                level,
                groundPos
        )) {
            return;
        }

        /*
         * Ağacın başlangıç noktası doluysa oluşturma.
         */
        if (!level.isEmptyBlock(treePos)) {
            return;
        }

        /*
         * Aynı konuma daha önce bizim ağacımız
         * konduysa tekrar koyma.
         */
        if (data.hasTreeAt(treePos)) {
            return;
        }

        /*
         * NBT ağacını yerleştir.
         */
        if (placeTree(
                level,
                treePos
        )) {

            data.addTree(treePos);
            data.setDirty();
        }
    }

    // =========================================================
    // ZEMİN
    // =========================================================

    private static boolean isSuitableGround(
            ServerLevel level,
            BlockPos pos
    ) {

        if (!level.hasChunkAt(pos)) {
            return false;
        }

        if (level.getBlockState(pos)
                .is(Blocks.GRASS_BLOCK)) {
            return true;
        }

        if (level.getBlockState(pos)
                .is(Blocks.DIRT)) {
            return true;
        }

        if (level.getBlockState(pos)
                .is(Blocks.PODZOL)) {
            return true;
        }

        if (level.getBlockState(pos)
                .is(Blocks.MYCELIUM)) {
            return true;
        }

        return false;
    }

    // =========================================================
    // NBT TREE
    // =========================================================

    private static boolean placeTree(
            ServerLevel level,
            BlockPos position
    ) {

        StructureTemplate template =
                level.getStructureManager()
                        .get(TREE_STRUCTURE)
                        .orElse(null);

        if (template == null) {

            System.err.println(
                    "[Humanoid] humanoidtree.nbt bulunamadı!"
            );

            return false;
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

            return template.placeInWorld(
                    level,
                    position,
                    position,
                    settings,
                    level.random,
                    2
            );

        } catch (Exception exception) {

            System.err.println(
                    "[Humanoid] humanoidtree yerleştirme hatası!"
            );

            exception.printStackTrace();

            return false;
        }
    }

    // =========================================================
    // SAVED DATA
    // =========================================================

    public static class HumanoidTreeData
            extends net.minecraft.world.level.saveddata.SavedData {

        private final List<BlockPos> trees =
                new ArrayList<>();

        public HumanoidTreeData() {
        }

        public static HumanoidTreeData load(
                CompoundTag tag
        ) {

            HumanoidTreeData data =
                    new HumanoidTreeData();

            if (tag.contains(
                    "Trees",
                    9
            )) {

                ListTag list =
                        tag.getList(
                                "Trees",
                                10
                        );

                for (int i = 0;
                     i < list.size();
                     i++) {

                    CompoundTag tree =
                            list.getCompound(i);

                    BlockPos pos =
                            new BlockPos(
                                    tree.getInt("X"),
                                    tree.getInt("Y"),
                                    tree.getInt("Z")
                            );

                    data.trees.add(pos);
                }
            }

            return data;
        }

        @Override
        public CompoundTag save(
                CompoundTag tag
        ) {

            ListTag list =
                    new ListTag();

            for (BlockPos pos : trees) {

                CompoundTag tree =
                        new CompoundTag();

                tree.putInt(
                        "X",
                        pos.getX()
                );

                tree.putInt(
                        "Y",
                        pos.getY()
                );

                tree.putInt(
                        "Z",
                        pos.getZ()
                );

                list.add(tree);
            }

            tag.put(
                    "Trees",
                    list
            );

            return tag;
        }

        public boolean hasTreeAt(
                BlockPos pos
        ) {

            for (BlockPos tree : trees) {

                if (tree.equals(pos)) {
                    return true;
                }
            }

            return false;
        }

        public void addTree(
                BlockPos pos
        ) {

            if (!hasTreeAt(pos)) {
                trees.add(pos);
            }
        }

        public static HumanoidTreeData get(
                ServerLevel level
        ) {

            return level.getDataStorage()
                    .computeIfAbsent(
                            HumanoidTreeData::load,
                            HumanoidTreeData::new,
                            DATA_NAME
                    );
        }
    }
}
