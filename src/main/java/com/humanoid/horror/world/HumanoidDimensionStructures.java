package com.humanoid.horror.world;

import com.humanoid.horror.HumanoidMod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Mod.EventBusSubscriber(
        modid = HumanoidMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE
)
public class HumanoidDimensionStructures {

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
     * ---------------------------------------------------------
     * HUMANOID BEDROCK
     * ---------------------------------------------------------
     */

    private static final ResourceLocation HUMANOID_BEDROCK =
            new ResourceLocation(
                    HumanoidMod.MOD_ID,
                    "humanoidbedrock"
            );

    /*
     * ---------------------------------------------------------
     * HUMANOID SP
     * ---------------------------------------------------------
     */

    private static final ResourceLocation HUMANOID_SP =
            new ResourceLocation(
                    HumanoidMod.MOD_ID,
                    "humanoidsp"
            );

    /*
     * ---------------------------------------------------------
     * HUMANOID SP2
     * ---------------------------------------------------------
     */

    private static final ResourceLocation HUMANOID_SP2 =
            new ResourceLocation(
                    HumanoidMod.MOD_ID,
                    "humanoidsp2"
            );

    /*
     * Her 20 tick = 1 saniye.
     */

    private static final int CHECK_INTERVAL = 20;

    /*
     * Her 300x300 bölgede maksimum 1 humanoidbedrock.
     */

    private static final int STRUCTURE_DISTANCE = 300;

    private static int tickCounter = 0;

    private static final Random RANDOM = new Random();

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

        ServerLevel dimension1 =
                event.getServer().getLevel(DIMENSION1);

        if (dimension1 == null) {
            return;
        }

        /*
         * Dimension1 sürekli gece.
         */

        dimension1.setDayTime(13000);

        /*
         * -----------------------------------------------------
         * HUMANOID BEDROCK SİSTEMİ
         * -----------------------------------------------------
         */

        for (var player : dimension1.players()) {

            checkStructureForPlayer(
                    dimension1,
                    player.blockPosition()
            );
        }

        /*
         * -----------------------------------------------------
         * HUMANOID SP KONTROLÜ
         * -----------------------------------------------------
         */

