package com.humanoid.horror.world;

import com.humanoid.horror.HumanoidMod;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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
public class HumanoidDİmensionStructures {

    /*
     * =========================================================
     * YAPILAR
     * =========================================================
     */

    private static final ResourceLocation HUMANOID_SUN =
            new ResourceLocation(
                    HumanoidMod.MOD_ID,
                    "humanoidsun"
            );

    private static final ResourceLocation HUMANOID_ST =
            new ResourceLocation(
                    HumanoidMod.MOD_ID,
                    "humanoidst"
            );


    /*
     * =========================================================
     * AYARLAR
     * =========================================================
     */

    /*
     * Her 40 tick = yaklaşık 2 saniyede bir kontrol.
     */
    private static final int CHECK_INTERVAL = 40;

    /*
     * Çok nadir oluşma ihtimali.
     *
     * 1 / 20.000  -> humanoidsun
     * 1 / 25.000  -> humanoidst
     */
    private static final int HUMANOID_SUN_CHANCE = 20_000;
    private static final int HUMANOID_ST_CHANCE = 25_000;

    /*
     * Yapı oyuncunun tam yanında oluşmasın.
     *
     * Minimum uzaklık:
     * 100 blok
     *
     * Maksimum uzaklık:
     * 300 blok
     */
    private static final int MIN_DISTANCE = 100;
    private static final int MAX_DISTANCE = 300;

    /*
     * HUMANOIDSUN yüksekliği.
     *
     * Dünya yüzeyinden 60 blok yukarı.
     */
    private static final int SUN_HEIGHT_OFFSET = 60;

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

        tickCounter++;

        if (tickCounter < CHECK_INTERVAL) {
            return;
        }

        tickCounter = 0;

        if (event.getServer() == null) {
            return;
        }


        /*
         * =====================================================
         * SADECE OVERWORLD
         * =====================================================
         */

        ServerLevel overworld =
                event.getServer().getLevel(Level.OVERWORLD);

        if (overworld == null) {
            return;
        }


        /*
         * Oyuncuları kontrol ediyoruz.
         */
        for (var player : overworld.players()) {

            RandomSource random = overworld.random;


            /*
             * =================================================
             * HUMANOIDSUN
             * =================================================
             */

            if (random.nextInt(HUMANOID_SUN_CHANCE) == 0) {

                trySpawnStructure(
                        overworld,
                        player.blockPosition(),
                        HUMANOID_SUN,
                        true
                );
            }


            /*
             * =================================================
             * HUMANOIDST
             * =================================================
             */

            if (random.nextInt(HUMANOID_ST_CHANCE) == 0) {

                trySpawnStructure(
                        overworld,
                        player.blockPosition(),
                        HUMANOID_ST,
                        false
                );
            }
        }
    }


    /*
     * =========================================================
     * YAPI OLUŞTURMA
     * =========================================================
     */

    private static void trySpawnStructure(
            ServerLevel level,
            BlockPos playerPos,
            ResourceLocation structureId,
            boolean isSun
    ) {

        /*
         * Rastgele yön.
         */
        double angle =
                level.random.nextDouble() * Math.PI * 2.0D;


        /*
         * Rastgele uzaklık.
         */
        int distance =
                MIN_DISTANCE
                        + level.random.nextInt(
                                MAX_DISTANCE - MIN_DISTANCE + 1
                        );


        /*
         * Oyuncunun etrafında rastgele koordinat.
         */
        int x =
                playerPos.getX()
                        + (int) Math.round(
                                Math.cos(angle) * distance
                        );

        int z =
                playerPos.getZ()
                        + (int) Math.round(
                                Math.sin(angle) * distance
                        );


        /*
         * Dünya yüzeyini bul.
         */
        int surfaceY =
                level.getHeight(
                        Heightmap.Types.WORLD_SURFACE,
                        x,
                        z
                );


        /*
         * HUMANOIDSUN:
         *
         * Yüzeyden 60 blok yukarı.
         *
         * HUMANOIDST:
         *
         * Yüzey seviyesinde.
         */
        int y;

        if (isSun) {
            y = surfaceY + SUN_HEIGHT_OFFSET;
        } else {
            y = surfaceY;
        }


        BlockPos placementPos =
                new BlockPos(x, y, z);


        /*
         * Aynı koordinatta daha önce yapı oluşturulduysa
         * tekrar oluşturma.
         */
        HumanoidStructureData data =
                HumanoidStructureData.get(level);


        long structureKey =
                createStructureKey(
                        x,
                        z
                );


        if (data.isPlaced(structureKey)) {
            return;
        }


        /*
         * =====================================================
         * NBT YAPISINI YÜKLE
         * =====================================================
         */

        StructureTemplateManager manager =
                level.getStructureManager();

        StructureTemplate template =
                manager.get(structureId).orElse(null);


        if (template == null) {

            System.err.println(
                    "[Humanoid] NBT yapı bulunamadı: "
                            + structureId
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

            template.placeInWorld(
                    level,
                    placementPos,
                    placementPos,
                    settings,
                    level.random,
                    2
            );


            /*
             * Başarıyla yerleştirildikten sonra kaydet.
             */
            data.markPlaced(structureKey);
            data.setDirty();


            System.out.println(
                    "[Humanoid] Yapı oluşturuldu: "
                            + structureId
                            + " X="
                            + x
                            + " Y="
                            + y
                            + " Z="
                            + z
            );

        } catch (Exception exception) {

            System.err.println(
                    "[Humanoid] Yapı yerleştirilirken hata oluştu: "
                            + structureId
            );

            exception.printStackTrace();
        }
    }


    /*
     * =========================================================
     * YAPI ANAHTARI
     * =========================================================
     *
     * Aynı X/Z koordinatındaki yapının daha önce oluşturulup
     * oluşturulmadığını takip eder.
     */

    private static long createStructureKey(
            int x,
            int z
    ) {

        return BlockPos.asLong(
                x,
                0,
                z
        );
    }


    /*
     * =========================================================
     * SAVED DATA
     * =========================================================
     *
     * Dünya kapanıp açıldığında hangi yapıların oluşturulduğunu
     * hatırlar.
     */

    public static class HumanoidStructureData
            extends SavedData {

        private static final String DATA_NAME =
                "humanoid_overworld_structures";

        private final Set<Long> placedStructures =
                new HashSet<>();


        public HumanoidStructureData() {
        }


        /*
         * Dünyadan kayıt yükleme.
         */
        public static HumanoidStructureData load(
                CompoundTag tag
        ) {

            HumanoidStructureData data =
                    new HumanoidStructureData();

            long[] positions =
                    tag.getLongArray(
                            "PlacedStructures"
                    );

            for (long position : positions) {

                data.placedStructures.add(
                        position
                );
            }

            return data;
        }


        /*
         * Dünyaya kayıt kaydetme.
         */
        @Override
        public CompoundTag save(
                CompoundTag tag
        ) {

            long[] positions =
                    new long[
                            placedStructures.size()
                    ];

            int index = 0;

            for (long position :
                    placedStructures) {

                positions[index++] =
                        position;
            }

            tag.putLongArray(
                    "PlacedStructures",
                    positions
            );

            return tag;
        }


        /*
         * Daha önce oluşturuldu mu?
         */
        public boolean isPlaced(
                long position
        ) {

            return placedStructures.contains(
                    position
            );
        }


        /*
         * Oluşturuldu olarak işaretle.
         */
        public void markPlaced(
                long position
        ) {

            placedStructures.add(
                    position
            );
        }


        /*
         * SavedData'yı dünyadan al.
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
