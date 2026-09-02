package com.humanoid.horror.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TPBlock2 extends Block {

    public TPBlock2(Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);

        if (!level.isClientSide && entity instanceof ServerPlayer player) {

            // Zaten Overworld'deyse ışınlama yapma
            if (player.level().dimension() == Level.OVERWORLD) {
                return;
            }

            ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);

            if (overworld != null) {
                BlockPos spawnPos = overworld.getSharedSpawnPos();

                player.teleportTo(
                        overworld,
                        spawnPos.getX() + 0.5D,
                        spawnPos.getY() + 1.0D,
                        spawnPos.getZ() + 0.5D,
                        player.getYRot(),
                        player.getXRot()
                );
            }
        }
    }
}
