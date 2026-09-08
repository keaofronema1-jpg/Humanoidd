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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
     * 30 saniyede bir ağaç oluşturma denemesi.
     */
    private static final int SPAWN_INTERVAL = 600;

    /*
     * Rastgele X/Z mesafesi.
     *
     * Oyuncunun:
     * -1000 / +1000
     * blok çevresinde.
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
         * /start yapılmadan çalışma.
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
                        RANDOM.nextInt(
                                players.size()
                        )
                );

        spawnRandomTree(
                overworld,
                player
        );
    }

    // =========================================================
    // RASTGELE HUMANOID TREE
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

        /*
         * Tamamen rastgele X/Z.
         */
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

        BlockPos checkPos =
                new BlockPos(
                        randomX,
                        level.getMinBuildHeight(),
                        randomZ
                );

        /*
         * Chunk yüklü değilse bu denemeyi atla.
         */
        if (!level.hasChunkAt(checkPos)) {
            return;
        }

        /*
         * Dünya yüzeyini bul.
         */
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
         * Sadece uygun zemin.
         */
        if (!isSuitableGround(
                level,
                groundPos
        )) {
            return;
        }

        /*
         * Başlangıç noktası boş olmalı.
         */
        if (!level.isEmptyBlock(treePos)) {
            return;
        }

        /*
         * Aynı noktaya daha önce ağaç koyulduysa
         * tekrar koyma.
         */
        if (data.hasTreeAt(treePos)) {
            return;
        }

        /*
         * NBT'yi yerleştir.
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
    // ZEMİN KONTROLÜ
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
    // NBT YERLEŞTİRME
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
                    Rotation.NONE
            );

            settings.setMirror(
                    Mirror.NONE
            );

            settings.setIgnoreEntities(
                    false
            );

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

                    data.trees.add(
                            new BlockPos(
                                    tree.getInt("X"),
                                    tree.getInt("Y"),
                                    tree.getInt("Z")
                            )
                    );
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
