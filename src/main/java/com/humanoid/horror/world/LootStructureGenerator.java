package com.humanoid.horror.world;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;

public class LootStructureGenerator {

    public LootStructureGenerator() {
        super();
    }

    public static void onChunkGenerate(ChunkAccess chunkAccess, LevelAccessor levelAccessor) {
        RandomSource random = levelAccessor.getRandom();
        
        // Chunk başına %3 doğma ihtimali (Nadir ve ürkütücü keşifler)
        if (random.nextDouble() > 0.03) { 
            return;
        }

        ChunkPos chunkPos = chunkAccess.getPos();
        int localX = 8;
        int localZ = 8;
        
        int worldX = (chunkPos.x << 4) + localX;
        int worldZ = (chunkPos.z << 4) + localZ;

        int y = chunkAccess.getHeight(Heightmap.Types.WORLD_SURFACE, localX, localZ);
        
        if (y <= levelAccessor.getMinBuildHeight()) {
            return;
        }
        
        BlockPos centerPos = new BlockPos(worldX, y, worldZ);

        // Önce yıkık dökük evi/harabeyi inşa et
        buildRuinedHut(chunkAccess, centerPos, random);

        // Sandığı harabenin içine yerleştir
        BlockPos chestPos = centerPos.above();
        buildAndFillChest(chunkAccess, chestPos, random);
    }

    /**
     * 5x5 boyutlarında, kırık dökük, yosunlu ve terk edilmiş kulübe harabesi oluşturur.
     */
    private static void buildRuinedHut(ChunkAccess chunkAccess, BlockPos center, RandomSource random) {
        // 1. Zemin Tablosu (5x5): Yosunlu Taş ve Çatlak Taş Tuğlalar
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos floorPos = center.offset(x, 0, z);
                if (random.nextBoolean()) {
                    chunkAccess.setBlockState(floorPos, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), false);
                } else {
                    chunkAccess.setBlockState(floorPos, Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), false);
                }

                // Üstlerindeki otları/ağaçları temizle
                for (int h = 1; h <= 4; h++) {
                    chunkAccess.setBlockState(floorPos.above(h), Blocks.AIR.defaultBlockState(), false);
                }
            }
        }

        // 2. Yıkılmış Duvar Köşeleri ve Çitler (Rastgele yükseklikte kırık duvarlar)
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                // Sadece dış çeperler duvar olsun
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    // Rastgele delikler ve kırık görünüm
                    if (random.nextDouble() < 0.6) {
                        BlockPos wallPos = center.offset(x, 1, z);
                        if (random.nextBoolean()) {
                            chunkAccess.setBlockState(wallPos, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), false);
                        } else {
                            chunkAccess.setBlockState(wallPos, Blocks.OAK_FENCE.defaultBlockState(), false);
                        }

                        // Bazı sütunlar 2 blok yüksekliğinde kalsın (çökmüş çatı hissi)
                        if (random.nextDouble() < 0.3) {
                            chunkAccess.setBlockState(wallPos.above(), Blocks.OAK_FENCE.defaultBlockState(), false);
                        }
                    }
                }
            }
        }

        // 3. Atmosferik Detaylar: Köşelere Örümcek Ağı
        BlockPos webPos = center.offset(1, 2, 1);
        chunkAccess.setBlockState(webPos, Blocks.COBWEB.defaultBlockState(), false);
    }

    private static void buildAndFillChest(ChunkAccess chunkAccess, BlockPos blockPos, RandomSource random) {
        chunkAccess.setBlockState(blockPos, Blocks.CHEST.defaultBlockState(), true);

        BlockEntity blockEntity = chunkAccess.getBlockEntity(blockPos);
        
        if (!(blockEntity instanceof ChestBlockEntity)) {
            return;
        }
        
        ChestBlockEntity chest = (ChestBlockEntity) blockEntity;

        if (random.nextDouble() <= 0.3) { 
            chest.setItem(2, new ItemStack(Items.APPLE));
        }
        if (random.nextDouble() <= 0.3) { 
            chest.setItem(5, new ItemStack(Items.IRON_INGOT));
        }
        if (random.nextDouble() <= 0.7) { 
            chest.setItem(12, new ItemStack(Items.BREAD, 4));
        }

        int containerSize = chest.getContainerSize();
        if (containerSize <= 0) containerSize = 27;

        for (int i = 0; i < containerSize; i++) {
            ItemStack itemStack = chest.getItem(i);
            if (itemStack.isEmpty()) {
                chest.setItem(i, new ItemStack(Blocks.DIRT));
            }
        }

        chest.setChanged(); 
    }
}
