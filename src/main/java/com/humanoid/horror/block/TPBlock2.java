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

    /*
     * ---------------------------------------------------------
     * ÜSTÜNE BASMA
     * ---------------------------------------------------------
     */

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

        tryTeleport(
                level,
                entity
        );
    }

    /*
     * ---------------------------------------------------------
     * BLOĞA TEMAS
     * ---------------------------------------------------------
     */

    @Override
    public void entityInside(
            BlockState state,
            Level level,
            BlockPos pos,
            Entity entity
    ) {

        super.entityInside(
                state,
                level,
                pos,
                entity
        );

        tryTeleport(
                level,
                entity
        );
    }

    /*
     * ---------------------------------------------------------
     * TELEPORT
     * ---------------------------------------------------------
     */

    private static void tryTeleport(
            Level level,
            Entity entity
    ) {

        /*
         * Sadece server.
         */

        if (level.isClientSide) {
            return;
        }

        /*
         * Sadece oyuncu.
         */

        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        /*
         * Server kontrolü.
         */

        if (player.getServer() == null) {
            return;
        }

        /*
         * Overworld.
         */

        ServerLevel overworld =
                player.getServer()
                        .getLevel(Level.OVERWORLD);

        if (overworld == null) {
            return;
        }

        /*
         * Overworld'ün gerçek spawn noktası.
         */

        BlockPos spawnPos =
                overworld.getSharedSpawnPos();

        /*
         * Spawn'ın üstüne bırak.
         */

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