        checkHumanoidSPStructures(dimension1);
    }

    /*
     * =========================================================
     * HUMANOID BEDROCK
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

        HumanoidStructureData data =
                HumanoidStructureData.get(level);

        long regionKey =
                createRegionKey(
                        regionX,
                        regionZ
                );

        /*
         * Bu bölgede daha önce yapı oluşturulduysa
         * tekrar oluşturma.
         */

        if (data.isPlaced(regionKey)) {
            return;
        }

        int baseX =
                regionX * STRUCTURE_DISTANCE;

        int baseZ =
                regionZ * STRUCTURE_DISTANCE;

        int randomX =
                baseX + RANDOM.nextInt(
                        STRUCTURE_DISTANCE
                );

        int randomZ =
                baseZ + RANDOM.nextInt(
                        STRUCTURE_DISTANCE
                );

        int surfaceY =
                level.getHeight(
                        Heightmap.Types.WORLD_SURFACE,
                        randomX,
                        randomZ
                );

        BlockPos placementPos =
                new BlockPos(
                        randomX,
                        surfaceY,
                        randomZ
                );

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
                    "[Humanoid] Beklenen yol: data/"
                            + HumanoidMod.MOD_ID
                            + "/structures/humanoidbedrock.nbt"
            );

            return;
        }

        StructurePlaceSettings settings =
                new StructurePlaceSettings();

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

            if (placed) {

                data.markPlaced(regionKey);

                data.setDirty();

                System.out.println(
                        "[Humanoid] humanoidbedrock oluşturuldu: "
                                + "X=" + randomX
                                + " Y=" + surfaceY
                                + " Z=" + randomZ
                );

                /*
                 * -------------------------------------------------
                 * HUMANOID SP DE AYNI BÖLGEDE OLUŞTURULUR.
                 * -------------------------------------------------
                 */

                spawnHumanoidSP(
                        level,
                        placementPos
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
     * HUMANOID SP OLUŞTUR
     * =========================================================
     */

    private static void spawnHumanoidSP(
            ServerLevel level,
            BlockPos placementPos
    ) {

        HumanoidStructureData data =
                HumanoidStructureData.get(level);

        /*
         * Aynı koordinatta zaten SP varsa oluşturma.
         */

        if (data.hasSPAt(placementPos)) {
            return;
        }

        StructureTemplateManager manager =
                level.getStructureManager();

        StructureTemplate template =
                manager.get(HUMANOID_SP)
                        .orElse(null);

        if (template == null) {

            System.err.println(
                    "[Humanoid] humanoidsp.nbt bulunamadı!"
            );

            System.err.println(
                    "[Humanoid] Beklenen yol: data/"
                            + HumanoidMod.MOD_ID
                            + "/structures/humanoidsp.nbt"
            );

            return;
        }

        /*
         * HumanoidSP'yi humanoidbedrock ile aynı
         * başlangıç koordinatında oluşturuyoruz.
         */

        StructurePlaceSettings settings =
                new StructurePlaceSettings();

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

            if (!placed) {
                return;
            }

            /*
             * Structure'ın yerleştirildiği gerçek alanı bul.
             */

            Vec3i size =
                    template.getSize();

            BoundingBox box =
                    new BoundingBox(
                            placementPos.getX(),
                            placementPos.getY(),
                            placementPos.getZ(),
                            placementPos.getX()
                                    + size.getX() - 1,
                            placementPos.getY()
                                    + size.getY() - 1,
                            placementPos.getZ()
                                    + size.getZ() - 1
                    );

            /*
             * Structure içindeki bütün Spawner'ları bul.
             */

            List<BlockPos> spawners =
                    findSpawners(
                            level,
                            box
                    );

            if (spawners.isEmpty()) {

                System.err.println(
                        "[Humanoid] humanoidsp içinde "
                                + "Spawner bulunamadı!"
                );

                /*
                 * Structure yine de oluşturuldu.
                 * Ancak takip edilecek spawner yok.
                 */

                return;
            }

            /*
             * Structure merkezini hesapla.
             */

            double centerX =
                    (box.minX() + box.maxX()) / 2.0D;

            double centerY =
                    (box.minY() + box.maxY()) / 2.0D;

            double centerZ =
                    (box.minZ() + box.maxZ()) / 2.0D;

            /*
             * Merkeze en yakın Spawner'ı seç.
             */

            BlockPos centerSpawner =
                    findClosestSpawner(
                            spawners,
                            centerX,
                            centerY,
                            centerZ
                    );

            /*
             * Takip bilgilerini kaydet.
             */

            data.addSP(
                    placementPos,
                    centerSpawner
            );

            data.setDirty();

            System.out.println(
                    "[Humanoid] humanoidsp oluşturuldu: "
                            + "X=" + placementPos.getX()
                            + " Y=" + placementPos.getY()
                            + " Z=" + placementPos.getZ()
                            + " | Kontrol Spawner: "
                            + centerSpawner
            );

        } catch (Exception exception) {

            System.err.println(
                    "[Humanoid] humanoidsp "
                            + "yerleştirilirken hata oluştu!"
            );

            exception.printStackTrace();
        }
    }

    /*
     * =========================================================
     * SPAWNER'LARI BUL
     * =========================================================
     */

    private static List<BlockPos> findSpawners(
            ServerLevel level,
            BoundingBox box
    ) {

        List<BlockPos> result =
                new ArrayList<>();

        for (int x = box.minX();
             x <= box.maxX();
             x++) {

            for (int y = box.minY();
                 y <= box.maxY();
                 y++) {

                for (int z = box.minZ();
                     z <= box.maxZ();
                     z++) {

                    BlockPos pos =
                            new BlockPos(
                                    x,
                                    y,
                                    z
                            );

                    BlockState state =
                            level.getBlockState(pos);

                    if (state.is(Blocks.SPAWNER)) {

                        result.add(pos);
                    }
                }
            }
        }

        return result;
    }

    /*
     * =========================================================
     * MERKEZE EN YAKIN SPAWNER
     * =========================================================
     */

    private static BlockPos findClosestSpawner(
            List<BlockPos> spawners,
            double centerX,
            double centerY,
            double centerZ
    ) {

        BlockPos closest =
                spawners.get(0);

        double closestDistance =
                Double.MAX_VALUE;

        for (BlockPos pos : spawners) {

            double dx =
                    (pos.getX() + 0.5D)
                            - centerX;

            double dy =
                    (pos.getY() + 0.5D)
                            - centerY;

            double dz =
                    (pos.getZ() + 0.5D)
                            - centerZ;

            double distance =
                    dx * dx
                            + dy * dy
                            + dz * dz;

            if (distance < closestDistance) {

                closestDistance =
                        distance;

                closest =
                        pos;
            }
        }

        return closest;
    }

    /*
     * =========================================================
     * HUMANOID SP KONTROLÜ
     * =========================================================
     */

    private static void checkHumanoidSPStructures(
            ServerLevel level
    ) {

        HumanoidStructureData data =
                HumanoidStructureData.get(level);

        List<SPInstance> instances =
                new ArrayList<>(
                        data.getSPInstances()
                );

        for (SPInstance instance : instances) {

            /*
             * Daha önce SP2'ye dönüştüyse atla.
             */

            if (instance.replaced) {
                continue;
            }

            BlockState state =
                    level.getBlockState(
                            instance.spawnerPos
                    );

            /*
             * Spawner hâlâ yerinde.
             */

            if (state.is(Blocks.SPAWNER)) {
                continue;
            }

            /*
             * Spawner kırıldı.
             *
             * HEMEN SP2.
             */

            replaceWithHumanoidSP2(
                    level,
                    instance
            );
        }
    }

    /*
     * =========================================================
     * HUMANOID SP → SP2
     * =========================================================
     */

    private static void replaceWithHumanoidSP2(
            ServerLevel level,
            SPInstance instance
    ) {

        HumanoidStructureData data =
                HumanoidStructureData.get(level);

        /*
         * Aynı instance ikinci kez çalışmasın.
         */

        if (instance.replaced) {
            return;
        }

        StructureTemplateManager manager =
                level.getStructureManager();

        StructureTemplate template =
                manager.get(HUMANOID_SP2)
                        .orElse(null);

        if (template == null) {

            System.err.println(
                    "[Humanoid] humanoidsp2.nbt bulunamadı!"
            );

            System.err.println(
                    "[Humanoid] Beklenen yol: data/"
                            + HumanoidMod.MOD_ID
                            + "/structures/humanoidsp2.nbt"
            );

            return;
        }

        StructurePlaceSettings settings =
                new StructurePlaceSettings();

        try {

            /*
             * SP2 TAM OLARAK SP'nin placement
             * koordinatında oluşturulur.
             */

            boolean placed =
                    template.placeInWorld(
                            level,
                            instance.structurePos,
                            instance.structurePos,
                            settings,
                            level.random,
                            2
                    );

            if (!placed) {
                return;
            }

            instance.replaced =
                    true;

            data.setDirty();

            System.out.println(
                    "[Humanoid] humanoidsp spawner kırıldı!"
            );

            System.out.println(
                    "[Humanoid] humanoidsp2 ANINDA oluşturuldu: "
                            + "X=" + instance.structurePos.getX()
                            + " Y=" + instance.structurePos.getY()
                            + " Z=" + instance.structurePos.getZ()
            );

        } catch (Exception exception) {

            System.err.println(
                    "[Humanoid] humanoidsp2 "
                            + "yerleştirilirken hata oluştu!"
            );

            exception.printStackTrace();
        }
    }

    /*
     * =========================================================
     * REGION KEY
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

        private final List<SPInstance> spInstances =
                new ArrayList<>();

        public HumanoidStructureData() {
        }

        /*
         * -----------------------------------------------------
         * LOAD
         * -----------------------------------------------------
         */

        public static HumanoidStructureData load(
                CompoundTag tag
        ) {

            HumanoidStructureData data =
                    new HumanoidStructureData();

            /*
             * Eski humanoidbedrock kayıtları.
             */

            long[] positions =
                    tag.getLongArray(
                            "PlacedRegions"
                    );

            for (long position : positions) {

                data.placedRegions.add(
                        position
                );
            }

            /*
             * HumanoidSP kayıtları.
             */

            if (tag.contains(
                    "SPInstances",
                    9
            )) {

                ListTag list =
                        tag.getList(
                                "SPInstances",
                                10
                        );

                for (int i = 0;
                     i < list.size();
                     i++) {

                    CompoundTag spTag =
                            list.getCompound(i);

                    BlockPos structurePos =
                            new BlockPos(
                                    spTag.getInt("SX"),
                                    spTag.getInt("SY"),
                                    spTag.getInt("SZ")
                            );

                    BlockPos spawnerPos =
                            new BlockPos(
                                    spTag.getInt("PX"),
                                    spTag.getInt("PY"),
                                    spTag.getInt("PZ")
                            );

                    boolean replaced =
                            spTag.getBoolean(
                                    "Replaced"
                            );

                    data.spInstances.add(
                            new SPInstance(
                                    structurePos,
                                    spawnerPos,
                                    replaced
                            )
                    );
                }
            }

            return data;
        }

        /*
         * -----------------------------------------------------
         * SAVE
         * -----------------------------------------------------
         */

        @Override
        public CompoundTag save(
                CompoundTag tag
        ) {

            /*
             * Region kayıtları.
             */

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

            /*
             * SP kayıtları.
             */

            ListTag list =
                    new ListTag();

            for (SPInstance instance :
                    spInstances) {

                CompoundTag spTag =
                        new CompoundTag();

                spTag.putInt(
                        "SX",
                        instance.structurePos.getX()
                );

                spTag.putInt(
                        "SY",
                        instance.structurePos.getY()
                );

                spTag.putInt(
                        "SZ",
                        instance.structurePos.getZ()
                );

                spTag.putInt(
                        "PX",
                        instance.spawnerPos.getX()
                );

                spTag.putInt(
                        "PY",
                        instance.spawnerPos.getY()
                );

                spTag.putInt(
                        "PZ",
                        instance.spawnerPos.getZ()
                );

                spTag.putBoolean(
                        "Replaced",
                        instance.replaced
                );

                list.add(spTag);
            }

            tag.put(
                    "SPInstances",
                    list
            );

            return tag;
        }

        /*
         * -----------------------------------------------------
         * REGION
         * -----------------------------------------------------
         */

        public boolean isPlaced(
                long region
        ) {

            return placedRegions.contains(
                    region
            );
        }

        public void markPlaced(
                long region
        ) {

            placedRegions.add(
                    region
            );
        }

        /*
         * -----------------------------------------------------
         * SP
         * -----------------------------------------------------
         */

        public void addSP(
                BlockPos structurePos,
                BlockPos spawnerPos
        ) {

            spInstances.add(
                    new SPInstance(
                            structurePos,
                            spawnerPos,
                            false
                    )
            );
        }

        public boolean hasSPAt(
                BlockPos structurePos
        ) {

            for (SPInstance instance :
                    spInstances) {

                if (instance.structurePos.equals(
                        structurePos
                )) {

                    return true;
                }
            }

            return false;
        }

        public List<SPInstance> getSPInstances() {
            return spInstances;
        }

        /*
         * -----------------------------------------------------
         * GET DATA
         * -----------------------------------------------------
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

    /*
     * =========================================================
     * SP INSTANCE
     * =========================================================
     */

    private static class SPInstance {

        private final BlockPos structurePos;

        private final BlockPos spawnerPos;

        private boolean replaced;

        private SPInstance(
                BlockPos structurePos,
                BlockPos spawnerPos,
                boolean replaced
        ) {

            this.structurePos =
                    structurePos;

            this.spawnerPos =
                    spawnerPos;

            this.replaced =
                    replaced;
        }
    }
}
