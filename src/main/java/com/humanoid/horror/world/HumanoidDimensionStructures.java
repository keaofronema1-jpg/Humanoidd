package com.humanoid.horror.world;

import com.humanoid.horror.HumanoidMod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class HumanoidDimensionStructures {

    /*
     * =========================================================
     * DIMENSION 1
     * =========================================================
     */

    private static final ResourceLocation HUMANOID_DIMENSION =
            new ResourceLocation(
                    HumanoidMod.MOD_ID,
                    "humanoid_dimension"
            );

    private static final ResourceKey<Level> DIMENSION1 =
            ResourceKey.create(
                    Registries.DIMENSION,
                    HUMANOID_DIMENSION
            );


    /*
     * =========================================================
     * YAPI
     * =========================================================
     *
     * Dosya:
     *
     * data/humanoid/structures/humanoidbedrock.nbt
     */

    private static final ResourceLocation HUMANOID_BEDROCK =
            new ResourceLocation(
                    HumanoidMod.MOD_ID,
                    "humanoidbedrock"
            );


    /*
     * =========================================================
     * AYARLAR
     * =========================================================
     */

    /*
     * Her 20 tick = yaklaşık 1 saniye kontrol.
     */
    private static final int CHECK_INTERVAL = 20;

    /*
     * Her 300 blokta bir bölge.
     */
    private static final int STRUCTURE_DISTANCE = 300;

    private static int tickCounter = 0;


    /*
     * =========================================================
     * SERVER TICK
     * =========================================================
     */

    @SubscribeEvent
    public static void onServerTick(
            TickEvent.ServerTickEvent event
    ) {

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (event.getServer() == null) {
            return;
        }

        tickCounter++;

        if (tickCounter < CHECK_INTERVAL) {
            return;
        }

        tickCounter = 0;


        /*
         * =====================================================
         * SADECE DIMENSION 1
         * =====================================================
         */

        ServerLevel dimension1 =
                event.getServer().getLevel(
                        DIMENSION1
                );

        if (dimension1 == null) {
            return;
        }


        /*
         * =====================================================
         * DIMENSION 1 OYUNCULARI
         * =====================================================
         */

        for (var player : dimension1.players()) {

            checkStructureForPlayer(
                    dimension1,
                    player.blockPosition()
            );
        }
    }


    /*
     * =========================================================
     * OYUNCUNUN BULUNDUĞU 300x300 BÖLGE
     * =========================================================
     */

    private static void checkStructureForPlayer(
            ServerLevel level,
            BlockPos playerPos
    ) {

        int regionX =
                Math.floorDiv(
                        playerPos.getX(),
                        STRUCTURE_DISTANCE
                );

        int regionZ =
                Math.floorDiv(
                        playerPos.getZ(),
                        STRUCTURE_DISTANCE
                );


        /*
         * Bu bölgenin merkez koordinatı.
         */
        int centerX =
                regionX * STRUCTURE_DISTANCE
                        + STRUCTURE_DISTANCE / 2;

        int centerZ =
                regionZ * STRUCTURE_DISTANCE
                        + STRUCTURE_DISTANCE / 2;


        /*
         * Aynı 300x300 bölgesine tekrar yapı koyma.
         */
        HumanoidStructureData data =
                HumanoidStructureData.get(level);

        long regionKey =
                createRegionKey(
                        regionX,
                        regionZ
                );

        if (data.isPlaced(regionKey)) {
            return;
        }


        /*
         * Yüzey yüksekliğini bul.
         */
        int surfaceY =
                level.getHeight(
                        Heightmap.Types.WORLD_SURFACE,
                        centerX,
                        centerZ
                );


        BlockPos placementPos =
                new BlockPos(
                        centerX,
                        surfaceY,
                        centerZ
                );


        /*
         * =====================================================
         * NBT YAPISINI YÜKLE
         * =====================================================
         */

        StructureTemplateManager manager =
                level.getStructureManager();

        StructureTemplate template =
                manager.get(HUMANOID_BEDROCK)
                        .orElse(null);


        if (template == null) {

            System.err.println(
                    "[Humanoid] humanoidbedrock.nbt bulunamadı!"
            );

            System.err.println(
                    "[Humanoid] Beklenen yol: "
                            + "data/"
                            + HumanoidMod.MOD_ID
                            + "/structures/humanoidbedrock.nbt"
            );

            return;
        }


        /*
         * =====================================================
         * YAPI AYARLARI
         * =====================================================
         */

        StructurePlaceSettings settings =
                new StructurePlaceSettings();


        /*
         * =====================================================
         * YAPIYI YERLEŞTİR
         * =====================================================
         */

        try {

            boolean placed =
                    template.placeInWorld(
                            level,
                            placementPos,
                            placementPos,
                            settings,
                            level.random,
                            2
                    );


            /*
             * Başarılıysa bölgeyi kaydet.
             */
            if (placed) {

                data.markPlaced(regionKey);
                data.setDirty();

                System.out.println(
                        "[Humanoid] humanoidbedrock oluşturuldu: "
                                + "X="
                                + centerX
                                + " Y="
                                + surfaceY
                                + " Z="
                                + centerZ
                );
            }

        } catch (Exception exception) {

            System.err.println(
                    "[Humanoid] humanoidbedrock "
                            + "yerleştirilirken hata oluştu!"
            );

            exception.printStackTrace();
        }
    }


    /*
     * =========================================================
     * REGION ANAHTARI
     * =========================================================
     */

    private static long createRegionKey(
            int regionX,
            int regionZ
    ) {

        return BlockPos.asLong(
                regionX,
                0,
                regionZ
        );
    }


    /*
     * =========================================================
     * SAVED DATA
     * =========================================================
     */

    public static class HumanoidStructureData
            extends SavedData {

        private static final String DATA_NAME =
                "humanoid_dimension_structures";

        private final Set<Long> placedRegions =
                new HashSet<>();


        public HumanoidStructureData() {
        }


        /*
         * Dünyadan yükle.
         */
        public static HumanoidStructureData load(
                CompoundTag tag
        ) {

            HumanoidStructureData data =
                    new HumanoidStructureData();

            long[] positions =
                    tag.getLongArray(
                            "PlacedRegions"
                    );

            for (long position : positions) {

                data.placedRegions.add(
                        position
                );
            }

            return data;
        }


        /*
         * Dünyaya kaydet.
         */
        @Override
        public CompoundTag save(
                CompoundTag tag
        ) {

            long[] positions =
                    new long[
                            placedRegions.size()
                    ];

            int index = 0;

            for (long position :
                    placedRegions) {

                positions[index++] =
                        position;
            }

            tag.putLongArray(
                    "PlacedRegions",
                    positions
            );

            return tag;
        }


        /*
         * Bölge daha önce oluşturuldu mu?
         */
        public boolean isPlaced(
                long region
        ) {

            return placedRegions.contains(
                    region
            );
        }


        /*
         * Bölgeyi oluşturuldu olarak işaretle.
         */
        public void markPlaced(
                long region
        ) {

            placedRegions.add(
                    region
            );
        }


        /*
         * SavedData'yı al.
         */
        public static HumanoidStructureData get(
                ServerLevel level
        ) {

            return level
                    .getDataStorage()
                    .computeIfAbsent(
                            HumanoidStructureData::load,
                            HumanoidStructureData::new,
                            DATA_NAME
                    );
        }
    }
}
