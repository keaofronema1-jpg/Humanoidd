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
    public void stepOn(
            Level level,
            BlockPos pos,
            BlockState state,
            Entity entity
    ) {

        super.stepOn(
                level,
                pos,
                state,
                entity
        );

        if (level.isClientSide) {
            return;
        }

        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        /*
         * Oyuncu TPBlock2'ye temas edince
         * Overworld spawn noktasına gönder.
         */
        ServerLevel overworld =
                player.getServer()
                        .getLevel(Level.OVERWORLD);

        if (overworld == null) {
            return;
        }

        BlockPos spawnPos =
                overworld.getSharedSpawnPos();

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
