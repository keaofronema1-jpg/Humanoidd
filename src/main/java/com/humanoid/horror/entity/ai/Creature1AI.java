package com.humanoid.horror.entity.ai;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.entity.Creature1;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.EnumSet;

public class Creature1AI extends Goal {

    // =========================================================
    // ENTITY
    // =========================================================

    private final Creature1 entity;

    // =========================================================
    // BLOCK BREAK COOLDOWN
    // =========================================================

    private int breakBlockCooldown = 0;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Creature1AI(Creature1 entity) {

        this.entity = entity;

        this.setFlags(
                EnumSet.of(
                        Goal.Flag.MOVE,
                        Goal.Flag.LOOK
                )
        );
    }

    // =========================================================
    // CAN USE
    // =========================================================

    @Override
    public boolean canUse() {

        /*
         * /start verilmeden AI çalışmasın.
         */
        if (!HumanoidMod.isStartTriggered) {
            return false;
        }

        return this.entity.getCurrentTargetPlayer() != null;
    }

    // =========================================================
    // CAN CONTINUE
    // =========================================================

    @Override
    public boolean canContinueToUse() {

        /*
         * /start sistemi kapanırsa AI hemen dursun.
         */
        if (!HumanoidMod.isStartTriggered) {
            return false;
        }

        return this.entity.getCurrentTargetPlayer() != null;
    }

    // =========================================================
    // START
    // =========================================================

    @Override
    public void start() {

        breakBlockCooldown = 0;
    }

    // =========================================================
    // STOP
    // =========================================================

    @Override
    public void stop() {

        breakBlockCooldown = 0;

        this.entity
                .getNavigation()
                .stop();
    }

    // =========================================================
    // TICK
    // =========================================================

    @Override
    public void tick() {

        /*
         * /start yoksa hiçbir AI işlemi yapma.
         */
        if (!HumanoidMod.isStartTriggered) {

            this.entity
                    .getNavigation()
                    .stop();

            return;
        }

        // =====================================================
        // HEDEF OYUNCU
        // =====================================================

        ServerPlayer target =
                this.entity.getCurrentTargetPlayer();

        if (target == null) {

            this.entity
                    .getNavigation()
                    .stop();

            return;
        }

        Level level =
                this.entity.level();

        // =====================================================
        // GERÇEK MESAFE
        // =====================================================

        double distance =
                this.entity.distanceTo(target);

        /*
         * Creature1'in HUD için kullandığı
         * gerçek mesafeyi güncelle.
         */
        this.entity.setTargetDistance(
                (int) distance
        );

        // =====================================================
        // OYUNCUYA DOĞRU YÜRÜ
        // =====================================================

        this.entity
                .getNavigation()
                .moveTo(
                        target,
                        1.0D
                );

        // =====================================================
        // OYUNCUYA BAK
        // =====================================================

        this.entity
                .getLookControl()
                .setLookAt(
                        target,
                        30.0F,
                        30.0F
                );

        // =====================================================
        // ENGEL KIRMA
        // =====================================================

        breakBlockCooldown++;

        /*
         * Her 5 tick'te bir önünü kontrol et.
         */
        if (breakBlockCooldown >= 5) {

            breakBlockCooldown = 0;

            clearObstaclesAhead(level);
        }

        /*
         * Jumpscare burada yapılmıyor.
         *
         * Creature1.tick() içerisinde:
         *
         * distance <= 2.0D
         *
         * olduğunda jumpscare ve curse çalışıyor.
         */
    }

    // =========================================================
    // ÖNDEKİ ENGELLERİ TEMİZLE
    // =========================================================

    private void clearObstaclesAhead(
            Level level
    ) {

        BlockPos basePos =
                this.entity
                        .blockPosition()
                        .relative(
                                this.entity.getDirection()
                        );

        /*
         * Creature1'in önündeki yaklaşık
         * 4 blok yüksekliğindeki alan.
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

            /*
             * Zaten boşsa hiçbir şey yapma.
             */
            if (level.isEmptyBlock(pos)) {
                continue;
            }

            /*
             * Önündeki bloğu doğrudan AIR yap.
             *
             * Bu mevcut davranış korunuyor:
             * taş, toprak, obsidyen, bedrock vb.
             * bloklar temizlenebilir.
             */
            level.setBlock(
                    pos,
                    Blocks.AIR.defaultBlockState(),
                    3
            );
        }
    }
}
