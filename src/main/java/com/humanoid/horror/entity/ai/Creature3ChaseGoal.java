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
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!HomeTrackerManager.ACTIVE || this.entity == null || !this.entity.isAlive()) {
            return false;
        }
        LivingEntity target = this.entity.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        LivingEntity target = this.entity.getTarget();
        if (target == null || !target.isAlive()) return;
        
        Level level = this.entity.level();
        if (level == null || level.isClientSide()) return;

        this.entity.getNavigation().moveTo(target, 1.35D);
        this.entity.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double distance = this.entity.distanceTo(target);

        // Yakalama mesafesi
        if (distance <= 1.5D) {
            this.entity.capturePlayer(target);
            return;
        }

        BlockPos myPos = this.entity.blockPosition();
        BlockPos targetPos = target.blockPosition();
        BlockPos belowMe = myPos.below();

        // Chunk yüklü değilse işlem yapma (Crash önleyici)
        if (!level.isLoaded(myPos) || !level.isLoaded(targetPos)) {
            return;
        }

        // 1. DÜŞMEME / YUKARI TIRMANMA (Tavan Sıkışması Korumalı)
        if (level.isEmptyBlock(belowMe) && targetPos.getY() >= myPos.getY()) {
            BlockPos headAbove = myPos.above(2);
            if (level.isEmptyBlock(headAbove)) {
                level.setBlock(belowMe, Blocks.COBBLESTONE.defaultBlockState(), 3);
                this.entity.setPos(this.entity.getX(), myPos.getY() + 1.0D, this.entity.getZ());
            }
        }

        // 2. AŞAĞI KAZI
        if (targetPos.getY() < myPos.getY()) {
            breakDelay++;
            if (breakDelay >= 2) {
                BlockState stateBelow = level.getBlockState(belowMe);
                if (!stateBelow.isAir() && !isUnbreakable(stateBelow)) {
                    level.destroyBlock(belowMe, false);
                }

                BlockPos targetHead = targetPos.above(2);
                if (level.isLoaded(targetHead)) {
                    BlockState stateTargetHead = level.getBlockState(targetHead);
                    if (!stateTargetHead.isAir() && !isUnbreakable(stateTargetHead)) {
                        level.destroyBlock(targetHead, false);
                    }
                }
                breakDelay = 0;
            }
        }

        // 3. DUVAR KIRMA
        if (this.entity.horizontalCollision) {
            wallBreakDelay++;
            if (wallBreakDelay >= 4) {
                BlockPos frontPos = myPos.relative(this.entity.getDirection());
                if (level.isLoaded(frontPos)) {
                    BlockState frontState = level.getBlockState(frontPos);
                    if (!frontState.isAir() && !isUnbreakable(frontState) && frontState.blocksMotion()) {
                        level.destroyBlock(frontPos, false);
                    }
                }
                wallBreakDelay = 0;
            }
        } else {
            wallBreakDelay = 0;
        }
    }

    private boolean isUnbreakable(BlockState state) {
        if (state == null) return true;
        return state.is(Blocks.BEDROCK)
                || state.is(Blocks.BARRIER)
                || state.getBlock() instanceof CommandBlock
                || state.is(Blocks.STRUCTURE_BLOCK)
                || state.is(Blocks.JIGSAW);
    }
}
