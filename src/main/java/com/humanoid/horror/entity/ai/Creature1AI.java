package com.humanoid.horror.entity.ai;

import com.humanoid.horror.HumanoidMod;
import com.humanoid.horror.entity.Creature1;
import com.humanoid.horror.entity.Creature1HUDState;

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
    // SABİT HIZ
    // =========================================================

    /*
     * Her zaman aynı hız.
     *
     * Sayaç düştükçe hız artmayacak.
     */
    private static final double CREATURE_SPEED = 1.05D;

    // =========================================================
    // MESAFE TOLERANSI
    // =========================================================

    private static final double DISTANCE_TOLERANCE = 0.75D;

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

        // =====================================================
        // /START KONTROLÜ
        // =====================================================

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
        // ORTAK HUD SAYACINI OKU
        // =====================================================

        /*
         * Artık Creature1 kendi gerçek mesafesini
         * HUD sayacının üzerine yazmıyor.
         *
         * AI doğrudan ortak state'i okuyor.
         */
        int targetDistance =
                Creature1HUDState.getDistance();

        if (targetDistance < 0) {
            targetDistance = 0;
        }

        // =====================================================
        // GERÇEK MESAFE
        // =====================================================

        double realDistance =
                this.entity.distanceTo(target);

        // =====================================================
        // HEDEF MESAFEYE DOĞRU KOŞ
        // =====================================================

        /*
         * Örnek:
         *
         * State = 90
         * Creature1 = 120 blok uzakta
         * -> 1.05 hızla oyuncuya doğru koşar.
         *
         * Creature1 = 90 blok uzakta
         * -> durur.
         *
         * State = 89 olduğunda
         * -> tekrar 89'a doğru koşar.
         */
        if (realDistance >
                targetDistance + DISTANCE_TOLERANCE) {

            this.entity
                    .getNavigation()
                    .moveTo(
                            target,
                            CREATURE_SPEED
                    );

        } else {

            this.entity
                    .getNavigation()
                    .stop();
        }

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

        if (breakBlockCooldown >= 5) {

            breakBlockCooldown = 0;

            clearObstaclesAhead(level);
        }

        /*
         * Jumpscare burada yapılmıyor.
         *
         * Creature1.tick() içindeki mevcut
         * jumpscare kontrolü çalışmaya devam eder.
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

            level.setBlock(
                    pos,
                    Blocks.AIR.defaultBlockState(),
                    3
            );
        }
    }
}
