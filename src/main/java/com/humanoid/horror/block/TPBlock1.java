package com.humanoid.horror.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TPBlock1 extends Block {

    public TPBlock1(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(
            Level level,
            BlockPos pos,
            BlockState state,
            Entity entity
    ) {
        super.stepOn(level, pos, state, entity);

        if (level.isClientSide) {
            return;
        }

        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel targetLevel =
                level.getServer().getLevel(
                        Level.OVERWORLD
                );

        if (targetLevel == null) {
            return;
        }

        BlockPos targetPos =
                targetLevel.getSharedSpawnPos();

        player.teleportTo(
                targetLevel,
                targetPos.getX() + 0.5D,
                targetPos.getY() + 1.0D,
                targetPos.getZ() + 0.5D,
                player.getYRot(),
                player.getXRot()
        );
    }
}
