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
public final class HumanoidCCManager {

    private static final ResourceLocation HUMANOID_CC =
            new ResourceLocation(
                    HumanoidMod.MOD_ID,
                    "humanoidcc"
            );

    /*
     * 30 saniye
     */
    private static final int SPAWN_INTERVAL = 600;

    /*
     * Oyuncudan maksimum rastgele uzaklık.
     */
    private static final int RANDOM_DISTANCE = 1000;

    private static final String DATA_NAME =
            "humanoid_cc_data";

    private static final Random RANDOM =
            new Random();

    private static int tickCounter = 0;

    private HumanoidCCManager() {
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
         * /start olmadan çalışma.
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
         * Rastgele oyuncu seç.
         */
        ServerPlayer player =
                players.get(
                        RANDOM.nextInt(players.size())
                );

        spawnRandomCC(
                overworld,
                player
        );
    }

    // =========================================================
    // RANDOM POSITION
    // =========================================================

    private static void spawnRandomCC(
            ServerLevel level,
            ServerPlayer player
    ) {

        HumanoidCCData data =
                HumanoidCCData.get(level);

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

        BlockPos surfaceCheck =
                new BlockPos(
                        randomX,
                        level.getMinBuildHeight(),
                        randomZ
                );

        /*
         * Chunk yüklü değilse bu denemeyi atla.
         */
        if (!level.hasChunkAt(surfaceCheck)) {
            return;
        }

        int surfaceY =
                level.getHeight(
                        Heightmap.Types.WORLD_SURFACE,
                        randomX,
                        randomZ
                );

        BlockPos position =
                new BlockPos(
                        randomX,
                        surfaceY,
                        randomZ
                );

        /*
         * Aynı konum daha önce kullanıldıysa
         * tekrar oluşturma.
         */
        if (data.hasStructureAt(position)) {
            return;
        }

        /*
         * Su/lava üzerine koyma.
         */
        BlockPos ground =
                position.below();

        if (level.getBlockState(ground)
                .is(Blocks.WATER)) {
            return;
        }

        if (level.getBlockState(ground)
                .is(Blocks.LAVA)) {
            return;
        }

        /*
         * Structure'ı yerleştir.
         */
        if (placeStructure(
                level,
                position
        )) {

            data.addStructure(position);
            data.setDirty();
        }
    }

    // =========================================================
    // PLACE STRUCTURE
    // =========================================================

    private static boolean placeStructure(
            ServerLevel level,
            BlockPos position
    ) {

        StructureTemplate template =
                level.getStructureManager()
                        .get(HUMANOID_CC)
                        .orElse(null);

        if (template == null) {

            System.err.println(
                    "[Humanoid] humanoidcc.nbt bulunamadı!"
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
                    "[Humanoid] humanoidcc.nbt yerleştirme hatası!"
            );

            exception.printStackTrace();

            return false;
        }
    }

    // =========================================================
    // SAVED DATA
    // =========================================================

    public static class HumanoidCCData
            extends net.minecraft.world.level.saveddata.SavedData {

        private final List<BlockPos> structures =
                new ArrayList<>();

        public HumanoidCCData() {
        }

        public static HumanoidCCData load(
                CompoundTag tag
        ) {

            HumanoidCCData data =
                    new HumanoidCCData();

            if (tag.contains(
                    "Structures",
                    9
            )) {

                ListTag list =
                        tag.getList(
                                "Structures",
                                10
                        );

                for (int i = 0;
                     i < list.size();
                     i++) {

                    CompoundTag structure =
                            list.getCompound(i);

                    BlockPos pos =
                            new BlockPos(
                                    structure.getInt("X"),
                                    structure.getInt("Y"),
                                    structure.getInt("Z")
                            );

                    data.structures.add(pos);
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

            for (BlockPos pos : structures) {

                CompoundTag structure =
                        new CompoundTag();

                structure.putInt(
                        "X",
                        pos.getX()
                );

                structure.putInt(
                        "Y",
                        pos.getY()
                );

                structure.putInt(
                        "Z",
                        pos.getZ()
                );

                list.add(structure);
            }

            tag.put(
                    "Structures",
                    list
            );

            return tag;
        }

        public boolean hasStructureAt(
                BlockPos pos
        ) {

            for (BlockPos structure : structures) {

                if (structure.equals(pos)) {
                    return true;
                }
            }

            return false;
        }

        public void addStructure(
                BlockPos pos
        ) {

            if (!hasStructureAt(pos)) {
                structures.add(pos);
            }
        }

        public static HumanoidCCData get(
                ServerLevel level
        ) {

            return level.getDataStorage()
                    .computeIfAbsent(
                            HumanoidCCData::load,
                            HumanoidCCData::new,
                            DATA_NAME
                    );
        }
    }
}
