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

        this.setFlags(
                EnumSet.of(
                        Goal.Flag.MOVE,
                        Goal.Flag.LOOK
                )
        );
    }

    @Override
    public boolean canUse() {
        return this.entity.getCurrentTargetPlayer() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.entity.getCurrentTargetPlayer() != null;
    }

    @Override
    public void start() {
        breakBlockCooldown = 0;
    }

    @Override
    public void stop() {
        breakBlockCooldown = 0;
        this.entity.getNavigation().stop();
    }

    @Override
    public void tick() {

        // Güncel hedef oyuncuyu al
        ServerPlayer target =
                this.entity.getCurrentTargetPlayer();

        if (target == null) {
            this.entity.getNavigation().stop();
            return;
        }

        Level level = this.entity.level();

        // Oyuncuya olan gerçek mesafe
        double distance =
                this.entity.distanceTo(target);

        // HUD'daki mesafeyi güncelle
        this.entity.setTargetDistance(
                (int) distance
        );

        // =====================================================
        // OYUNCUYA DOĞRU YÜRÜ
        // =====================================================

        this.entity.getNavigation().moveTo(
                target,
                1.0D
        );

        // =====================================================
        // OYUNCUYA BAK
        // =====================================================

        this.entity.getLookControl().setLookAt(
                target,
                30.0F,
                30.0F
        );

        // =====================================================
        // ENGELLERİ KIR
        // Her 5 tick'te bir kontrol
        // =====================================================

        breakBlockCooldown++;

        if (breakBlockCooldown >= 5) {

            breakBlockCooldown = 0;

            clearObstaclesAhead(level);
        }

        /*
         * DİKKAT:
         *
         * Jumpscare ve curse işlemi burada yapılmıyor.
         *
         * Bunlar Creature1.tick() içerisinde zaten yapılıyor:
         *
         * distance <= 2.0
         *
         * olduğunda:
         *
         * ModMessages.sendToPlayer(
         *     new JumpscarePacket(),
         *     oyuncu
         * );
         *
         * executeCurse(oyuncu);
         *
         * çalışıyor.
         *
         * Böylece iki kere tetiklenmesi engelleniyor.
         */
    }

    // =========================================================
    // ÖNÜNDEKİ ENGELLERİ TEMİZLE
    // =========================================================

    private void clearObstaclesAhead(Level level) {

        BlockPos basePos =
                this.entity.blockPosition()
                        .relative(
                                this.entity.getDirection()
                        );

        /*
         * Canavarın önünde 4 blokluk tünel açılır:
         *
         * basePos       = ayak
         * basePos.above = gövde
         * above(2)      = kafa
         * above(3)      = kafa üstü
         */

        BlockPos[] tunnelPositions =
                new BlockPos[]{

                        basePos,

                        basePos.above(),

                        basePos.above(2),

                        basePos.above(3)
                };

        for (BlockPos pos : tunnelPositions) {

            if (level.isEmptyBlock(pos)) {
                continue;
            }

            /*
             * Engeli doğrudan AIR yap.
             *
             * Böylece:
             * - Toprak
             * - Taş
             * - Obsidyen
             * - hatta Bedrock
             *
             * dahil önündeki blok temizlenir.
             */

            level.setBlock(
                    pos,
                    Blocks.AIR.defaultBlockState(),
                    3
            );
        }
    }
}
