package com.humanoid.horror.entity.ai;

import com.humanoid.horror.entity.Creature1;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.EnumSet;

public class Creature1AI extends Goal {

    private final Creature1 entity;
    private int breakBlockCooldown = 0;

    public Creature1AI(Creature1 entity) {
        this.entity = entity;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.entity.getCurrentTargetPlayer() != null;
    }

    @Override
    public void tick() {
        ServerPlayer target = this.entity.getCurrentTargetPlayer();
        if (target == null) return;

        Level level = this.entity.level();
        double distance = this.entity.distanceTo(target);

        // Mesafe bilgisini HUD için senkronize et
        this.entity.setTargetDistance((int) distance);

        // 1. Hedefe Doğru Yürüme
        this.entity.getNavigation().moveTo(target, 1.0D);
        this.entity.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // 2. Performanslı Blok Kırma (Her 5 tick'te bir)
        if (++breakBlockCooldown >= 5) {
            breakBlockCooldown = 0;
            clearObstaclesAhead(level);
        }

        // 3. 2 Blok Mesafeye Girildiğinde Lanet Tetikleme
        if (distance <= 2.0D) {
            this.entity.executeCurseAndRespawn(target);
        }
    }

    // Önündeki engelleri (Toprak, Taş, Obsidian, Bedrock) temizleyip tünel açma
    private void clearObstaclesAhead(Level level) {
        BlockPos basePos = this.entity.blockPosition().relative(this.entity.getDirection());

        // Ayak, gövde, kafa ve kafa üstü pozisyonları
        BlockPos[] tunnelPositions = new BlockPos[]{
                basePos,                       // Ayak
                basePos.above(),               // Gövde
                basePos.above(2),             // Kafa
                basePos.above(3)              // Kafa Üstü
        };

        for (BlockPos pos : tunnelPositions) {
            if (!level.isEmptyBlock(pos)) {
                // Bedrock dahil tüm blokları doğrudan AIR yapar (Performans için parçacık üretmez)
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }
}
