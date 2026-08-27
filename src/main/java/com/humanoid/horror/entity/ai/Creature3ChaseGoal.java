package com.humanoid.horror.entity.ai;

import com.humanoid.horror.entity.Creature3;
import com.humanoid.horror.system.HomeTrackerManager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

public class Creature3ChaseGoal extends Goal {

    private final Creature3 entity;

    private int breakDelay = 0;
    private int wallBreakDelay = 0;

    public Creature3ChaseGoal(Creature3 entity) {
        this.entity = entity;

        this.setFlags(
                EnumSet.of(
                        Goal.Flag.MOVE,
                        Goal.Flag.LOOK
                )
        );
    }

    // =========================================================
    // GOAL BAŞLAYABİLİR Mİ?
    // =========================================================

    @Override
    public boolean canUse() {

        if (entity == null) {
            return false;
        }

        if (!entity.isAlive()) {
            return false;
        }

        if (!HomeTrackerManager.ACTIVE) {
            return false;
        }

        if (entity.level().isClientSide()) {
            return false;
        }

        LivingEntity target =
                entity.getTarget();

        return target != null
                && target.isAlive();
    }

    // =========================================================
    // GOAL DEVAM EDEBİLİR Mİ?
    // =========================================================

    @Override
    public boolean canContinueToUse() {

        if (entity == null) {
            return false;
        }

        if (!entity.isAlive()) {
            return false;
        }

        if (!HomeTrackerManager.ACTIVE) {
            return false;
        }

        LivingEntity target =
                entity.getTarget();

        return target != null
                && target.isAlive();
    }

    // =========================================================
    // BAŞLANGIÇ
    // =========================================================

    @Override
    public void start() {

        breakDelay = 0;
        wallBreakDelay = 0;
    }

    // =========================================================
    // DURMA
    // =========================================================

    @Override
    public void stop() {

        breakDelay = 0;
        wallBreakDelay = 0;

        entity.getNavigation().stop();
    }

    // =========================================================
    // TICK
    // =========================================================

    @Override
    public void tick() {

        if (entity == null || !entity.isAlive()) {
            return;
        }

        Level level =
                entity.level();

        if (level.isClientSide()) {
            return;
        }

        // =====================================================
        // HEDEF
        // =====================================================

        LivingEntity target =
                entity.getTarget();

        if (target == null || !target.isAlive()) {

            entity.getNavigation().stop();

            return;
        }

        // =====================================================
        // HEDEFE DOĞRU YÜRÜ
        // =====================================================

        entity.getNavigation().moveTo(
                target,
                1.35D
        );

        // =====================================================
        // HEDEFE BAK
        // =====================================================

        entity.getLookControl().setLookAt(
                target,
                30.0F,
                30.0F
        );

        // =====================================================
        // MESAFE
        // =====================================================

        double distance =
                entity.distanceTo(target);

        // =====================================================
        // OYUNCUYU YAKALA
        // =====================================================

        if (distance <= 1.5D) {

            entity.capturePlayer(target);

            return;
        }

        // =====================================================
        // POZİSYONLAR
        // =====================================================

        BlockPos myPos =
                entity.blockPosition();

        BlockPos targetPos =
                target.blockPosition();

        // =====================================================
        // CHUNK KONTROLÜ
        // =====================================================

        if (!level.isLoaded(myPos)) {
            return;
        }

        if (!level.isLoaded(targetPos)) {
            return;
        }

        // =====================================================
        // ALT BLOK
        // =====================================================

        BlockPos belowMe =
                myPos.below();

        // =====================================================
        // 1. DÜŞMEME / YUKARI ÇIKMA
        // =====================================================

        if (
                level.isEmptyBlock(belowMe)
                        && targetPos.getY() >= myPos.getY()
        ) {

            BlockPos bodySpace =
                    myPos.above();

            BlockPos headSpace =
                    myPos.above(2);

            /*
             * Creature3'ün gövdesi ve kafası
             * için alan boş olmalı.
             */
            if (
                    level.isEmptyBlock(bodySpace)
                            && level.isEmptyBlock(headSpace)
            ) {

                /*
                 * Altına geçici olarak cobblestone
                 * koy ve bir blok yukarı çıkar.
                 */
                level.setBlock(
                        belowMe,
                        Blocks.COBBLESTONE
                                .defaultBlockState(),
                        3
                );

                entity.setPos(
                        entity.getX(),
                        myPos.getY() + 1.0D,
                        entity.getZ()
                );
            }
        }

        // =====================================================
        // 2. AŞAĞI KAZMA
        // =====================================================

        if (
                targetPos.getY()
                        < myPos.getY()
        ) {

            breakDelay++;

            if (breakDelay >= 2) {

                // ---------------------------------------------
                // ALT BLOĞU KIR
                // ---------------------------------------------

                BlockState stateBelow =
                        level.getBlockState(
                                belowMe
                        );

                if (
                        !stateBelow.isAir()
                                && !isUnbreakable(
                                        stateBelow
                                )
                ) {

                    level.destroyBlock(
                            belowMe,
                            false
                    );
                }

                // ---------------------------------------------
                // HEDEFİN ÜST TARAFINI AÇ
                // ---------------------------------------------

                BlockPos targetHead =
                        targetPos.above(2);

                if (
                        level.isLoaded(
                                targetHead
                        )
                ) {

                    BlockState stateTargetHead =
                            level.getBlockState(
                                    targetHead
                            );

                    if (
                            !stateTargetHead.isAir()
                                    && !isUnbreakable(
                                            stateTargetHead
                                    )
                    ) {

                        level.destroyBlock(
                                targetHead,
                                false
                        );
                    }
                }

                breakDelay = 0;
            }

        } else {

            /*
             * Artık aşağı kazmaya gerek yoksa
             * timer sıfırlanır.
             */
            breakDelay = 0;
        }

        // =====================================================
        // 3. DUVAR KIRMA
        // =====================================================

        if (entity.horizontalCollision) {

            wallBreakDelay++;

            if (wallBreakDelay >= 4) {

                BlockPos frontPos =
                        myPos.relative(
                                entity.getDirection()
                        );

                if (
                        level.isLoaded(
                                frontPos
                        )
                ) {

                    BlockState frontState =
                            level.getBlockState(
                                    frontPos
                            );

                    /*
                     * Sadece gerçekten hareketi engelleyen
                     * blokları kır.
                     */
                    if (
                            !frontState.isAir()
                                    && frontState.blocksMotion()
                                    && !isUnbreakable(
                                            frontState
                                    )
                    ) {

                        level.destroyBlock(
                                frontPos,
                                false
                        );
                    }
                }

                wallBreakDelay = 0;
            }

        } else {

            /*
             * Duvara çarpmıyorsa timer sıfırlanır.
             */
            wallBreakDelay = 0;
        }
    }

    // =========================================================
    // KIRILAMAZ BLOK KONTROLÜ
    // =========================================================

    private boolean isUnbreakable(
            BlockState state
    ) {

        if (state == null) {
            return true;
        }

        return state.is(Blocks.BEDROCK)
                || state.is(Blocks.BARRIER)
                || state.getBlock()
                        instanceof CommandBlock
                || state.is(Blocks.STRUCTURE_BLOCK)
                || state.is(Blocks.JIGSAW);
    }
}
